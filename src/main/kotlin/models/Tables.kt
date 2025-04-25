package com.ktproject.models

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    val email = varchar("email", 50).uniqueIndex()
    val passwordHash = varchar("password", 64)
    val phone = varchar("phone", 20).default("")
    val role = varchar("role", 10).default("user") // user / admin

    override val primaryKey = PrimaryKey(id)
}

object Services : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val price = double("price")
    val description = text("description")

    override val primaryKey = PrimaryKey(Users.id)
}

object Requests : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val serviceId = integer("service_id").references(Services.id)
    val date = varchar("date", 255)
    val carBrand = varchar("car_brand", 255)
    val customerComment = text("customer_comment")
    val status = varchar("status", 255)
    val result = text("result").nullable()

    override val primaryKey = PrimaryKey(Users.id)
}

object News : Table() {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val content = text("content")
    val date = varchar("date", 255)

    override val primaryKey = PrimaryKey(Users.id)
}
