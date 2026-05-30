package com.example.exame2.ui.screen.ticketlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.exame2.core.flags.FeatureFlags
import com.example.exame2.core.state.UiState
import com.example.exame2.domain.model.Ticket
import com.example.exame2.domain.model.TicketPriority
import com.example.exame2.domain.model.TicketStatus
import com.example.exame2.ui.theme.PriorityColorCritical
import com.example.exame2.ui.theme.PriorityColorHigh
import com.example.exame2.ui.theme.PriorityColorLow
import com.example.exame2.ui.theme.PriorityColorMedium
import com.example.exame2.ui.theme.StatusColorClosed
import com.example.exame2.ui.theme.StatusColorInProgress
import com.example.exame2.ui.theme.StatusColorOpen
import com.example.exame2.ui.theme.StatusColorResolved

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketListScreen(
    onTicketClick: (String) -> Unit,
    onCreateTicket: () -> Unit,
    viewModel: TicketListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support Tickets", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { viewModel.loadTickets() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            if (FeatureFlags.isTicketCreationEnabled) {
                FloatingActionButton(
                    onClick = onCreateTicket,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Create ticket",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material3.TextButton(onClick = { viewModel.loadTickets() }) {
                            Text("Retry")
                        }
                    }
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Text(
                            text = "No tickets found",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.data, key = { it.id }) { ticket ->
                                TicketCard(ticket = ticket, onClick = { onTicketClick(ticket.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketCard(ticket: Ticket, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = ticket.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PriorityChip(priority = ticket.priority)
                StatusChip(status = ticket.status)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = ticket.supplier,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ticket.category.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = ticket.createdAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun PriorityChip(priority: TicketPriority) {
    val color = when (priority) {
        TicketPriority.CRITICAL -> PriorityColorCritical
        TicketPriority.HIGH -> PriorityColorHigh
        TicketPriority.MEDIUM -> PriorityColorMedium
        TicketPriority.LOW -> PriorityColorLow
    }
    SuggestionChip(
        onClick = {},
        label = { Text(priority.label, style = MaterialTheme.typography.labelSmall) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.15f),
            labelColor = color
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = color.copy(alpha = 0.4f)
        )
    )
}

@Composable
fun StatusChip(status: TicketStatus) {
    val color = when (status) {
        TicketStatus.OPEN -> StatusColorOpen
        TicketStatus.IN_PROGRESS -> StatusColorInProgress
        TicketStatus.RESOLVED -> StatusColorResolved
        TicketStatus.CLOSED -> StatusColorClosed
    }
    SuggestionChip(
        onClick = {},
        label = { Text(status.label, style = MaterialTheme.typography.labelSmall) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.15f),
            labelColor = color
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = color.copy(alpha = 0.4f)
        )
    )
}
