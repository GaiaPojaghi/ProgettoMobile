package com.example.fitjourney.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fitjourney.data.UserData
import com.example.fitjourney.ui.viewModel.AuthViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: AuthViewModel) {
    val context = LocalContext.current
    val userData by viewModel.userData

    var isEditing by remember { mutableStateOf(false) }
    var editedUserData by remember(userData) { mutableStateOf(userData) }
    var showImagePicker by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) } // Stato per il caricamento
    var showDeleteDialog by remember { mutableStateOf(false) } // Dialog conferma eliminazione
    var isDeleting by remember { mutableStateOf(false) } // Stato per eliminazione account
    // Stati per la riautenticazione
    var showReauthDialog by remember { mutableStateOf(false) }
    var passwordForReauth by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.ITALY) }

    // Aggiorna editedUserData quando userData cambia
    LaunchedEffect(userData) {
        editedUserData = userData
        // Non resettare isEditing qui automaticamente
    }

    // Funzione per salvare l'immagine permanentemente
    fun saveImagePermanently(sourceUri: Uri): String? {
        return try {
            val fileName = "profile_image_${System.currentTimeMillis()}.jpg"
            val internalDir = File(context.filesDir, "profile_images")
            if (!internalDir.exists()) {
                internalDir.mkdirs()
            }
            val destinationFile = File(internalDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }

            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Funzione per eliminare la vecchia immagine
    fun deleteOldImage(imagePath: String) {
        try {
            if (imagePath.startsWith("/") && File(imagePath).exists()) {
                File(imagePath).delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Launcher per selezione immagine dalla galleria
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val permanentPath = saveImagePermanently(selectedUri)
            permanentPath?.let { path ->
                // Elimina la vecchia immagine se esiste
                if (editedUserData.photoUrl.isNotEmpty() && editedUserData.photoUrl.startsWith("/")) {
                    deleteOldImage(editedUserData.photoUrl)
                }
                editedUserData = editedUserData.copy(photoUrl = path)
                isEditing = true
            } ?: run {
                Toast.makeText(context, "Errore nel salvare l'immagine", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher per scattare foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { tempUri ->
                val permanentPath = saveImagePermanently(tempUri)
                permanentPath?.let { path ->
                    // Elimina la vecchia immagine se esiste
                    if (editedUserData.photoUrl.isNotEmpty() && editedUserData.photoUrl.startsWith("/")) {
                        deleteOldImage(editedUserData.photoUrl)
                    }
                    editedUserData = editedUserData.copy(photoUrl = path)
                    isEditing = true
                } ?: run {
                    Toast.makeText(context, "Errore nel salvare l'immagine", Toast.LENGTH_SHORT).show()
                }

                // Elimina il file temporaneo
                try {
                    File(tempUri.path ?: "").delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Launcher per permessi fotocamera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val imageFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            tempImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                imageFile
            )
            tempImageUri?.let { cameraLauncher.launch(it) }
        } else {
            Toast.makeText(context, "Permesso fotocamera negato", Toast.LENGTH_SHORT).show()
        }
    }

    // Funzione per mostrare il DatePicker
    val showDatePicker = {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                editedUserData = editedUserData.copy(dataNascita = dateFormatter.format(calendar.time))
                isEditing = true
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Carica i dati utente all'avvio
    LaunchedEffect(viewModel.isLoggedIn()) {
        if (viewModel.isLoggedIn()) {
            viewModel.loadUserData()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (viewModel.isLoggedIn()) {
            // Header con foto profilo e benvenuto
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Foto profilo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { showImagePicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (editedUserData.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = if (editedUserData.photoUrl.startsWith("/")) {
                                File(editedUserData.photoUrl)
                            } else {
                                editedUserData.photoUrl
                            },
                            contentDescription = "Foto profilo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Aggiungi foto",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Icona modifica
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifica foto",
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.BottomEnd)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                            .padding(2.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Benvenuto!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "${userData.nome} ${userData.cognome}".takeIf { it.isNotBlank() }
                            ?: viewModel.getCurrentUserName() ?: "Utente",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Campi dati personali
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Dati Personali",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Nome
                    OutlinedTextField(
                        value = editedUserData.nome,
                        onValueChange = {
                            editedUserData = editedUserData.copy(nome = it)
                            isEditing = true
                        },
                        label = { Text("Nome") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Cognome
                    OutlinedTextField(
                        value = editedUserData.cognome,
                        onValueChange = {
                            editedUserData = editedUserData.copy(cognome = it)
                            isEditing = true
                        },
                        label = { Text("Cognome") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Data di nascita
                    OutlinedTextField(
                        value = editedUserData.dataNascita,
                        onValueChange = { },
                        label = { Text("Data di nascita") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { if (!isSaving) showDatePicker() },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Seleziona data",
                                modifier = Modifier.clickable { if (!isSaving) showDatePicker() }
                            )
                        },
                        enabled = !isSaving
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sesso
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Sesso",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    selected = editedUserData.sesso == "Maschio",
                                    onClick = {
                                        if (!isSaving) {
                                            editedUserData = editedUserData.copy(sesso = "Maschio")
                                            isEditing = true
                                        }
                                    },
                                    enabled = !isSaving
                                )
                                Text(
                                    text = "Maschio",
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .clickable {
                                            if (!isSaving) {
                                                editedUserData = editedUserData.copy(sesso = "Maschio")
                                                isEditing = true
                                            }
                                        }
                                )
                            }
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = editedUserData.sesso == "Femmina",
                                    onClick = {
                                        if (!isSaving) {
                                            editedUserData = editedUserData.copy(sesso = "Femmina")
                                            isEditing = true
                                        }
                                    },
                                    enabled = !isSaving
                                )
                                Text(
                                    text = "Femmina",
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .clickable {
                                            if (!isSaving) {
                                                editedUserData = editedUserData.copy(sesso = "Femmina")
                                                isEditing = true
                                            }
                                        }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Email
                    OutlinedTextField(
                        value = editedUserData.email,
                        onValueChange = {
                            editedUserData = editedUserData.copy(email = it)
                            isEditing = true
                        },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Telefono
                    OutlinedTextField(
                        value = editedUserData.telefono,
                        onValueChange = {
                            editedUserData = editedUserData.copy(telefono = it)
                            isEditing = true
                        },
                        label = { Text("Telefono") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pulsanti Salva/Annulla
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isSaving = true

                        // Timeout dopo 30 secondi
                        val timeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
                        val timeoutRunnable = Runnable {
                            if (isSaving) {
                                isSaving = false
                                Toast.makeText(context, "Timeout: operazione troppo lenta. Riprova.", Toast.LENGTH_LONG).show()
                            }
                        }
                        timeoutHandler.postDelayed(timeoutRunnable, 30000) // 30 secondi

                        viewModel.updateUserData(
                            editedUserData,
                            onSuccess = {
                                timeoutHandler.removeCallbacks(timeoutRunnable)
                                isSaving = false
                                isEditing = false
                                Toast.makeText(context, "Modifiche salvate con successo!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { exception ->
                                timeoutHandler.removeCallbacks(timeoutRunnable)
                                isSaving = false
                                Toast.makeText(context, "Errore nel salvare le modifiche: ${exception.message}", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isEditing && !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Salva",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Text(if (isSaving) "Salvataggio..." else "Salva")
                }

                OutlinedButton(
                    onClick = {
                        editedUserData = userData
                        isEditing = false
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isEditing && !isSaving
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Annulla",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Annulla")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pulsante Logout
            Button(
                onClick = {
                    viewModel.logout()
                    Toast.makeText(context, "Logout effettuato", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                enabled = !isSaving && !isDeleting
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pulsante Elimina Account
            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                enabled = !isSaving && !isDeleting
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminazione...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Elimina Account",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Elimina Account")
                }
            }

        } else {
            // Utente non autenticato
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Effettua l'accesso",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Accedi")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { navController.navigate("register") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrati")
                }
            }
        }
    }

    // Dialog per conferma eliminazione account
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Elimina Account",
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text("Sei sicuro di voler eliminare definitivamente il tuo account? Questa azione non può essere annullata e tutti i tuoi dati verranno persi.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        isDeleting = true

                        // Timeout dopo 30 secondi
                        val timeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
                        val timeoutRunnable = Runnable {
                            if (isDeleting) {
                                isDeleting = false
                                Toast.makeText(context, "Timeout: operazione troppo lenta. Riprova.", Toast.LENGTH_LONG).show()
                            }
                        }
                        timeoutHandler.postDelayed(timeoutRunnable, 30000)

                        viewModel.deleteAccount(
                            onSuccess = {
                                timeoutHandler.removeCallbacks(timeoutRunnable)
                                isDeleting = false
                                Toast.makeText(context, "Account eliminato con successo. Arrivederci!", Toast.LENGTH_LONG).show()
                                navController.navigate("home") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                }
                            },
                            onFailure = { exception ->
                                timeoutHandler.removeCallbacks(timeoutRunnable)
                                isDeleting = false
                                Toast.makeText(context, "Errore nell'eliminazione account: ${exception.message}", Toast.LENGTH_LONG).show()
                            },
                            onNeedReauth = {
                                timeoutHandler.removeCallbacks(timeoutRunnable)
                                isDeleting = false
                                showReauthDialog = true
                            }
                        )
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Elimina")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Annulla")
                }
            }
        )
    }

    // Dialog per riautenticazione
    if (showReauthDialog) {
        AlertDialog(
            onDismissRequest = {
                showReauthDialog = false
                passwordForReauth = ""
                passwordError = ""
            },
            title = { Text("Conferma Password") },
            text = {
                Column {
                    Text("Per eliminare l'account, conferma la tua password:")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = passwordForReauth,
                        onValueChange = {
                            passwordForReauth = it
                            passwordError = ""
                        },
                        label = { Text("Password") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        isError = passwordError.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (passwordError.isNotEmpty()) {
                        Text(
                            text = passwordError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (passwordForReauth.isBlank()) {
                            passwordError = "La password è richiesta"
                            return@TextButton
                        }

                        showReauthDialog = false
                        isDeleting = true

                        // Timeout dopo 30 secondi
                        val timeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
                        val timeoutRunnable = Runnable {
                            if (isDeleting) {
                                isDeleting = false
                                Toast.makeText(context, "Timeout: operazione troppo lenta. Riprova.", Toast.LENGTH_LONG).show()
                            }
                        }
                        timeoutHandler.postDelayed(timeoutRunnable, 30000)

                        viewModel.deleteAccount(
                            password = passwordForReauth,
                            onSuccess = {
                                timeoutHandler.removeCallbacks(timeoutRunnable)
                                isDeleting = false
                                passwordForReauth = ""
                                Toast.makeText(context, "Account eliminato con successo. Arrivederci!", Toast.LENGTH_LONG).show()
                                navController.navigate("home") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                }
                            },
                            onFailure = { exception ->
                                timeoutHandler.removeCallbacks(timeoutRunnable)
                                isDeleting = false
                                passwordForReauth = ""

                                val errorMessage = when {
                                    exception.message?.contains("password is invalid") == true ->
                                        "Password non corretta"
                                    exception.message?.contains("network") == true ->
                                        "Errore di connessione. Riprova."
                                    else -> "Errore nell'eliminazione account: ${exception.message}"
                                }

                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Conferma")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showReauthDialog = false
                        passwordForReauth = ""
                        passwordError = ""
                    }
                ) {
                    Text("Annulla")
                }
            }
        )
    }

    // Dialog per selezione immagine
    if (showImagePicker) {
        AlertDialog(
            onDismissRequest = { showImagePicker = false },
            title = { Text("Seleziona foto profilo") },
            text = { Text("Scegli come aggiungere la tua foto profilo") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImagePicker = false
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                            val imageFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                            tempImageUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                imageFile
                            )
                            tempImageUri?.let { cameraLauncher.launch(it) }
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                ) {
                    Text("Fotocamera")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImagePicker = false
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Text("Galleria")
                }
            }
        )
    }
}