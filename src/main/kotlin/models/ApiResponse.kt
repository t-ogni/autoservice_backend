package models


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable


@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

// Extension function for success response
suspend fun ApplicationCall.responseSuccess() {
    respond(ApiResponse(success = true, data = Unit))
}


suspend inline fun <reified T> ApplicationCall.responseSuccess(
    data: T,
    status: HttpStatusCode = HttpStatusCode.OK
) {
    respond(status, ApiResponse(success = true, data = data))
}


suspend fun ApplicationCall.responseError(
    message: String,
    status: HttpStatusCode = HttpStatusCode.BadRequest
) {
    respond(status, ApiResponse<Unit>(success = false, error = message))
}
