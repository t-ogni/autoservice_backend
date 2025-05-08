package com.ktproject.services

import com.ktproject.models.AddNewsRequest
import com.ktproject.models.News
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class ExposedNews(
    val id: Int,
    val title: String,
    val content: String,
    val date: String
)

class NewsService(private val database: Database) {

    init {
        transaction(database) {
            SchemaUtils.create(News)
        }
    }

    suspend fun createFromRequest(news: AddNewsRequest): Int = dbQuery {
        News.insert {
            it[title] = news.title
            it[content] = news.content
            it[date] = news.date
        }[News.id]
    }

    suspend fun read(id: Int): List<ExposedNews> = dbQuery {
        News.select(News.id.eq(id))
            .map { row ->
                ExposedNews(
                    id = row[News.id],
                    title = row[News.title],
                    content = row[News.content],
                    date = row[News.date]
                )
            }
    }

    suspend fun readAll(): List<ExposedNews> = dbQuery {
        News.selectAll()
            .orderBy(News.date, SortOrder.DESC)
            .map { row ->
                ExposedNews(
                    id = row[News.id],
                    title = row[News.title],
                    content = row[News.content],
                    date = row[News.date]
                )
            }
    }

    suspend fun updateNews(id: Int, title: String, content: String, date: String): Boolean = dbQuery {
        News.update({ News.id.eq(id) }) { row ->
            row[News.title] = title
            row[News.content] = content
            row[News.date] = date
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        News.deleteWhere(op = { News.id eq id }) > 0
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }
}