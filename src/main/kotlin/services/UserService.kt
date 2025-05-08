package com.ktproject.services

import com.ktproject.models.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class ExposedUser(
    val id: Int,
    val name: String,
    val email: String,
    val passwordHash: String,
    val phone: String = "",
    val role: String = "user"
)

class UserService(private val database: Database) {

    init {
        transaction(database) {
            SchemaUtils.create(Users)

            // Создаем суперадминистратора, если он не существует
            val adminExists = Users.select(Users.email.eq("sa@example.com")).count() > 0
            if (!adminExists) {
                Users.insert {
                    it[name] = "Администратор"
                    it[email] = "sa@example.com"
                    it[passwordHash] = com.ktproject.routes.hashPassword("admin123")
                    it[role] = "admin"
                }
            }
        }
    }

    suspend fun create(user: ExposedUser): Int = dbQuery {
        Users.insert {
            it[name] = user.name
            it[email] = user.email
            it[passwordHash] = user.passwordHash
            it[phone] = user.phone
            it[role] = user.role
        }[Users.id]
    }

    suspend fun read(id: Int): ExposedUser? = dbQuery {
        Users.select(Users.id.eq(id))
            .map { row ->
                ExposedUser(
                    id = row[Users.id],
                    name = row[Users.name],
                    email = row[Users.email],
                    passwordHash = row[Users.passwordHash],
                    phone = row[Users.phone],
                    role = row[Users.role]
                )
            }
            .singleOrNull()
    }

    suspend fun readAll(): List<ExposedUser> = dbQuery {
        Users.selectAll()
            .map { row ->
                ExposedUser(
                    id = row[Users.id],
                    name = row[Users.name],
                    email = row[Users.email],
                    passwordHash = row[Users.passwordHash],
                    phone = row[Users.phone],
                    role = row[Users.role]
                )
            }
    }

    suspend fun update(id: Int, user: ExposedUser): Boolean = dbQuery {
        Users.update({ Users.id.eq(id) }) { row ->
            row[name] = user.name
            row[email] = user.email
            row[passwordHash] = user.passwordHash
            row[phone] = user.phone
            row[role] = user.role
        } > 0
    }

    suspend fun updatePartial(id: Int, updates: Map<String, Any>): Boolean = dbQuery {
        if (updates.isEmpty()) return@dbQuery false

        val statement = Users.update({ Users.id.eq(id) }) { stmt ->
            updates.forEach { (key, value) ->
                when (key) {
                    "name" -> stmt[Users.name] = value as String
                    "email" -> stmt[Users.email] = value as String
                    "passwordHash" -> stmt[Users.passwordHash] = value as String
                    "phone" -> stmt[Users.phone] = value as String
                    "role" -> stmt[Users.role] = value as String
                }
            }
        }

        statement > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        Users.deleteWhere(op =  { Users.id eq id }) > 0
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }
}