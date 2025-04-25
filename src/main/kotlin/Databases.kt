package com.ktproject

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
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

fun Application.configureDatabases() {
    val database = Database.connect( // &createDatabaseIfNotExist=true
        url = "jdbc:mysql://localhost:3306/ktproject?useSSL=false&serverTimezone=UTC",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "ktproj",
        password = "passwd\$kT"
    )

}
