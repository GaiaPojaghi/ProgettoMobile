package com.example.fitjourney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourney.ui.screens.StudyHomeScreen
import androidx.navigation.compose.*
import com.example.fitjourney.ui.viewModel.AuthViewModel
import com.example.fitjourney.ui.screens.*
import com.example.fitjourney.ui.theme.FitJourneyTheme
import com.example.fitjourney.ui.viewModel.StudyViewModel

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
    val studyViewModel = remember { StudyViewModel() }

    // Osserva i cambiamenti nei dati di studio per il badge
    val studyData by studyViewModel.studyData

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
                        icon = {
                            Box {
                                Icon(item.icon, contentDescription = item.label)

                                // Mostra il badge rosso solo sull'icona "Medaglie" se c'è una nuova medaglia
                                if (item.route == "rewards" && studyData.newMedalUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }
                        },
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
            composable("home") { StudyHomeScreen(navController, studyViewModel) } // Passa anche qui il ViewModel
            composable("studySession") { StudySessionScreen(navController, studyViewModel) } // E qui
            composable("weekly") { WeeklyDataScreen() }
            composable("rewards") { RewardsScreen(viewModel = studyViewModel) }
            composable("profile") { ProfileScreen(navController, viewModel) }
            composable("login") { LoginScreen(navController, viewModel) }
            composable("register") { RegisterForm(navController, viewModel) }
        }
    }
}

data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)