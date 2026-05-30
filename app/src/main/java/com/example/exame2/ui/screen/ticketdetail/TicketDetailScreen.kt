package com.example.exame2.ui.screen.ticketdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.exame2.core.flags.FeatureFlags
import com.example.exame2.core.state.UiState
import com.example.exame2.domain.model.Ticket
import com.example.exame2.domain.model.TicketPriority
import com.example.exame2.domain.model.TicketStatus
import com.example.exame2.ui.screen.ticketlist.PriorityChip
import com.example.exame2.ui.screen.ticketlist.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticketId: String,
    onBack: () -> Unit,
    viewModel: TicketDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showStatusDialog by remember { mutableStateOf(false) }
    var showPriorityDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(ticketId) {
        viewModel.loadTicket(ticketId)
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(ticketId, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Error -> Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                is UiState.Success -> {
                    TicketDetailContent(
                        ticket = state.data,
                        onUpdateStatusClick = { showStatusDialog = true },
                        onUpdatePriorityClick = { showPriorityDialog = true }
                    )
                }
            }
        }
    }

    if (showStatusDialog && uiState is UiState.Success) {
        val ticket = (uiState as UiState.Success<Ticket>).data
        StatusUpdateDialog(
            currentStatus = ticket.status,
            onDismiss = { showStatusDialog = false },
            onConfirm = { newStatus ->
                showStatusDialog = false
                viewModel.updateStatus(ticketId, newStatus)
            }
        )
    }

    if (showPriorityDialog && uiState is UiState.Success) {
        val ticket = (uiState as UiState.Success<Ticket>).data
        PriorityUpdateDialog(
            currentPriority = ticket.priority,
            onDismiss = { showPriorityDialog = false },
            onConfirm = { newPriority ->
                showPriorityDialog = false
                viewModel.updatePriority(ticketId, newPriority)
            }
        )
    }
}

@Composable
private fun TicketDetailContent(
    ticket: Ticket,
    onUpdateStatusClick: () -> Unit,
    onUpdatePriorityClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = ticket.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PriorityChip(priority = ticket.priority)
            StatusChip(status = ticket.status)
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        DetailRow(label = "Supplier", value = ticket.supplier)
        DetailRow(label = "Category", value = ticket.category.label)
        DetailRow(label = "Reported by", value = ticket.reportedBy)
        DetailRow(label = "Created", value = ticket.createdAt)
        DetailRow(label = "Ticket ID", value = ticket.id)

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Description", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = ticket.description, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Actions", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onUpdateStatusClick, modifier = Modifier.fillMaxWidth()) {
            Text("Update Status")
        }

        if (FeatureFlags.isPriorityUpdateEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onUpdatePriorityClick, modifier = Modifier.fillMaxWidth()) {
                Text("Update Priority")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusUpdateDialog(
    currentStatus: TicketStatus,
    onDismiss: () -> Unit,
    onConfirm: (TicketStatus) -> Unit
) {
    var selected by remember { mutableStateOf(currentStatus) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Status") },
        text = {
            Column {
                TicketStatus.entries.forEach { status ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(selected = selected == status, onClick = { selected = status })
                        Text(status.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(selected) }) { Text("Confirm") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun PriorityUpdateDialog(
    currentPriority: TicketPriority,
    onDismiss: () -> Unit,
    onConfirm: (TicketPriority) -> Unit
) {
    var selected by remember { mutableStateOf(currentPriority) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Priority") },
        text = {
            Column {
                TicketPriority.entries.forEach { priority ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(selected = selected == priority, onClick = { selected = priority })
                        Text(priority.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(selected) }) { Text("Confirm") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
