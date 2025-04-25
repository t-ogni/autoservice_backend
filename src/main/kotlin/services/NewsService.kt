package com.ktproject.services

import com.ktproject.models.News
import com.ktproject.models.Requests
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
data class ExposedNews(
    val id: Int,  // Добавим id
    val title: String,
    val content: String,
    val date: String
)

class NewsService(database: Database) {

    init {
        transaction(database) {
            SchemaUtils.create(News) // создаем таблицу для новостей
        }
    }

    suspend fun create(news: ExposedNews): Int = dbQuery {
        News.insert {
            it[title] = news.title
            it[content] = news.content
            it[date] = news.date
        }[News.id]
    }
    suspend fun read(newsId: Int): List<ExposedNews> {
        return dbQuery {
            News.selectAll().where { News.id eq newsId }
                .map {
                    ExposedNews(
                        id = it[News.id],
                        it[News.title],
                        it[News.content],
                        it[News.date]
                    )
                }
        }
    }
    suspend fun readAll(): List<ExposedNews> {
        return dbQuery {
            News.selectAll()
                .map {
                    ExposedNews(
                        id = it[News.id],
                        it[News.title],
                        it[News.content],
                        it[News.date]
                    )
                }
        }
    }

    suspend fun updateNews(id: Int, title: String, content: String, date: String) {
        dbQuery {
            News.update({ News.id eq id }) {
                it[this.title] = title
                it[this.content] = content
                it[this.date] = date
            }
        }
    }


    suspend fun delete(id: Int) {
        dbQuery {
            News.deleteWhere { News.id.eq(id) }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
