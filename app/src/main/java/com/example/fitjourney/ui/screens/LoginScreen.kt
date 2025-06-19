package com.example.fitjourney.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitjourney.ui.profile.AuthViewModel

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password con pulsante visibilità
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible }
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Nascondi password" else "Mostra password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pulsante Login
        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    isLoading = true

                    viewModel.login(
                        email.trim(),
                        password,
                        onSuccess = {
                            isLoading = false
                            Toast.makeText(context, "Login effettuato", Toast.LENGTH_SHORT).show()
                            navController.navigate("profile") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onFailure = { exception ->
                            isLoading = false
                            val errorMessage = when {
                                exception.message?.contains("invalid-email") == true ->
                                    "Email non valida"
                                exception.message?.contains("user-not-found") == true ->
                                    "Utente non trovato"
                                exception.message?.contains("wrong-password") == true ->
                                    "Password non corretta"
                                exception.message?.contains("network") == true ->
                                    "Errore di connessione. Riprova."
                                else -> "Errore: ${exception.message}"
                            }
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "Inserisci email e password", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Accesso...")
            } else {
                Text("Accedi")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Non hai un account? Registrati",
            modifier = Modifier.clickable {
                if (!isLoading) {
                    navController.navigate("register")
                }
            },
            color = MaterialTheme.colorScheme.primary
        )
    }
}