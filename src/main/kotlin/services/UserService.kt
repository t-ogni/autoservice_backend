package com.ktproject.services

import com.ktproject.models.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

@Serializable
data class ExposedUser(
    val id: Int,  // Добавим id
    val name: String,
    val email: String,
    val passwordHash: String,
    val phone: String,
    val role: String,
)

class UserService(database: Database) {


    init {
        transaction(database) {
            SchemaUtils.create(Users)
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

    suspend fun read(id: Int): ExposedUser? {
        return dbQuery {
            Users.selectAll()
                .where { Users.id eq id }
                .map { ExposedUser(
                    it[Users.id],
                    it[Users.name],
                    it[Users.email],
                    it[Users.passwordHash],
                    it[Users.phone],
                    it[Users.role],
                    ) }
                .singleOrNull()
        }
    }

    suspend fun readAll(): List<ExposedUser> {
        return dbQuery {
            Users.selectAll()
                .map { ExposedUser(
                    it[Users.id],
                    it[Users.name],
                    it[Users.email],
                    it[Users.passwordHash],
                    it[Users.phone],
                    it[Users.role],
                ) }
        }
    }

    suspend fun update(id: Int, user: ExposedUser) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[name] = user.name
                it[email] = user.email
                it[passwordHash] = user.passwordHash
                it[phone] = user.phone
                it[role] = user.role
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }

    suspend fun updatePartial(id: Int, updates: Map<String, Any>) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                updates.forEach { (key, value) ->
                    when (key) {
                        "name" -> if (value is String) it[name] = value
                        "email" -> if (value is String) it[email] = value
                        "password" -> if (value is String) it[passwordHash] = value
                        "phone" -> if (value is String) it[phone] = value
                        "role" -> if (value is String) it[role] = value
                        // Handle other fields if necessary
                    }
                }
            }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
