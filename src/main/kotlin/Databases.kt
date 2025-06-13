package com.ktproject

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ktproject.models.News
import com.ktproject.models.Requests
import com.ktproject.models.Services
import com.ktproject.models.Users
import com.ktproject.services.ExposedUser
import com.ktproject.services.UserService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDatabases(): Database {
    val database = Database.connect( // &createDatabaseIfNotExist=true &createDatabaseIfNotExist=true&autoReconnect=true
        url = "jdbc:mariadb://0.0.0.0:3306/ktproject?useSSL=false&serverTimezone=UTC",
        driver = "org.mariadb.jdbc.Driver",
        user = "ktproj",
        password = "passwd\$kT"
    )
//    url = "jdbc:mysql://localhost:3306/ktproject?useSSL=false&serverTimezone=UTC",
//    driver = "com.mysql.cj.jdbc.Driver",

    transaction {
        addLogger(StdOutSqlLogger)
//        SchemaUtils.drop(Requests, News, Services, Users)
    }

    return database
}
