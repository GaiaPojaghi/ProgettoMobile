package com.example.fitjourney

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun FitJourneyNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
    }
}
