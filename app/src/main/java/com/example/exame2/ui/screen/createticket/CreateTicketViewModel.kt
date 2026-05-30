package com.example.exame2.ui.screen.createticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exame2.core.events.TicketEvent
import com.example.exame2.core.events.TicketEventBus
import com.example.exame2.core.flags.FeatureFlags
import com.example.exame2.core.state.UiState
import com.example.exame2.data.repository.TicketRepository
import com.example.exame2.domain.model.Ticket
import com.example.exame2.domain.model.TicketCategory
import com.example.exame2.domain.model.TicketPriority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateTicketViewModel : ViewModel() {

    private val repository = TicketRepository()

    private val _createState = MutableStateFlow<UiState<Ticket>?>(null)
    val createState: StateFlow<UiState<Ticket>?> = _createState.asStateFlow()

    fun createTicket(
        title: String,
        description: String,
        priority: TicketPriority,
        category: TicketCategory,
        supplier: String
    ) {
        if (!FeatureFlags.isTicketCreationEnabled) {
            _createState.value = UiState.Error("Ticket creation is currently disabled")
            return
        }
        viewModelScope.launch {
            _createState.value = UiState.Loading
            repository.createTicket(title, description, priority, category, supplier)
                .onSuccess { ticket ->
                    _createState.value = UiState.Success(ticket)
                    TicketEventBus.publish(TicketEvent.TicketCreated(ticket))
                }
                .onFailure {
                    _createState.value = UiState.Error(it.message ?: "Failed to create ticket")
                }
        }
    }

    fun clearState() {
        _createState.value = null
    }
}
