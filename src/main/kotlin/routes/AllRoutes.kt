package com.ktproject.routes

import com.ktproject.models.Users
import com.ktproject.services.ExposedNews
import com.ktproject.services.ExposedRequest
import com.ktproject.services.ExposedService
import com.ktproject.services.ExposedUser
import com.ktproject.services.NewsService
import com.ktproject.services.RequestService
import com.ktproject.services.ServicesService
import com.ktproject.services.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.UnauthorizedResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.configureRoutes(database: Database) {
    val serviceService = ServicesService(database)
    val requestService = RequestService(database)
    val newsService = NewsService(database)
    val userService = UserService(database)

    routing {

        authenticate("admin") {
            // Create user (только для админов)
            post("/users") {
                val user = call.receive<ExposedUser>()
                val id = userService.create(user)
                call.respond(HttpStatusCode.Created, id)
            }
        }

        authenticate {
            get("/users") {
                val id = userService.readAll()
                call.respond(HttpStatusCode.OK, id)
            }

            // Read user
            get("/users/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val user = userService.read(id)
                if (user != null) {
                    call.respond(HttpStatusCode.OK, user)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            put("/users/{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized, "User not authenticated")

                val userIdFromToken = principal.payload.getClaim("id").asInt()
                val userRole = principal.payload.getClaim("role").asString()

                val idParam = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")

                if (userRole != "admin" && userIdFromToken != idParam) {
                    return@put call.respond(HttpStatusCode.Forbidden, "You can only update your own account")
                }

                val fieldUpdates = call.receive<Map<String, Any>>()
                userService.updatePartial(idParam, fieldUpdates)
                call.respond(HttpStatusCode.OK)
            }

            // Delete user
            delete("/users/{id}") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized, "User not authenticated")

                val userIdFromToken = principal.payload.getClaim("id").asInt()
                val userRole = principal.payload.getClaim("role").asString()

                val idParam = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")

                if (userRole != "admin" && userIdFromToken != idParam) {
                    return@delete call.respond(HttpStatusCode.Forbidden, "You can only delete your own account")
                }
                userService.delete(idParam)
                call.respond(HttpStatusCode.OK)
            }
        }

        // Services Routes
        authenticate("admin") {
            post("/services") {
                val service = call.receive<ExposedService>()
                val id = serviceService.create(service)
                call.respond(HttpStatusCode.Created, id)
            }

            delete("/services/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                serviceService.delete(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }

        get("/services") {
            val services = serviceService.readAll()
            call.respond(services)
        }

        // Requests Routes
        authenticate {
            post("/requests") {
                val request = call.receive<ExposedRequest>()
                val id = requestService.create(request)
                call.respond(HttpStatusCode.Created, id)
            }

            get("/requests/mine") {

                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
                    ?:  call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                val requests = requestService.readByUser(userId as Int)
                call.respond(requests)
            }

            authenticate("admin") {
                get("/requests") {
                    val requests = requestService.readAll()
                    call.respond(requests)
                }

                put("/requests/{id}") {
                    val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                    val request = call.receive<ExposedRequest>()
                    requestService.updateStatus(id, request.status, request.result)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }

        // News Routes
        authenticate("admin") {
            post("/news") {
                val news = call.receive<ExposedNews>()
                val id = newsService.create(news)
                call.respond(HttpStatusCode.Created, id)
            }

            delete("/news/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                newsService.delete(id)
                call.respond(HttpStatusCode.NoContent)
            }

            put("/news/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val updatedNews = call.receive<ExposedNews>()

                newsService.updateNews(id, updatedNews.title, updatedNews.content, updatedNews.date)

                call.respond(HttpStatusCode.OK)
            }

        }

        get("/news") {
            val news = newsService.readAll()
            call.respond(news)
        }

        get("/news/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val news = newsService.read(id)
            call.respond(news)
        }
    }
}
