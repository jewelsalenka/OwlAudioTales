package com.example.owlaudiotales

import android.content.Context
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState

import androidx.navigation.compose.rememberNavController
import com.example.owlaudiotales.datastore.SettingsDataStore
import com.example.owlaudiotales.navigation.AppNavGraph
import com.example.owlaudiotales.ui.components.BottomNavigationBar
import com.example.owlaudiotales.ui.screens.welcome.WelcomeViewModelFactory
import com.example.owlaudiotales.ui.screens.welcome.WelcomeViewModel
import com.example.owlaudiotales.ui.theme.OwlAudioTalesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OwlAudioTalesTheme {
                // A surface container using the 'background' color from the theme
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
fun OwlAudioTalesApp(context: Context = LocalContext.current) {
    val navController = rememberNavController()
    val launchViewModel: WelcomeViewModel = viewModel(
        factory = WelcomeViewModelFactory(SettingsDataStore(context))
    )
    val startDest by launchViewModel.startDestination.collectAsState()

    val database = remember {
        androidx.room.Room.databaseBuilder(
            context.applicationContext,
            com.example.owlaudiotales.data.local.AppDatabase::class.java,
            "audio_db"
        ).build()
    }
    val audioDao = remember { database.audioDao() }
    val recordingsDir = remember { java.io.File(context.filesDir, "recordings") }

    if (startDest.isNotEmpty()) {
        Scaffold(bottomBar = {
            val currentDestination =
                navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentDestination in listOf(
                    "library",
                    "record",
                    "settings"
                ) || startDest != "welcome"
            ) {
                BottomNavigationBar(navController)
            }
        }) { padding ->
            Box(Modifier.padding(padding)) {
                AppNavGraph(
                    navController = navController,
                    startDestination = startDest,
                    onFirstLaunchComplete = { launchViewModel.markFirstLaunchComplete() },
                    recordingsDir = recordingsDir,
                    audioDao = audioDao
                )
            }
        }
    }
}




