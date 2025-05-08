package com.ktproject

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ktproject.auth.JwtConfig
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.responseError
import org.jetbrains.exposed.sql.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureSecurity() {

    install(Authentication) {
        jwt("admin") {
            realm = "ktor"
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (credential.payload.getClaim("id").asInt() != null) {
                    if (credential.payload.getClaim("role").asString() == "admin") {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.responseError( "У вас нет прав администратора для доступа к этому ресурсу", HttpStatusCode.Unauthorized)
            }
        }
        jwt("user") {
            realm = "ktor"
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (credential.payload.getClaim("id").asInt() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.responseError("Требуется аутентификация", HttpStatusCode.Unauthorized)
            }
        }
        jwt {
            realm = "ktor"
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (credential.payload.getClaim("id").asInt() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.responseError("Требуется аутентификация", HttpStatusCode.Unauthorized)
            }
        }
    }
}