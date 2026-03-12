package com.example.owlaudiotales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.owlaudiotales.navigation.AppNavGraph
import com.example.owlaudiotales.ui.components.BottomNavigationBar
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
    val startDest by welcomeViewModel.startDestination.collectAsState()

    if (startDest.isNotEmpty()) {
        Scaffold(bottomBar = {
            val currentDestination =
                navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentDestination in listOf("library", "record", "settings") || startDest != "welcome") {
                BottomNavigationBar(navController)
            }
        }) { padding ->
            Box(Modifier.padding(padding)) {
                AppNavGraph(
                    navController = navController,
                    startDestination = startDest,
                    onFirstLaunchComplete = { welcomeViewModel.markFirstLaunchComplete() }
                )
            }
        }
    }
}
