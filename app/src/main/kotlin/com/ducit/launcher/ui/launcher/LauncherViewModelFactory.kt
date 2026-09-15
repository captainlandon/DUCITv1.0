package com.ducit.launcher.ui.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.ducit.launcher.data.InstalledAppRepository

class LauncherViewModelFactory(
    private val repository: InstalledAppRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return LauncherViewModel(repository) as T
    }
}
