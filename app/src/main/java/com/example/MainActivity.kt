package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.StudioMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: StudioViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            MyApplicationTheme(darkTheme = !uiState.isLightTheme) {
                StudioMainScreen(viewModel = viewModel)
            }
        }
    }
}
