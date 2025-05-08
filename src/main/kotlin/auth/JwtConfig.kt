package com.ktproject.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    private const val secret = "secret" // В продакшене следует использовать безопасный ключ из конфигурации
    private const val issuer = "ktor-autoservice"
    private const val audience = "ktor-users"
    private const val validity = 3_600_000 * 24 * 365 // 1 год в миллисекундах

    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()
        
    fun generateToken(userId: Int, role: String): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("id", userId)
        .withClaim("role", role)
        .withExpiresAt(Date(System.currentTimeMillis() + validity))
        .sign(algorithm)
}