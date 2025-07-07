package org.kevin.trello_v2.auth.service.impl

import org.apache.commons.validator.routines.EmailValidator
import org.kevin.trello_v2.account.mapper.AccountInsertQuery
import org.kevin.trello_v2.account.mapper.AccountMapper
import org.kevin.trello_v2.account.model.Account
import org.kevin.trello_v2.auth.MAX_NICKNAME_LENGTH
import org.kevin.trello_v2.auth.service.RegisterService
import org.kevin.trello_v2.auth.service.vo.EmailRegisterVo
import org.kevin.trello_v2.framework.BadArgumentException
import org.kevin.trello_v2.framework.TrelloException
import org.kevin.trello_v2.framework.response.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class RegisterServiceImpl(
    private val accountMapper: AccountMapper,
    private val passwordEncoder: PasswordEncoder,
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

    override fun emailRegister(vo: EmailRegisterVo): ApiResponse {
        validateRegistrationData(vo)
        val uid = insertAccount(vo)
        logger.debug("Successfully registered user with email: ${vo.email}, uid: $uid")

        // TODO: Send verification email with the uid and token

        return ApiResponse.success()
            .message("Registration successful, please check your email to activate your account.")
            .add("accountUid" to uid)
            .build()
    }

    override fun verificationEmail(token: String): Account {
        TODO("Not yet implemented")
    }

    override fun resendVerificationEmail(uid: String): ApiResponse {
        TODO("Not yet implemented")
    }

    override fun cleanUpExpiredTokens() {
        TODO("Not yet implemented")
    }
}