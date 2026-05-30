package com.example.exame2.core.events

import com.example.exame2.domain.model.Ticket
import com.example.exame2.domain.model.TicketPriority
import com.example.exame2.domain.model.TicketStatus

sealed class TicketEvent {
    data class TicketCreated(val ticket: Ticket) : TicketEvent()
    data class PriorityUpdated(val ticketId: String, val newPriority: TicketPriority) : TicketEvent()
    data class StatusUpdated(val ticketId: String, val newStatus: TicketStatus) : TicketEvent()
}
