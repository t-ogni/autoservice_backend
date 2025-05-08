package com.ktproject.routes

import com.ktproject.models.AddServiceRequest
import com.ktproject.models.UpdateServiceRequest
import com.ktproject.models.toMap
import com.ktproject.services.ServicesService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import models.responseError
import models.responseSuccess

fun Application.configureServiceRoutes(serviceService: ServicesService) {
    routing {
        // Публичный доступ к списку услуг
        get("/services") {
            val services = serviceService.readAll()
            call.responseSuccess(services)
        }

        // Получение одной услуги по ID
        get("/services/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.responseError("Некорректный ID", HttpStatusCode.BadRequest)

            val service = serviceService.read(id)
                ?: return@get call.responseError("Услуга не найдена", HttpStatusCode.NotFound)

            call.responseSuccess(service)
        }

        // Маршруты только для администраторов
        authenticate("admin") {
            // Создание новой услуги
            post("/services") {
                val request = call.receive<AddServiceRequest>()
                val id = serviceService.createFromRequest(request)
                call.responseSuccess(id, HttpStatusCode.Created)
            }

            // Обновление существующей услуги
            put("/services/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.responseError("Некорректный ID", HttpStatusCode.BadRequest)

                val request = call.receive<UpdateServiceRequest>()

                if (serviceService.update(id, request)) {
                    call.responseSuccess("Услуга обновлена")
                } else {
                    call.responseError("Услуга не найдена", HttpStatusCode.NotFound)
                }
            }

            // Удаление услуги
            delete("/services/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.responseError("Некорректный ID", HttpStatusCode.BadRequest)

                if (serviceService.delete(id)) {
                    call.responseSuccess("Услуга удалена")
                } else {
                    call.responseError("Услуга не найдена", HttpStatusCode.NotFound)
                }
            }
        }
    }
}