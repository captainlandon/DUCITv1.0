package com.ducit.launcher.ui.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducit.domain.model.PersonalContextRecord
import com.ducit.domain.model.RetentionClass
import com.ducit.domain.model.SensitivityClass
import com.ducit.launcher.data.PersonalContextRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MemoryUiState(
    val records: List<PersonalContextRecord> = emptyList(),
    val isCaptureOpen: Boolean = false,
    val selectedRecordId: String? = null,
) {
    val selectedRecord: PersonalContextRecord?
        get() = records.find { it.recordId == selectedRecordId }
}

/**
 * Drives the Memory Inspector surface (Intelligence-to-Implementation
 * Dossier v1.0, section 6): capture, and — per record —
 * confirm / correct / dispute / restrict-purpose / delete, all of which
 * must "work and propagate" per that section's release criterion. Every
 * mutation here delegates to [PersonalContextRecordRepository], which in
 * turn delegates the actual rules to the pure, JVM-tested
 * `PersonalContextRecordLifecycle` — this class only owns UI state.
 */
class MemoryViewModel(
    private val repository: PersonalContextRecordRepository,
) : ViewModel() {

    private val isCaptureOpen = MutableStateFlow(false)
    private val selectedRecordId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MemoryUiState> = combine(
        repository.observeRecords(),
        isCaptureOpen,
        selectedRecordId,
    ) { records, captureOpen, selectedId ->
        MemoryUiState(records = records, isCaptureOpen = captureOpen, selectedRecordId = selectedId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MemoryUiState(),
    )

    fun openCapture() {
        isCaptureOpen.value = true
    }

    fun closeCapture() {
        isCaptureOpen.value = false
    }

    fun capture(
        subject: String,
        predicate: String,
        value: String,
        purposeAllowlist: List<String>,
        sensitivityClass: SensitivityClass,
    ) {
        if (subject.isBlank() || predicate.isBlank() || value.isBlank()) return
        viewModelScope.launch {
            repository.capture(
                entityId = "person:user",
                subject = subject.trim(),
                predicate = predicate.trim(),
                value = value.trim(),
                sensitivityClass = sensitivityClass,
                purposeAllowlist = purposeAllowlist,
                retentionPolicy = RetentionClass.DURABLE_USER_MODEL,
            )
            isCaptureOpen.value = false
        }
    }

    fun selectRecord(recordId: String) {
        selectedRecordId.value = recordId
    }

    fun closeDetail() {
        selectedRecordId.value = null
    }

    fun confirm(recordId: String) {
        viewModelScope.launch { repository.confirm(recordId) }
    }

    fun dispute(recordId: String) {
        viewModelScope.launch { repository.dispute(recordId) }
    }

    fun correct(recordId: String, newValue: String) {
        if (newValue.isBlank()) return
        viewModelScope.launch {
            repository.correct(recordId, newValue.trim())
            selectedRecordId.value = null
        }
    }

    fun restrictPurpose(recordId: String, newPurposeAllowlist: Set<String>) {
        viewModelScope.launch { repository.restrictPurpose(recordId, newPurposeAllowlist) }
    }

    fun delete(recordId: String) {
        viewModelScope.launch {
            repository.delete(recordId)
            if (selectedRecordId.value == recordId) selectedRecordId.value = null
        }
    }
}
