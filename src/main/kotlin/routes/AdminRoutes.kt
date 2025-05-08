package com.ktproject.routes

import com.ktproject.models.Users
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

data class SetAdminRequest(val userId: Int)

fun Application.configureAdminRoutes(userService: UserService) {
    routing {
        authenticate("admin") {
            post("/admin/set-admin") {
                val request = call.receive<SetAdminRequest>()
                
                val user = userService.read(request.userId) 
                    ?: return@post call.responseError("Пользователь не найден", HttpStatusCode.NotFound)
                
                userService.updatePartial(request.userId, mapOf("role" to "admin"))
                call.responseSuccess("Пользователь назначен администратором")
            }
            
            post("/admin/unset-admin") {
                val request = call.receive<SetAdminRequest>()
                
                val user = userService.read(request.userId) 
                    ?: return@post call.responseError("Пользователь не найден", HttpStatusCode.NotFound)
                
                if (user.email == "sa@example.com") {
                    return@post call.responseError("Невозможно понизить суперадминистратора", HttpStatusCode.Forbidden)
                }
                
                userService.updatePartial(request.userId, mapOf("role" to "user"))
                call.responseSuccess("Пользователю назначена роль обычного пользователя")
            }
        }
    }
}
