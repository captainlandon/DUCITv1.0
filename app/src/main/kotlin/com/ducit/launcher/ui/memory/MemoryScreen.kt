package com.ducit.launcher.ui.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ducit.domain.model.PersonalContextRecord
import com.ducit.domain.model.SensitivityClass
import com.ducit.launcher.ui.theme.DucitOnSurfaceMuted
import com.ducit.launcher.ui.theme.DucitSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The Memory Inspector (Intelligence-to-Implementation Dossier v1.0,
 * section 6): "Show 'Ducit thinks / Because / Last verified / Used for.'
 * Confirm, correct, remove, restrict-purpose actions work and propagate."
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Memory") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openCapture) {
                Icon(Icons.Filled.Add, contentDescription = "Remember something")
            }
        },
    ) { padding ->
        if (state.records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Nothing remembered yet. Tap + to add something Ducit should know.",
                    color = DucitOnSurfaceMuted,
                    modifier = Modifier.padding(32.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.records, key = { it.recordId }) { record ->
                    RecordRow(record = record, onClick = { viewModel.selectRecord(record.recordId) })
                }
            }
        }
    }

    if (state.isCaptureOpen) {
        CaptureDialog(
            onDismiss = viewModel::closeCapture,
            onSubmit = { subject, predicate, value, purpose, sensitivity ->
                viewModel.capture(subject, predicate, value, purpose, sensitivity)
            },
        )
    }

    state.selectedRecord?.let { record ->
        RecordDetailDialog(
            record = record,
            onDismiss = viewModel::closeDetail,
            onConfirm = { viewModel.confirm(record.recordId) },
            onDispute = { viewModel.dispute(record.recordId) },
            onCorrect = { newValue -> viewModel.correct(record.recordId, newValue) },
            onRestrictPurpose = { kept -> viewModel.restrictPurpose(record.recordId, kept) },
            onDelete = { viewModel.delete(record.recordId) },
        )
    }
}

@Composable
private fun RecordRow(record: PersonalContextRecord, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DucitSurface, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Text(
            text = "${record.subject} ${record.predicate} ${record.value}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = record.explanationLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = DucitOnSurfaceMuted,
        )
    }
}

@Composable
private fun RecordDetailDialog(
    record: PersonalContextRecord,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onDispute: () -> Unit,
    onCorrect: (String) -> Unit,
    onRestrictPurpose: (Set<String>) -> Unit,
    onDelete: () -> Unit,
) {
    var correctionValue by remember(record.recordId) { mutableStateOf(record.value) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${record.subject} ${record.predicate}") },
        text = {
            Column {
                DetailField(label = "Ducit thinks", value = record.value)
                DetailField(label = "Because", value = record.explanationLabel)
                DetailField(
                    label = "Last verified",
                    value = record.lastVerifiedAt?.let { dateFormat.format(Date(it)) } ?: "Never",
                )
                DetailField(label = "Status", value = "${record.status} / ${record.correctionState}")
                DetailField(label = "Used for", value = if (record.usedForPlanIds.isEmpty()) "Nothing yet" else record.usedForPlanIds.joinToString())
                DetailField(label = "Purpose", value = record.purposeAllowlist.joinToString().ifBlank { "(none)" })

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                OutlinedTextField(
                    value = correctionValue,
                    onValueChange = { correctionValue = it },
                    label = { Text("Correct value") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onConfirm) { Text("Confirm") }
                TextButton(onClick = onDispute) { Text("Dispute") }
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) { Text("Delete") }
                if (record.purposeAllowlist.size > 1) {
                    TextButton(onClick = { onRestrictPurpose(setOf(record.purposeAllowlist.first())) }) {
                        Text("Restrict purpose")
                    }
                }
                Button(
                    onClick = { onCorrect(correctionValue) },
                    enabled = correctionValue.isNotBlank() && correctionValue != record.value,
                ) { Text("Save correction") }
            }
        },
    )
}

@Composable
private fun DetailField(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = DucitOnSurfaceMuted)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CaptureDialog(
    onDismiss: () -> Unit,
    onSubmit: (subject: String, predicate: String, value: String, purpose: List<String>, sensitivity: SensitivityClass) -> Unit,
) {
    var subject by remember { mutableStateOf("") }
    var predicate by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var sensitivity by remember { mutableStateOf(SensitivityClass.STANDARD) }
    var sensitivityMenuExpanded by remember { mutableStateOf(false) }

    val canSubmit = subject.isNotBlank() && predicate.isNotBlank() && value.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remember something") },
        text = {
            Column {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject (e.g. \"user\")") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                )
                OutlinedTextField(
                    value = predicate,
                    onValueChange = { predicate = it },
                    label = { Text("Predicate (e.g. \"hasAllergy\")") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Value") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                )
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Purpose (comma-separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                )
                ExposedDropdownMenuBox(
                    expanded = sensitivityMenuExpanded,
                    onExpandedChange = { sensitivityMenuExpanded = it },
                ) {
                    OutlinedTextField(
                        value = sensitivity.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sensitivity") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sensitivityMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = sensitivityMenuExpanded,
                        onDismissRequest = { sensitivityMenuExpanded = false },
                    ) {
                        SensitivityClass.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name) },
                                onClick = {
                                    sensitivity = option
                                    sensitivityMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = canSubmit,
                onClick = {
                    val purposeList = purpose.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        .ifEmpty { listOf("general") }
                    onSubmit(subject, predicate, value, purposeList, sensitivity)
                },
            ) { Text("Remember") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
