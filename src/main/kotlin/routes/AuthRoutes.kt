package com.ktproject.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.ktproject.auth.JwtConfig
import com.ktproject.models.LoginRequest
import com.ktproject.models.RegisterRequest
import com.ktproject.models.Users
import com.ktproject.services.ExposedUser
import com.ktproject.services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import models.responseError
import models.responseSuccess
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

@Serializable
data class TokenResponse(
    val token: String,
    val userId: Int,
    val role: String
)

fun hashPassword(password: String): String =
    BCrypt.withDefaults().hashToString(12, password.toCharArray())

fun verifyPassword(password: String, hash: String): Boolean =
    BCrypt.verifyer().verify(password.toCharArray(), hash).verified

fun Application.configureAuthRoutes(database: Database, userService: UserService) {
    routing {
        // Регистрация нового пользователя
        post("/register") {
            val registerRequest = call.receive<RegisterRequest>()

            // Проверка, что пользователь с таким email еще не существует
            val userExists = transaction(database) {
                Users.select(Users.email.eq(registerRequest.email)).count() > 0
            }

            if (userExists) {
                call.responseError("Пользователь с таким email уже существует", HttpStatusCode.Conflict)
                return@post
            }

            // Хешируем пароль и создаем пользователя
            val hashedPassword = hashPassword(registerRequest.password)

            val user = ExposedUser(
                id = 0,
                name = registerRequest.name,
                email = registerRequest.email,
                passwordHash = hashedPassword,
                phone = registerRequest.phone,
                role = "user"
            )

            val userId = userService.create(user)

            // Генерируем JWT токен
            val token = JwtConfig.generateToken(userId, "user")

            call.responseSuccess(TokenResponse(token, userId, "user"))
        }
        get("/login") {
            call.responseSuccess(TokenResponse("test-token", 1, "admin"))
        }

        // Авторизация пользователя
        post("/login") {
            val loginRequest = call.receive<LoginRequest>()

            // Проверяем существование пользователя с указанным email
            val users = userService.readAll()
            val user = users.find { it.email == loginRequest.email }
                ?: return@post call.responseError("Неверный email", HttpStatusCode.Unauthorized)

            // Проверяем пароль
            if (!verifyPassword(loginRequest.password, user.passwordHash)) {
                return@post call.responseError("Неверный пароль", HttpStatusCode.Unauthorized)
            }

            // Генерируем JWT токен
            val token = JwtConfig.generateToken(user.id, user.role)

            call.responseSuccess(TokenResponse(token, user.id, user.role))
        }

        // Выход из системы
        authenticate {
            post("/logout") {
                // В JWT аутентификации выход обычно реализуется на стороне клиента путём удаления токена
                // Здесь просто подтверждаем успешный выход
                call.responseSuccess("Выход выполнен успешно")
            }
        }
    }
}