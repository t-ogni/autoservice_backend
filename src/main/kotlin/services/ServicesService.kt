package com.ktproject.services

import com.ktproject.models.Services
import com.ktproject.models.Users
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
data class ExposedService(
    val id: Int,  // Добавим id
    val name: String,
    val price: Double,
    val description: String
)

class ServicesService(database: Database) {

    init {
        transaction(database) {
            SchemaUtils.create(Services) // создаем таблицу для услуг
        }
    }

    suspend fun create(service: ExposedService): Int = dbQuery {
        Services.insert {
            it[name] = service.name
            it[price] = service.price
            it[description] = service.description
        }[Services.id]
    }

    suspend fun readAll(): List<ExposedService> {
        return dbQuery {
            Services.selectAll()
                .map { ExposedService(
                    it[Services.id],
                    it[Services.name],
                    it[Services.price],
                    it[Services.description]
                )}
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Services.deleteWhere { Services.id.eq(id) }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
