package com.example.exame2.data.dto

import com.example.exame2.domain.model.Ticket
import com.example.exame2.domain.model.TicketCategory
import com.example.exame2.domain.model.TicketPriority
import com.example.exame2.domain.model.TicketStatus
import com.google.gson.annotations.SerializedName

data class TicketDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("priority") val priority: String,
    @SerializedName("status") val status: String,
    @SerializedName("category") val category: String,
    @SerializedName("supplier") val supplier: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("reported_by") val reportedBy: String
)

fun TicketDto.toDomain(): Ticket = Ticket(
    id = id,
    title = title,
    description = description,
    priority = runCatching { TicketPriority.valueOf(priority) }.getOrDefault(TicketPriority.MEDIUM),
    status = runCatching { TicketStatus.valueOf(status) }.getOrDefault(TicketStatus.OPEN),
    category = runCatching { TicketCategory.valueOf(category) }.getOrDefault(TicketCategory.SUPPLIER),
    supplier = supplier,
    createdAt = createdAt,
    reportedBy = reportedBy
)

data class CreateTicketRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("priority") val priority: String,
    @SerializedName("category") val category: String,
    @SerializedName("supplier") val supplier: String
)

data class UpdateStatusRequestDto(
    @SerializedName("status") val status: String
)

data class UpdatePriorityRequestDto(
    @SerializedName("priority") val priority: String
)

data class LoginRequestDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class LoginResponseDto(
    @SerializedName("token") val token: String,
    @SerializedName("username") val username: String,
    @SerializedName("role") val role: String
)
