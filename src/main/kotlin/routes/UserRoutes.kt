package com.ktproject.routes

import com.ktproject.models.AddUserRequest
import com.ktproject.models.UpdateUserRequest
import com.ktproject.models.toMap
import com.ktproject.services.UserService
import com.ktproject.services.ExposedUserDTO
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import models.responseError
import models.responseSuccess

fun Application.configureUserRoutes(userService: UserService) {
    routing {
        authenticate {
            get("/profile") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
                    ?: return@get call.responseError("Не авторизован", HttpStatusCode.Unauthorized)

                val response = userService.read(userId)

                System.out.println("DEBUG: Отправляем user: $response")
                call.responseSuccess(response)
            }

            get("/users") {
                val users = userService.readAll()
                val usersDto: List<ExposedUserDTO> = users.map { row ->
                    ExposedUserDTO(
                        id = row.id,
                        name = row.name,
                        email = row.email,
                        phone = row.phone,
                        role = row.role
                    )
                }
                call.responseSuccess(usersDto)
            }

            get("/users/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() 
                    ?: return@get call.responseError("Некорректный ID", HttpStatusCode.BadRequest)
                
                val user = userService.read(id)
                    ?: return@get call.responseError("Пользователь не найден", HttpStatusCode.NotFound)
                
                call.responseSuccess(user)
            }

            put("/users/{id}") {
                val principal = call.principal<JWTPrincipal>() 
                    ?: return@put call.responseError("Не авторизован", HttpStatusCode.Unauthorized)
                
                val userIdFromToken = principal.payload.getClaim("id").asInt()
                val userRole = principal.payload.getClaim("role").asString()
                val idParam = call.parameters["id"]?.toIntOrNull() 
                    ?: return@put call.responseError("Некорректный ID", HttpStatusCode.BadRequest)

                // Проверка прав: админ может менять любой аккаунт, пользователь - только свой
                if (userRole != "admin" && userIdFromToken != idParam) {
                    return@put call.responseError("Вы можете изменять только свой аккаунт", HttpStatusCode.Forbidden)
                }

                val updateReq = call.receive<UpdateUserRequest>()
                userService.updatePartial(idParam, updateReq.toMap())
                call.responseSuccess("Пользователь обновлен")
            }

            delete("/users/{id}") {
                val principal = call.principal<JWTPrincipal>() 
                    ?: return@delete call.responseError("Не авторизован", HttpStatusCode.Unauthorized)
                
                val userIdFromToken = principal.payload.getClaim("id").asInt()
                val userRole = principal.payload.getClaim("role").asString()
                val idParam = call.parameters["id"]?.toIntOrNull() 
                    ?: return@delete call.responseError("Некорректный ID", HttpStatusCode.BadRequest)

                // Проверка прав: админ может удалять любой аккаунт, пользователь - только свой
                if (userRole != "admin" && userIdFromToken != idParam) {
                    return@delete call.responseError("Вы можете удалять только свой аккаунт", HttpStatusCode.Forbidden)
                }
                
                // Проверка: нельзя удалить суперадмина
                val user = userService.read(idParam) 
                    ?: return@delete call.responseError("Пользователь не найден", HttpStatusCode.NotFound)
                
                if (user.email == "sa@example.com") {
                    return@delete call.responseError("Невозможно удалить суперадминистратора", HttpStatusCode.Forbidden)
                }
                
                userService.delete(idParam)
                call.responseSuccess("Пользователь удален")
            }
        }
        
        authenticate("admin") {
            post("/users") {
                val request = call.receive<AddUserRequest>()
                
                // Проверка на существование пользователя с таким email
                val existingUsers = userService.readAll()
                val emailExists = existingUsers.any { it.email == request.email }
                
                if (emailExists) {
                    return@post call.responseError("Пользователь с таким email уже существует", HttpStatusCode.Conflict)
                }
                
                val hashedPassword = hashPassword(request.password)
                val newUser = com.ktproject.services.ExposedUser(
                    id = 0,
                    name = request.name,
                    email = request.email,
                    passwordHash = hashedPassword,
                    phone = request.phone,
                    role = request.role
                )
                
                val id = userService.create(newUser)
                call.responseSuccess(id, HttpStatusCode.Created)
            }
        }
    }
}
