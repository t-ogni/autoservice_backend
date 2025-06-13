package com.ktproject.models

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    val email = varchar("email", 50).uniqueIndex()
    val passwordHash = varchar("passwordHash", 128) // Увеличиваем длину для хеша BCrypt
    val phone = varchar("phone", 20).default("")
    val role = varchar("role", 10).default("user") // user / admin

    override val primaryKey = PrimaryKey(id)
}

object Services : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val description = text("description")
    val price = varchar("price", 255)

    override val primaryKey = PrimaryKey(id)
}

object Requests : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val serviceId = integer("service_id").references(Services.id, onDelete = ReferenceOption.CASCADE)
    val date = varchar("date", 20) // Формат YYYY-MM-DD
    val time = varchar("time", 20)
    val carBrand = varchar("car_brand", 255)
    val carModel = varchar("car_model", 255)
    val customerComment = text("customer_comment")
    val status = varchar("status", 50).default("wait")
    val result = text("result").nullable()

    override val primaryKey = PrimaryKey(id)
}

object News : Table() {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val content = text("content")
    val date = varchar("date", 20) // Формат YYYY-MM-DD

    override val primaryKey = PrimaryKey(id)
}
