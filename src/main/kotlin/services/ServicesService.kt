package com.ktproject.services

import com.ktproject.models.AddServiceRequest
import com.ktproject.models.Services
import com.ktproject.models.UpdateServiceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class ExposedService(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String
)

class ServicesService(private val database: Database) {

    init {
        transaction(database) {
            SchemaUtils.create(Services)
        }
    }

    suspend fun createFromRequest(serviceRequest: AddServiceRequest): Int = dbQuery {
        Services.insert {
            it[name] = serviceRequest.title
            it[description] = serviceRequest.description
            it[price] = serviceRequest.price.toDoubleOrNull() ?: 0.0
        }[Services.id]
    }

    suspend fun create(service: ExposedService): Int = dbQuery {
        Services.insert {
            it[name] = service.name
            it[price] = service.price
            it[description] = service.description
        }[Services.id]
    }

    suspend fun read(id: Int): ExposedService? = dbQuery {
        Services.select(Services.id.eq(id))
            .map { row ->
                ExposedService(
                    id = row[Services.id],
                    name = row[Services.name],
                    price = row[Services.price],
                    description = row[Services.description]
                )
            }
            .singleOrNull()
    }

    suspend fun readAll(): List<ExposedService> = dbQuery {
        Services.selectAll()
            .map { row ->
                ExposedService(
                    id = row[Services.id],
                    name = row[Services.name],
                    price = row[Services.price],
                    description = row[Services.description]
                )
            }
    }

    suspend fun update(id: Int, updates: UpdateServiceRequest): Boolean = dbQuery {
        val service = read(id) ?: return@dbQuery false
        
        Services.update({ Services.id.eq(id) }) { row ->
            updates.title?.let { row[Services.name] = it }
            updates.description?.let { row[Services.description] = it }
            updates.price?.let { row[Services.price] = it.toDoubleOrNull() ?: service.price }
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        Services.deleteWhere(op = { Services.id eq id }) > 0
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }
}
