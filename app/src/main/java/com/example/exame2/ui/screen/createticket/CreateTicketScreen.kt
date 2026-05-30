package com.example.exame2.ui.screen.createticket

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.exame2.domain.model.TicketCategory
import com.example.exame2.domain.model.TicketPriority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketScreen(
    onBack: () -> Unit,
    onTicketCreated: () -> Unit,
    viewModel: CreateTicketViewModel = viewModel()
) {
    val createState by viewModel.createState.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TicketPriority.MEDIUM) }
    var selectedCategory by remember { mutableStateOf(TicketCategory.SUPPLIER) }

    LaunchedEffect(createState) {
        if (createState is UiState.Success) {
            viewModel.clearState()
            onTicketCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Ticket", fontWeight = FontWeight.Bold) },
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
        if (!FeatureFlags.isTicketCreationEnabled) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    text = "Ticket creation is currently disabled",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = supplier,
                onValueChange = { supplier = it },
                label = { Text("Supplier *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            DropdownSelector(
                label = "Priority",
                selected = selectedPriority.label,
                options = TicketPriority.entries.map { it.label },
                onSelect = { label ->
                    selectedPriority = TicketPriority.entries.first { it.label == label }
                }
            )

            val visibleCategories = if (FeatureFlags.isDistributionCategoryVisible) {
                TicketCategory.entries
            } else {
                TicketCategory.entries.filter { it != TicketCategory.DISTRIBUTION }
            }

            DropdownSelector(
                label = "Category",
                selected = selectedCategory.label,
                options = visibleCategories.map { it.label },
                onSelect = { label ->
                    selectedCategory = TicketCategory.entries.first { it.label == label }
                }
            )

            if (createState is UiState.Error) {
                Text(
                    text = (createState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.createTicket(
                        title = title,
                        description = description,
                        priority = selectedPriority,
                        category = selectedCategory,
                        supplier = supplier
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = createState !is UiState.Loading && title.isNotBlank() && supplier.isNotBlank()
            ) {
                if (createState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit Ticket")
                }
            }
        }
    }
}

@Composable
private fun DropdownSelector(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Expand")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
