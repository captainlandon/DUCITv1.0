package com.ducit.launcher.ui.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducit.domain.model.InstalledApp
import com.ducit.launcher.data.InstalledAppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LauncherUiState(
    val apps: List<InstalledApp> = emptyList(),
    val searchQuery: String = "",
    val isRefreshing: Boolean = true,
    val lastLaunchFailed: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class LauncherViewModel(
    private val repository: InstalledAppRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val isRefreshing = MutableStateFlow(true)
    private val lastLaunchFailed = MutableStateFlow(false)

    private val appsForQuery = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) repository.observeApps() else repository.search(query)
    }

    val uiState: StateFlow<LauncherUiState> = combine(
        appsForQuery,
        searchQuery,
        isRefreshing,
        lastLaunchFailed,
    ) { apps, query, refreshing, launchFailed ->
        LauncherUiState(
            apps = apps,
            searchQuery = query,
            isRefreshing = refreshing,
            lastLaunchFailed = launchFailed,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LauncherUiState(),
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            repository.refresh()
            isRefreshing.value = false
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun launch(packageName: String) {
        lastLaunchFailed.value = !repository.launch(packageName)
    }

    fun consumeLaunchFailure() {
        lastLaunchFailed.value = false
    }
}
