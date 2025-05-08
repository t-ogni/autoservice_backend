package com.ktproject.routes

import com.ktproject.models.AddRequestRequest
import com.ktproject.models.UpdateRequestStatusRequest
import com.ktproject.services.RequestService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import models.responseError
import models.responseSuccess

fun Application.configureRequestRoutes(requestService: RequestService) {
    routing {
        // Маршруты для авторизованных пользователей
        authenticate {
            // Создание новой заявки
            post("/requests") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
                    ?: return@post call.responseError("Не авторизован", HttpStatusCode.Unauthorized)
                
                val request = call.receive<AddRequestRequest>()
                val id = requestService.createFromRequest(request.copy(userId = userId))
                call.responseSuccess(id, HttpStatusCode.Created)
            }
            
            // Получение списка своих заявок
            get("/my_requests") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
                    ?: return@get call.responseError("Не авторизован", HttpStatusCode.Unauthorized)
                
                val requests = requestService.readByUser(userId)
                call.responseSuccess(requests)
            }
        }
        
        // Маршруты только для администраторов
        authenticate("admin") {
            // Получение всех заявок
            get("/requests") {
                val requests = requestService.readAll()
                call.responseSuccess(requests)
            }
            
            // Обновление статуса заявки
            put("/requests/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() 
                    ?: return@put call.responseError("Некорректный ID", HttpStatusCode.BadRequest)
                
                val request = call.receive<UpdateRequestStatusRequest>()
                
                if (requestService.updateStatus(id, request.status, request.result)) {
                    call.responseSuccess("Заявка обновлена")
                } else {
                    call.responseError("Заявка не найдена", HttpStatusCode.NotFound)
                }
            }
            
            // Удаление заявки
            delete("/requests/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() 
                    ?: return@delete call.responseError("Некорректный ID", HttpStatusCode.BadRequest)
                
                if (requestService.delete(id)) {
                    call.responseSuccess("Заявка удалена")
                } else {
                    call.responseError("Заявка не найдена", HttpStatusCode.NotFound)
                }
            }
        }
    }
}
