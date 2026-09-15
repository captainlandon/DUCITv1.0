package com.ducit.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.ducit.launcher.ui.launcher.LauncherScreen
import com.ducit.launcher.ui.launcher.LauncherViewModel
import com.ducit.launcher.ui.launcher.LauncherViewModelFactory
import com.ducit.launcher.ui.memory.MemoryScreen
import com.ducit.launcher.ui.memory.MemoryViewModel
import com.ducit.launcher.ui.memory.MemoryViewModelFactory
import com.ducit.launcher.ui.theme.DucitTheme

/**
 * The v0.1 entry point. Renders the offline app-grid fallback and the
 * Memory Inspector — Now/Command/Governance surfaces are later sprints
 * (Intelligence-to-Implementation Dossier v1.0, section 25 Phase 1:
 * "stable default-launcher experience; process-death recovery").
 *
 * No navigation library yet: a single `rememberSaveable` boolean is
 * enough for two screens and survives process death on its own, which is
 * what B-003 ("process death at every state restores or safely
 * reconciles") actually requires here — a real backstack can wait until
 * there's a third screen to justify it.
 */
class MainActivity : ComponentActivity() {

    private val launcherViewModel by viewModels<LauncherViewModel> {
        LauncherViewModelFactory((application as DucitApplication).installedAppRepository)
    }

    private val memoryViewModel by viewModels<MemoryViewModel> {
        MemoryViewModelFactory((application as DucitApplication).personalContextRecordRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showMemory by rememberSaveable { mutableStateOf(false) }
            BackHandler(enabled = showMemory) { showMemory = false }

            DucitTheme {
                if (showMemory) {
                    MemoryScreen(
                        viewModel = memoryViewModel,
                        onBack = { showMemory = false },
                    )
                } else {
                    LauncherScreen(
                        viewModel = launcherViewModel,
                        onOpenMemory = { showMemory = true },
                    )
                }
            }
        }
    }
}
