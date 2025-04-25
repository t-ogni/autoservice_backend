package com.ktproject.routes


import io.ktor.http.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.statements.InsertStatement
import com.ktproject.models.Users
import com.ktproject.auth.JwtConfig
import at.favre.lib.crypto.bcrypt.BCrypt
import com.ktproject.models.LoginRequest
import com.ktproject.models.RegisterRequest
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.insertAndGetId


fun hashPassword(password: String): String =
    BCrypt.withDefaults().hashToString(12, password.toCharArray())

fun verifyPassword(password: String, hash: String): Boolean =
    BCrypt.verifyer().verify(password.toCharArray(), hash).verified

fun Application.configureAuthRoutes() {
    routing {
        post("/register") {
            val request = call.receive<RegisterRequest>()

            val exists = transaction {
                Users.selectAll().where { Users.email eq request.email }.count() > 0
            }

            if (exists) {
                call.respond(HttpStatusCode.Conflict, "User already exists")
                return@post
            }

            val id = transaction {
                Users.insert { row ->
                    row[name] = request.name
                    row[email] = request.email
                    row[passwordHash] = hashPassword(request.password)
                    row[role] = "user"
                }[Users.id]
            }

            call.respond(HttpStatusCode.Created, mapOf("token" to JwtConfig.generateToken(id)))
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            val user = transaction {
                Users.selectAll().where { Users.email eq request.email }.firstOrNull()
            }


            if (user == null || !verifyPassword(request.password, user[Users.passwordHash])) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid email or password")
                return@post
            }

            val token = JwtConfig.generateToken(user[Users.id])
            call.respond(mapOf("token" to token))
        }
    }
}
