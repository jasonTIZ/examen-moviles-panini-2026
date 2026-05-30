package com.example.exame2.core.events

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object TicketEventBus {
    private val _events = MutableSharedFlow<TicketEvent>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<TicketEvent> = _events.asSharedFlow()

    fun publish(event: TicketEvent) {
        _events.tryEmit(event)
    }
}
