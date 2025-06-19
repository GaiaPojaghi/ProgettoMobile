package com.example.fitjourney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.fitjourney.ui.screens.StudyHomeScreen
import androidx.navigation.compose.*
import com.example.fitjourney.ui.viewModel.AuthViewModel
import com.example.fitjourney.ui.screens.*
import com.example.fitjourney.ui.theme.FitJourneyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitJourneyTheme {
                val authViewModel = remember { AuthViewModel() }
                FitJourneyApp(authViewModel)
            }
        }
    }
}

@Composable
fun FitJourneyApp(viewModel: AuthViewModel) {
    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem("Home", Icons.Filled.Home, "home"),
        BottomNavItem("Settimana", Icons.Filled.BarChart, "weekly"),
        BottomNavItem("Medaglie", Icons.Filled.EmojiEvents, "rewards"),
        BottomNavItem("Profilo", Icons.Filled.Person, "profile")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { StudyHomeScreen(navController) }
            composable("studySession") { StudySessionScreen(navController) }
            composable("weekly") { WeeklyDataScreen() }
            composable("rewards") { RewardsScreen() }
            composable("profile") { ProfileScreen(navController, viewModel) }
            composable("login") { LoginScreen(navController, viewModel) }
            composable("register") { RegisterForm(navController, viewModel) }
        }
    }
}

data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)
