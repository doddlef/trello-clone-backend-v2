package org.kevin.trello_v2.auth.service.impl

import org.apache.commons.validator.routines.EmailValidator
import org.kevin.trello_v2.account.mapper.AccountInsertQuery
import org.kevin.trello_v2.account.mapper.AccountMapper
import org.kevin.trello_v2.account.mapper.AccountUpdateQuery
import org.kevin.trello_v2.account.model.Account
import org.kevin.trello_v2.account.repo.AccountCacheRepo
import org.kevin.trello_v2.auth.AuthProperties
import org.kevin.trello_v2.auth.MAX_NICKNAME_LENGTH
import org.kevin.trello_v2.auth.mapper.EmailActiveTokenMapper
import org.kevin.trello_v2.auth.model.EmailActiveToken
import org.kevin.trello_v2.auth.service.RegisterService
import org.kevin.trello_v2.auth.service.vo.EmailRegisterVo
import org.kevin.trello_v2.email.service.impl.EmailService
import org.kevin.trello_v2.framework.BadArgumentException
import org.kevin.trello_v2.framework.TrelloException
import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.framework.utils.TokenGenerator
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class RegisterServiceImpl(
    private val accountMapper: AccountMapper,
    private val accountCacheRepo: AccountCacheRepo,
    private val emailActiveTokenMapper: EmailActiveTokenMapper,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder,
    private val authProperties: AuthProperties,
): RegisterService {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    /**
     * Validates the registration data provided in the EmailRegisterVO.
     * This method checks if the email format is valid and if the password meets the required criteria.
     * It should throw an exception if validation fails.
     * at least 8 characters long, contains at least one letter and one number.
     *
     * @param vo The EmailRegisterVO containing the registration details.
     */
    private fun isPasswordValid(password: String): Boolean {
        val regex = """^(?=.*[A-Za-z])(?=.*\d).{8,}$""".toRegex()
        return regex.matches(password)
    }

    private fun validateRegistrationData(vo: EmailRegisterVo) {
        // Validate the registration data, such as checking if the email is already registered
        // and if the password meets the required criteria.
        // This method should throw an exception if validation fails.
        val (email, password, nickname) = vo
        email.takeIf {
            EmailValidator.getInstance().isValid(email)
        } ?: throw BadArgumentException("Invalid email format: $email")
        password.takeIf {
            isPasswordValid(password)
        } ?: throw BadArgumentException("Password must be at least 8 characters long and contain at least one letter and one number.")
        nickname.takeIf {
            it.isNotBlank() && it.length <= MAX_NICKNAME_LENGTH
        } ?: throw BadArgumentException("Nickname must be non-empty and up to $MAX_NICKNAME_LENGTH characters long.")

        // check if the email is already registered
        accountMapper.findByEmail(email)?.let {
            throw BadArgumentException("Email $email is already registered, account uid: ${it.uid}")
        }
    }

    /**
     * Inserts a new account into the database.
     * This method should be called after validating the registration data.
     *
     * @param vo The EmailRegisterVO containing the registration details.
     * @return The UID of the newly created account.
     */
    private fun insertAccount(vo: EmailRegisterVo): String {
        val query = AccountInsertQuery(
            email = vo.email,
            nickname = vo.nickname,
            password = passwordEncoder.encode(vo.password),
            verified = false,
        )
        val count = accountMapper.insertAccount(query)
        if (count != 1) throw TrelloException("Failed to insert account for email: query: ${query}")
        return query.uid
    }

    /**
     * Creates an email activation token for the given user ID.
     * This method generates a token, sets its expiration time, and inserts it into the database.
     *
     * @param uid The unique identifier of the user for whom the token is created.
     * @return The content of the created email activation token.
     */
    private fun createEmailActiveToken(uid: String): String {
        EmailActiveToken(
            content = TokenGenerator.generateToken(48),
            uid = uid,
            expiredAt = LocalDateTime.now().plusHours(authProperties.emailActiveTokenLifeHours)
        )
            .let {
                val count = emailActiveTokenMapper.insertToken(it)
                if (count != 1) throw TrelloException("Failed to insert email active token for uid: $uid")
                return it.content
            }
    }

    private fun sendEmailWithToken(email: String, token: String) {
        emailService.sendEmail(
            to = email,
            subject = "Activate your Trello account",
            body = "Please click the link to activate your account: http://localhost:5173/email/active?token=$token"
        )
    }

    @Transactional
    override fun emailRegister(vo: EmailRegisterVo): ApiResponse {
        validateRegistrationData(vo)
        val uid = insertAccount(vo)
        logger.debug("Successfully registered user with email: ${vo.email}, uid: $uid")

        val token = createEmailActiveToken(uid)
        logger.debug("Successfully generate token for email active, uid: $uid, token: $token")
        sendEmailWithToken(vo.email, token)

        return ApiResponse.success()
            .message("Registration successful, please check your email to activate your account.")
            .add("accountUid" to uid)
            .build()
    }

    /**
     * Validates the email activation token.
     * This method checks if the token exists in the database and if it has not expired.
     *
     * @param content The content of the email activation token.
     * @return The EmailActiveToken entity if the token is valid.
     * @throws BadArgumentException if the token is invalid or expired.
     */
    private fun validateEmailToken(content: String): EmailActiveToken {
        val token = emailActiveTokenMapper.findByContent(content)
        if (token == null || token.expiredAt.isBefore(LocalDateTime.now()))
            throw BadArgumentException("Invalid or expired token: $content")
        return token
    }

    /**
     * Activates the account associated with the given user ID.
     * This method updates the account status to verified in the database.
     *
     * @param uid The unique identifier of the user whose account is to be activated.
     * @throws TrelloException if the activation fails.
     */
    private fun activeAccount(uid: String) {
        val count = AccountUpdateQuery(
            uid = uid,
            verified = true
        ).let {
            val count = accountMapper.updateAccount(it)
            count
        }

        if (count != 1) throw TrelloException("Failed to activate account for uid: $uid")
        accountCacheRepo.evict(uid)
    }

    @Transactional
    override fun verificationEmail(content: String): Account {
        val (_, uid, _) = validateEmailToken(content)
        logger.debug("Email verification successful for token: $content, uid: $uid")
        activeAccount(uid)
        emailActiveTokenMapper.deleteByContent(content)

        return accountMapper.findByEmail(uid)?.let {
            accountCacheRepo.save(it)
            it
        } ?: throw TrelloException("Failed to find active account for uid: $uid")
    }

    /**
     * Creates or reuses an email activation token for the given user ID.
     * If a token already exists for the user, it will be reused; otherwise, a new token will be created.
     *
     * @param uid The unique identifier of the user for whom the token is created or reused.
     * @return The content of the email activation token.
     */
    private fun createOrReuse(uid: String): String {
        val tokenEntity = emailActiveTokenMapper.findByUid(uid)
        if (tokenEntity != null) return tokenEntity.content
        return createEmailActiveToken(uid)
    }

    @Transactional
    override fun resendVerificationEmail(email: String): ApiResponse {
        val account = accountMapper.findByEmail(email)
            ?: throw BadArgumentException("Account not existed")
        if (account.verified) throw BadArgumentException("Account has been verified")

        val token = createOrReuse(account.uid)
        sendEmailWithToken(account.email, token)

        return ApiResponse.success()
            .message("Email verification link has been resent, please check your email.")
            .add("accountUid" to account.uid)
            .build()
    }

    @Transactional
    override fun cleanUpExpiredTokens() {
        logger.info("Cleaning up expired email active tokens")
        val deletedCount = emailActiveTokenMapper.deleteAllExpiredTokens()
        logger.info("Deleted $deletedCount expired email active tokens")
    }
}