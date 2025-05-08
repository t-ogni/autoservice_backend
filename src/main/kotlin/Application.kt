package com.ktproject

import com.ktproject.routes.*
import com.ktproject.services.NewsService
import com.ktproject.services.RequestService
import com.ktproject.services.ServicesService
import com.ktproject.services.UserService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {

//    // Настройка CORS
//    install(CORS) {
//        allowMethod(HttpMethod.Options)
//        allowMethod(HttpMethod.Put)
//        allowMethod(HttpMethod.Delete)
//        allowMethod(HttpMethod.Patch)
//        allowMethod(HttpMethod.Post)
//        allowMethod(HttpMethod.Get)
//        allowHeader(HttpHeaders.Authorization)
//        allowHeader(HttpHeaders.ContentType)
//        allowHeader(HttpHeaders.Accept)
//        anyHost()
//    }

    val database = configureDatabases()

    // Конфигурация безопасности
    configureSecurity()
    configureSerialization()


    // Инициализация сервисов
    val userService = UserService(database)
    val serviceService = ServicesService(database)
    val requestService = RequestService(database)
    val newsService = NewsService(database)
    
    // Конфигурация маршрутов
    configureAuthRoutes(database, userService)
    configureUserRoutes(userService)
    configureServiceRoutes(serviceService)
    configureRequestRoutes(requestService)
    configureNewsRoutes(newsService)
    configureAdminRoutes(userService)
}
