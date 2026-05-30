package com.example.exame2.ui.screen.ticketlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exame2.core.events.TicketEvent
import com.example.exame2.core.events.TicketEventBus
import com.example.exame2.core.state.UiState
import com.example.exame2.data.repository.TicketRepository
import com.example.exame2.domain.model.Ticket
import com.example.exame2.domain.model.TicketPriority
import com.example.exame2.domain.model.TicketStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TicketListViewModel : ViewModel() {

    private val repository = TicketRepository()

    private val _uiState = MutableStateFlow<UiState<List<Ticket>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Ticket>>> = _uiState.asStateFlow()

    init {
        loadTickets()
        observeEvents()
    }

    fun loadTickets() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getTickets()
                .onSuccess { tickets ->
                    _uiState.value = UiState.Success(tickets.sortedBy { it.priority.order })
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load tickets")
                }
        }
    }

    // Subscribes to the event bus to react to ticket changes without manual screen refresh
    private fun observeEvents() {
        viewModelScope.launch {
            TicketEventBus.events.collect { event ->
                when (event) {
                    is TicketEvent.TicketCreated -> addTicketAndSort(event.ticket)
                    is TicketEvent.PriorityUpdated -> updatePriorityAndSort(event.ticketId, event.newPriority)
                    is TicketEvent.StatusUpdated -> updateStatus(event.ticketId, event.newStatus)
                }
            }
        }
    }

    private fun addTicketAndSort(ticket: Ticket) {
        val current = _uiState.value
        if (current is UiState.Success) {
            val updated = (current.data + ticket).sortedBy { it.priority.order }
            _uiState.value = UiState.Success(updated)
        }
    }

    private fun updatePriorityAndSort(ticketId: String, newPriority: TicketPriority) {
        val current = _uiState.value
        if (current is UiState.Success) {
            val updated = current.data
                .map { if (it.id == ticketId) it.copy(priority = newPriority) else it }
                .sortedBy { it.priority.order }
            _uiState.value = UiState.Success(updated)
        }
    }

    private fun updateStatus(ticketId: String, newStatus: TicketStatus) {
        val current = _uiState.value
        if (current is UiState.Success) {
            val updated = current.data.map { if (it.id == ticketId) it.copy(status = newStatus) else it }
            _uiState.value = UiState.Success(updated)
        }
    }
}
