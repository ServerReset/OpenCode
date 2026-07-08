package com.opencode.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.opencode.app.ui.OpenCodeApp
import com.opencode.app.ui.theme.OpenCodeTheme
import com.opencode.app.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsState()
            OpenCodeTheme(darkTheme = state.isDarkMode) {
                OpenCodeApp(viewModel, state)
            }
        }
    }
}
