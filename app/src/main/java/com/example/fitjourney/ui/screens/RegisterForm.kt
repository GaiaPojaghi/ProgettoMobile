package com.example.fitjourney.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitjourney.ui.viewModel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RegisterForm(navController: NavController, viewModel: AuthViewModel) {
    var nome by remember { mutableStateOf("") }
    var cognome by remember { mutableStateOf("") }
    var dataNascita by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var sesso by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.ITALY) }

    // Funzione per mostrare il DatePicker
    val showDatePicker = {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                dataNascita = dateFormatter.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Validazione password
    val passwordsMatch = password == confirmPassword
    val passwordError = when {
        password.isNotEmpty() && password.length < 6 -> "La password deve essere di almeno 6 caratteri"
        confirmPassword.isNotEmpty() && !passwordsMatch -> "Le password non coincidono"
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registrati", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Nome *
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome *") },
            modifier = Modifier.fillMaxWidth(),
            isError = nome.isBlank()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Cognome *
        OutlinedTextField(
            value = cognome,
            onValueChange = { cognome = it },
            label = { Text("Cognome *") },
            modifier = Modifier.fillMaxWidth(),
            isError = cognome.isBlank()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo data con icona calendario *
        OutlinedTextField(
            value = dataNascita,
            onValueChange = { },
            label = { Text("Data di nascita *") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker() },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Seleziona data",
                    modifier = Modifier.clickable { showDatePicker() }
                )
            },
            isError = dataNascita.isBlank()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo sesso con selezione tramite pulsanti radio *
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Sesso *",
                style = MaterialTheme.typography.bodyMedium,
                color = if (sesso.isBlank()) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = sesso == "Maschio",
                        onClick = { sesso = "Maschio" }
                    )
                    Text(
                        text = "Maschio",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable { sesso = "Maschio" }
                    )
                }
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = sesso == "Femmina",
                        onClick = { sesso = "Femmina" }
                    )
                    Text(
                        text = "Femmina",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable { sesso = "Femmina" }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Email *
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email *") },
            modifier = Modifier.fillMaxWidth(),
            isError = email.isBlank()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Telefono (opzionale)
        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Telefono") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password *
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password *") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = password.isNotEmpty() && password.length < 6,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Nascondi password" else "Mostra password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Conferma Password *
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Conferma Password *") },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = confirmPassword.isNotEmpty() && !passwordsMatch,
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Nascondi password" else "Mostra password"
                    )
                }
            }
        )

        // Messaggio di errore per le password
        if (passwordError != null) {
            Text(
                text = passwordError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nota sui campi obbligatori
        Text(
            text = "* Campi obbligatori",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Validazione completa
                val isValid = nome.isNotBlank() &&
                        cognome.isNotBlank() &&
                        dataNascita.isNotBlank() &&
                        sesso.isNotBlank() &&
                        email.isNotBlank() &&
                        password.length >= 6 &&
                        passwordsMatch

                if (isValid) {
                    viewModel.register(
                        nome,
                        cognome,
                        dataNascita,
                        email,
                        telefono,
                        password,
                        sesso,
                        onSuccess = {
                            Toast.makeText(context, "Registrazione completata", Toast.LENGTH_LONG).show()
                            navController.navigate("profile")
                        },
                        onFailure = {
                            Toast.makeText(context, "Errore: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                } else {
                    val errorMessage = when {
                        nome.isBlank() || cognome.isBlank() || dataNascita.isBlank() ||
                                sesso.isBlank() || email.isBlank() -> "Completa tutti i campi obbligatori!"
                        password.length < 6 -> "La password deve essere di almeno 6 caratteri"
                        !passwordsMatch -> "Le password non coincidono"
                        else -> "Controlla i dati inseriti"
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crea account")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Hai già un account? Accedi",
            modifier = Modifier.clickable {
                navController.navigate("login")
            },
            color = MaterialTheme.colorScheme.primary
        )
    }
}