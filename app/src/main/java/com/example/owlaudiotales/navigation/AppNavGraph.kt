package com.example.owlaudiotales.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.owlaudiotales.data.local.AudioDao
import com.example.owlaudiotales.ui.screens.library.LibraryScreen
import com.example.owlaudiotales.ui.screens.record.RecordScreen
import com.example.owlaudiotales.ui.screens.settings.SettingsScreen
import com.example.owlaudiotales.ui.screens.welcome.WelcomeScreen
import java.io.File

@Composable
fun AppNavGraph(navController: NavHostController,
                startDestination: String,
                onFirstLaunchComplete: () -> Unit,
                recordingsDir: File,
                audioDao: AudioDao
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("welcome") { WelcomeScreen(onGetStarted = {
            onFirstLaunchComplete()
            navController.navigate("library") {
                popUpTo("welcome") { inclusive = true }
                launchSingleTop = true
            }}) }
        composable("library") { LibraryScreen(audioDao) }
        composable("record") { RecordScreen(
            recordingsDir = recordingsDir,
            audioDao = audioDao
        ) }
        composable("settings") { SettingsScreen(navController) }
    }
}