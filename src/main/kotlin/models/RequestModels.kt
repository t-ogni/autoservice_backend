package com.ktproject.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String = ""
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AddUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String = "",
    val role: String = "user"
)

@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val phone: String? = null,
    val role: String? = null
)

@Serializable
data class AddServiceRequest(
    val title: String,
    val description: String,
    val price: String
)

@Serializable
data class UpdateServiceRequest(
    val title: String? = null,
    val description: String? = null,
    val price: String? = null
)

@Serializable
data class AddRequestRequest(
    val serviceId: Int,
    val date: String,
    val carModel: String,
    val carBrand: String,
    val customerComment: String,
    val status: String = "новая",
    val userId: Int = 0
)

@Serializable
data class UpdateRequestStatusRequest(
    val status: String,
    val result: String? = null
)

@Serializable
data class AddNewsRequest(
    val title: String,
    val content: String,
    val date: String
)

@Serializable
data class UpdateNewsRequest(
    val title: String,
    val content: String,
    val date: String
)

// Вспомогательная функция для преобразования в Map для частичных обновлений
fun UpdateUserRequest.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    name?.let { map["name"] = it }
    email?.let { map["email"] = it }
    password?.let { map["password"] = it }
    phone?.let { map["phone"] = it }
    role?.let { map["role"] = it }
    return map
}

fun UpdateServiceRequest.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    title?.let { map["name"] = it }
    description?.let { map["description"] = it }
    price?.let { map["price"] = it }
    return map
}
