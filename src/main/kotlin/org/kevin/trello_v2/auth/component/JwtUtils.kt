package org.kevin.trello_v2.auth.component

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.kevin.trello_v2.auth.AuthProperties
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtils(
    private val authProperties: AuthProperties,
) {
    private val signingKey: SecretKey by lazy {
        val keyBytes = Decoders.BASE64.decode(authProperties.jwtSecret)
        Keys.hmacShaKeyFor(keyBytes)
    }

    fun isTokenExpired(token: String) = extractExpiration(token).before(Date())

    fun isTokenValid(token: String, check: String): Boolean {
        val subject = extractSubject(token)
        return check == subject && !isTokenExpired(token)
    }

    fun generateToken(subject: String, expire: Date, otherClaims: Map<String, Any> = emptyMap()): String {
        return Jwts.builder()
            .claims(otherClaims)
            .subject(subject)
            .issuedAt(Date())
            .expiration(expire)
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact()
    }

    fun extractSubject(token: String) = extractClaim(token, Claims::getSubject)

    private fun extractExpiration(token: String) = extractClaim(token, Claims::getExpiration)

    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}