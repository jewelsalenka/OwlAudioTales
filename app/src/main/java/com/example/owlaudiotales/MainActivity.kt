package com.example.owlaudiotales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.owlaudiotales.navigation.AppNavGraph
import com.example.owlaudiotales.ui.screens.welcome.WelcomeViewModel
import com.example.owlaudiotales.ui.theme.OwlAudioTalesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OwlAudioTalesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OwlAudioTalesApp()
                }
            }
        }
    }
}

@Composable
fun OwlAudioTalesApp() {
    val navController = rememberNavController()
    val welcomeViewModel: WelcomeViewModel = hiltViewModel()
    val startDest by welcomeViewModel.startDestination.collectAsStateWithLifecycle()

    if (startDest.isNotEmpty()) {
        AppNavGraph(
            navController = navController,
            startDestination = startDest,
            onFirstLaunchComplete = welcomeViewModel::markFirstLaunchComplete
        )
    }
}
