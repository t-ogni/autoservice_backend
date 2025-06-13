package com.ktproject.services

import com.ktproject.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class ExposedRequest(
    val id: Int,
    val userId: Int,
    val serviceId: Int,
    val time: String,
    val date: String,
    val carModel: String,
    val carBrand: String,
    val customerComment: String,
    val status: String,
    val result: String? = null
)

class RequestService(private val database: Database) {

    init {
        transaction(database) {
            SchemaUtils.create(Requests)
        }
    }

    suspend fun createFromRequest(request: AddRequestRequest): Int = dbQuery {
        Requests.insert {
            it[userId] = request.userId
            it[serviceId] = request.serviceId
            it[date] = request.date
            it[time] = request.time
            it[carModel] = request.carModel
            it[carBrand] = request.carBrand
            it[customerComment] = request.customerComment
            it[status] = request.status
            it[result] = null
        }[Requests.id]
    }

    suspend fun read(id: Int): ExposedRequest? = dbQuery {
        Requests.select(Requests.id.eq(id))
            .map { row ->
                ExposedRequest(
                    id = row[Requests.id],
                    userId = row[Requests.userId],
                    serviceId = row[Requests.serviceId],
                    date = row[Requests.date],
                    time = row[Requests.time],
                    carModel = row[Requests.carModel],
                    carBrand = row[Requests.carBrand],
                    customerComment = row[Requests.customerComment],
                    status = row[Requests.status],
                    result = row[Requests.result]
                )
            }
            .singleOrNull()
    }

    suspend fun readAll(): List<ExposedRequest> = dbQuery {
        Requests.selectAll()
            .map { row ->
                ExposedRequest(
                    id = row[Requests.id],
                    userId = row[Requests.userId],
                    serviceId = row[Requests.serviceId],
                    date = row[Requests.date],
                    time = row[Requests.time],
                    carModel = row[Requests.carModel],
                    carBrand = row[Requests.carBrand],
                    customerComment = row[Requests.customerComment],
                    status = row[Requests.status],
                    result = row[Requests.result]
                )
            }
    }

    suspend fun readByUser(userId: Int): List<ExposedRequest> = dbQuery {
        Requests.select(Requests.userId.eq(userId))
            .map { row ->
                ExposedRequest(
                    id = row[Requests.id],
                    userId = row[Requests.userId],
                    serviceId = row[Requests.serviceId],
                    date = row[Requests.date],
                    time = row[Requests.time],
                    carModel = row[Requests.carModel],
                    carBrand = row[Requests.carBrand],
                    customerComment = row[Requests.customerComment],
                    status = row[Requests.status],
                    result = row[Requests.result]
                )
            }
    }

    suspend fun updateStatus(id: Int, status: String, result: String?): Boolean = dbQuery {
        Requests.update({ Requests.id.eq(id) }) { row ->
            row[Requests.status] = status
            result?.let { row[Requests.result] = result }
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        Requests.deleteWhere(op = { Requests.id eq id }) > 0
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }
}
