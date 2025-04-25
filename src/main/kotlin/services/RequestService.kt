package com.ktproject.services

import com.ktproject.models.Requests
import com.ktproject.models.Services
import com.ktproject.models.Users
import com.ktproject.models.Users.email
import com.ktproject.models.Users.passwordHash
import com.ktproject.models.Users.phone
import com.ktproject.models.Users.role
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

@Serializable
data class ExposedRequest(
    val id: Int,  // Добавим id
    val userId: Int,
    val serviceId: Int,
    val date: String, // Для простоты, допустим, что дата передается как строка
    val carBrand: String,
    val customerComment: String,
    val status: String,
    val result: String? = null
)

class RequestService(database: Database) {

    init {
        transaction(database) {
            SchemaUtils.create(Requests) // создаем таблицу для заявок
        }
    }

    suspend fun create(request: ExposedRequest): Int = dbQuery {
        Requests.insert {
            it[userId] = request.userId
            it[serviceId] = request.serviceId
            it[date] = request.date
            it[carBrand] = request.carBrand
            it[customerComment] = request.customerComment
            it[status] = request.status
            it[result] = request.result
        }[Requests.id]
    }

    suspend fun readByUser(userId: Int): List<ExposedRequest> {
        return dbQuery {
            Requests.selectAll().where { Requests.userId eq userId }
                .map {
                    ExposedRequest(
                        it[Requests.id],
                        it[Requests.userId],
                        it[Requests.serviceId],
                        it[Requests.date],
                        it[Requests.carBrand],
                        it[Requests.customerComment],
                        it[Requests.status],
                        it[Requests.result]
                    )
                }
        }
    }

    suspend fun readAll(): List<ExposedRequest> {
        return dbQuery {
            Requests.selectAll()
                .map {
                    ExposedRequest(
                        it[Requests.id],
                        it[Requests.userId],
                        it[Requests.serviceId],
                        it[Requests.date],
                        it[Requests.carBrand],
                        it[Requests.customerComment],
                        it[Requests.status],
                        it[Requests.result]
                    )
                }
        }
    }

    suspend fun updateStatus(id: Int, status: String, result: String?) {
        dbQuery {
            Requests.update({ Requests.id eq id }) {
                it[this.status] = status
                it[this.result] = result
            }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
