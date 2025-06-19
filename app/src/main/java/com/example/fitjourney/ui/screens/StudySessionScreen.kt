package com.example.fitjourney.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.fitjourney.ui.viewModel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySessionScreen(navController: NavController, viewModel: StudyViewModel = viewModel()) {
    val totalSeconds = 25 * 60
    var remainingTime by remember { mutableStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(true) }
    var elapsedTime by remember { mutableStateOf(0) }

    LaunchedEffect(isRunning) {
        while (isRunning && remainingTime > 0) {
            delay(1000)
            remainingTime--
            elapsedTime++
        }

        if (remainingTime == 0 && elapsedTime > 0) {
            viewModel.addLiveStudyTime(elapsedTime)
            viewModel.incrementSessionCount()
        }
    }

    val progress = 1f - (remainingTime.toFloat() / totalSeconds)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = androidx.compose.animation.core.tween(500),
        label = "Progress"
    )

    val minutes = remainingTime / 60
    val seconds = remainingTime % 60

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sessione di Studio", fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        isRunning = false
                        if (elapsedTime > 0) {
                            viewModel.addLiveStudyTime(elapsedTime)
                            viewModel.incrementSessionCount()
                        }
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 16.dp.toPx()
                        val center = center
                        val radius = size.minDimension / 2 - stroke / 2
                        drawCircle(
                            color = Color.LightGray,
                            radius = radius,
                            center = center,
                            style = Stroke(stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFF4CAF50),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(stroke, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row {
                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isRunning) "Pausa" else "Riprendi", color = MaterialTheme.colorScheme.onPrimary)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            isRunning = false
                            if (elapsedTime > 0) {
                                viewModel.addLiveStudyTime(elapsedTime)
                                viewModel.incrementSessionCount()
                            }
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Termina", color = Color.White)
                    }
                }
            }
        }
    }
}
