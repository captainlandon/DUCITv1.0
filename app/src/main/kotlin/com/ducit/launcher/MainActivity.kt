package com.ducit.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.ducit.launcher.ui.launcher.LauncherScreen
import com.ducit.launcher.ui.launcher.LauncherViewModel
import com.ducit.launcher.ui.launcher.LauncherViewModelFactory
import com.ducit.launcher.ui.theme.DucitTheme

/**
 * The v0.1 entry point. Renders only the offline app-grid fallback —
 * Now/Command/Governance surfaces are later sprints (Intelligence-to-
 * Implementation Dossier v1.0, section 25 Phase 1: "stable default-
 * launcher experience; process-death recovery").
 */
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<LauncherViewModel> {
        LauncherViewModelFactory((application as DucitApplication).installedAppRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DucitTheme {
                LauncherScreen(viewModel = viewModel)
            }
        }
    }
}
