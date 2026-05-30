package com.example.exame2.data.repository

import com.example.exame2.data.dto.CreateTicketRequestDto
import com.example.exame2.data.dto.UpdatePriorityRequestDto
import com.example.exame2.data.dto.UpdateStatusRequestDto
import com.example.exame2.data.dto.toDomain
import com.example.exame2.data.remote.NetworkModule
import com.example.exame2.domain.model.Ticket
import com.example.exame2.domain.model.TicketCategory
import com.example.exame2.domain.model.TicketPriority
import com.example.exame2.domain.model.TicketStatus

class TicketRepository {

    private val api = NetworkModule.ticketApiService

    suspend fun getTickets(): Result<List<Ticket>> = runCatching {
        val response = api.getTickets()
        if (response.isSuccessful) {
            response.body()?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception("Error ${response.code()}: failed to load tickets")
        }
    }

    suspend fun getTicketById(id: String): Result<Ticket> = runCatching {
        val response = api.getTicketById(id)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Ticket not found")
        } else {
            throw Exception("Error ${response.code()}: failed to load ticket")
        }
    }

    suspend fun createTicket(
        title: String,
        description: String,
        priority: TicketPriority,
        category: TicketCategory,
        supplier: String
    ): Result<Ticket> = runCatching {
        val request = CreateTicketRequestDto(
            title = title,
            description = description,
            priority = priority.name,
            category = category.name,
            supplier = supplier
        )
        val response = api.createTicket(request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Invalid response from server")
        } else {
            throw Exception("Error ${response.code()}: failed to create ticket")
        }
    }

    suspend fun updateTicketStatus(ticketId: String, newStatus: TicketStatus): Result<Ticket> = runCatching {
        val request = UpdateStatusRequestDto(status = newStatus.name)
        val response = api.updateTicketStatus(ticketId, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Invalid response from server")
        } else {
            throw Exception("Error ${response.code()}: failed to update status")
        }
    }

    suspend fun updateTicketPriority(ticketId: String, newPriority: TicketPriority): Result<Ticket> = runCatching {
        val request = UpdatePriorityRequestDto(priority = newPriority.name)
        val response = api.updateTicketPriority(ticketId, request)
        if (response.isSuccessful) {
            response.body()?.toDomain() ?: throw Exception("Invalid response from server")
        } else {
            throw Exception("Error ${response.code()}: failed to update priority")
        }
    }
}
