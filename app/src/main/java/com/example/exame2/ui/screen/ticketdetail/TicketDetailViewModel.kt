package com.example.exame2.ui.screen.ticketdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exame2.core.events.TicketEvent
import com.example.exame2.core.events.TicketEventBus
import com.example.exame2.core.flags.FeatureFlags
import com.example.exame2.core.state.UiState
import com.example.exame2.data.repository.TicketRepository
import com.example.exame2.domain.model.Ticket
import com.example.exame2.domain.model.TicketPriority
import com.example.exame2.domain.model.TicketStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TicketDetailViewModel : ViewModel() {

    private val repository = TicketRepository()

    private val _uiState = MutableStateFlow<UiState<Ticket>>(UiState.Loading)
    val uiState: StateFlow<UiState<Ticket>> = _uiState.asStateFlow()

    fun loadTicket(ticketId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getTicketById(ticketId)
                .onSuccess { _uiState.value = UiState.Success(it) }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Failed to load ticket") }
        }
    }

    fun updateStatus(ticketId: String, newStatus: TicketStatus) {
        viewModelScope.launch {
            repository.updateTicketStatus(ticketId, newStatus)
                .onSuccess { updatedTicket ->
                    _uiState.value = UiState.Success(updatedTicket)
                    TicketEventBus.publish(TicketEvent.StatusUpdated(ticketId, newStatus))
                }
                .onFailure {
                    _uiState.value = UiState.Error(it.message ?: "Failed to update status")
                }
        }
    }

    fun updatePriority(ticketId: String, newPriority: TicketPriority) {
        if (!FeatureFlags.isPriorityUpdateEnabled) {
            _uiState.value = UiState.Error("Priority update is currently disabled")
            return
        }
        viewModelScope.launch {
            repository.updateTicketPriority(ticketId, newPriority)
                .onSuccess { updatedTicket ->
                    _uiState.value = UiState.Success(updatedTicket)
                    TicketEventBus.publish(TicketEvent.PriorityUpdated(ticketId, newPriority))
                }
                .onFailure {
                    _uiState.value = UiState.Error(it.message ?: "Failed to update priority")
                }
        }
    }
}
