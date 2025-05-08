package com.ktproject.routes

import com.ktproject.models.AddNewsRequest
import com.ktproject.models.UpdateNewsRequest
import com.ktproject.services.NewsService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import models.responseError
import models.responseSuccess

fun Application.configureNewsRoutes(newsService: NewsService) {
    routing {
        // Публичный доступ к новостям
        get("/news") {
            val news = newsService.readAll()
            call.responseSuccess(news)
        }
        
        get("/news/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() 
                ?: return@get call.responseError("Некорректный ID", HttpStatusCode.BadRequest)
            
            val news = newsService.read(id)
            if (news.isNotEmpty()) {
                call.responseSuccess(news.first())
            } else {
                call.responseError("Новость не найдена", HttpStatusCode.NotFound)
            }
        }
        
        // Маршруты только для администраторов
        authenticate("admin") {
            post("/news") {
                val request = call.receive<AddNewsRequest>()
                val id = newsService.createFromRequest(request)
                call.responseSuccess(id, HttpStatusCode.Created)
            }
            
            put("/news/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() 
                    ?: return@put call.responseError("Некорректный ID", HttpStatusCode.BadRequest)
                
                val request = call.receive<UpdateNewsRequest>()
                newsService.updateNews(id, request.title, request.content, request.date)
                call.responseSuccess("Новость обновлена")
            }
            
            delete("/news/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() 
                    ?: return@delete call.responseError("Некорректный ID", HttpStatusCode.BadRequest)
                
                newsService.delete(id)
                call.responseSuccess("Новость удалена")
            }
        }
    }
}
