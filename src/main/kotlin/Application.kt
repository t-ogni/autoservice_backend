package com.ktproject

import com.ktproject.routes.configureAuthRoutes
import com.ktproject.routes.configureRoutes
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureSerialization()
    val database: Database = configureDatabases()
    configureRoutes(database)
    configureAuthRoutes()
}
