package com.example.owlaudiotales.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.owlaudiotales.ui.screens.library.LibraryScreen
import com.example.owlaudiotales.ui.screens.record.RecordScreen
import com.example.owlaudiotales.ui.screens.settings.SettingsScreen
import com.example.owlaudiotales.ui.screens.welcome.WelcomeScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    onFirstLaunchComplete: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("welcome") {
            WelcomeScreen(onGetStarted = {
                onFirstLaunchComplete()
                navController.navigate("library") {
                    popUpTo("welcome") { inclusive = true }
                    launchSingleTop = true
                }
            })
        }
        composable("library") {
            LibraryScreen(
                onOpenSettings = { navController.navigate("settings") { launchSingleTop = true } },
                onCreateRecording = { navController.navigate("record") { launchSingleTop = true } }
            )
        }
        composable("record") { RecordScreen(onBack = { navController.popBackStack() }) }
        composable("settings") { SettingsScreen(onBack = { navController.popBackStack() }) }
    }
}
