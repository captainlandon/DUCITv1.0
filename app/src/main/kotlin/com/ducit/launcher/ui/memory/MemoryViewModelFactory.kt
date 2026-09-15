package com.ducit.launcher.ui.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.ducit.launcher.data.PersonalContextRecordRepository

class MemoryViewModelFactory(
    private val repository: PersonalContextRecordRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return MemoryViewModel(repository) as T
    }
}
