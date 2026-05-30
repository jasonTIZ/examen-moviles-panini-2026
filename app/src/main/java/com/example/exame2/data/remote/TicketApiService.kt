package com.example.exame2.data.remote

import com.example.exame2.data.dto.CreateTicketRequestDto
import com.example.exame2.data.dto.LoginRequestDto
import com.example.exame2.data.dto.LoginResponseDto
import com.example.exame2.data.dto.TicketDto
import com.example.exame2.data.dto.UpdatePriorityRequestDto
import com.example.exame2.data.dto.UpdateStatusRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface TicketApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @GET("tickets")
    suspend fun getTickets(): Response<List<TicketDto>>

    @GET("tickets/{id}")
    suspend fun getTicketById(@Path("id") id: String): Response<TicketDto>

    @POST("tickets")
    suspend fun createTicket(@Body request: CreateTicketRequestDto): Response<TicketDto>

    @PATCH("tickets/{id}/status")
    suspend fun updateTicketStatus(
        @Path("id") id: String,
        @Body request: UpdateStatusRequestDto
    ): Response<TicketDto>

    @PATCH("tickets/{id}/priority")
    suspend fun updateTicketPriority(
        @Path("id") id: String,
        @Body request: UpdatePriorityRequestDto
    ): Response<TicketDto>
}
