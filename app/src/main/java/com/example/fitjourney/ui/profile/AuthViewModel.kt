package com.example.fitjourney.ui.profile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fitjourney.data.UserData
import android.util.Log
import java.io.File

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val userData = mutableStateOf(UserData())

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserName(): String? {
        return auth.currentUser?.email
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun register(
        nome: String,
        cognome: String,
        dataNascita: String,
        email: String,
        telefono: String,
        password: String,
        sesso: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                // Salva i dati utente in Firestore
                val userId = result.user?.uid
                if (userId != null) {
                    val userDataMap = hashMapOf(
                        "nome" to nome,
                        "cognome" to cognome,
                        "dataNascita" to dataNascita,
                        "email" to email,
                        "telefono" to telefono,
                        "sesso" to sesso,
                        "photoUrl" to ""
                    )

                    firestore.collection("users").document(userId)
                        .set(userDataMap)
                        .addOnSuccessListener {
                            // Carica immediatamente i dati dopo la registrazione
                            loadUserData()
                            onSuccess()
                        }
                        .addOnFailureListener { onFailure(it) }
                } else {
                    onSuccess()
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                loadUserData()
                onSuccess()
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun loadUserData() {
        val userId = getCurrentUserId()
        if (userId != null) {
            Log.d("AuthViewModel", "Caricamento dati utente per ID: $userId")
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val newUserData = UserData(
                            nome = document.getString("nome") ?: "",
                            cognome = document.getString("cognome") ?: "",
                            dataNascita = document.getString("dataNascita") ?: "",
                            email = document.getString("email") ?: "",
                            telefono = document.getString("telefono") ?: "",
                            sesso = document.getString("sesso") ?: "",
                            photoUrl = document.getString("photoUrl") ?: ""
                        )
                        userData.value = newUserData
                        Log.d("AuthViewModel", "Dati utente caricati: $newUserData")
                    } else {
                        Log.d("AuthViewModel", "Documento utente non trovato")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("AuthViewModel", "Errore nel caricamento dati utente", exception)
                }
        } else {
            Log.d("AuthViewModel", "User ID è null")
        }
    }

    fun updateUserData(
        updatedUserData: UserData,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getCurrentUserId()
        if (userId != null) {
            Log.d("AuthViewModel", "Aggiornamento dati utente per ID: $userId")
            Log.d("AuthViewModel", "Nuovi dati: $updatedUserData")

            val userDataMap = mapOf(
                "nome" to updatedUserData.nome,
                "cognome" to updatedUserData.cognome,
                "dataNascita" to updatedUserData.dataNascita,
                "email" to updatedUserData.email,
                "telefono" to updatedUserData.telefono,
                "sesso" to updatedUserData.sesso,
                "photoUrl" to updatedUserData.photoUrl
            )

            // Prima prova a verificare se il documento esiste
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Il documento esiste, usa update
                        Log.d("AuthViewModel", "Documento esistente, uso update")
                        firestore.collection("users").document(userId)
                            .update(userDataMap)
                            .addOnSuccessListener {
                                Log.d("AuthViewModel", "Dati utente aggiornati con successo (update)")
                                userData.value = updatedUserData
                                onSuccess()
                            }
                            .addOnFailureListener { exception ->
                                Log.e("AuthViewModel", "Errore nell'update dei dati utente", exception)
                                onFailure(exception)
                            }
                    } else {
                        // Il documento non esiste, usa set
                        Log.d("AuthViewModel", "Documento non esistente, uso set")
                        firestore.collection("users").document(userId)
                            .set(userDataMap)
                            .addOnSuccessListener {
                                Log.d("AuthViewModel", "Dati utente creati con successo (set)")
                                userData.value = updatedUserData
                                onSuccess()
                            }
                            .addOnFailureListener { exception ->
                                Log.e("AuthViewModel", "Errore nel set dei dati utente", exception)
                                onFailure(exception)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("AuthViewModel", "Errore nel controllo esistenza documento", exception)
                    onFailure(exception)
                }
        } else {
            Log.e("AuthViewModel", "Tentativo di aggiornamento senza utente autenticato")
            onFailure(Exception("Utente non autenticato"))
        }
    }

    fun deleteAccount(
        password: String? = null,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
        onNeedReauth: () -> Unit = {}
    ) {
        val user = auth.currentUser
        val userId = getCurrentUserId()

        if (user != null && userId != null) {
            Log.d("AuthViewModel", "Eliminazione account per ID: $userId")

            // Funzione per eliminare l'account dopo la riautenticazione
            val performDeletion = {
                // PRIMA elimina l'account da Firebase Auth
                user.delete()
                    .addOnSuccessListener {
                        Log.d("AuthViewModel", "Account Auth eliminato con successo")

                        // DOPO elimina i dati da Firestore
                        firestore.collection("users").document(userId)
                            .delete()
                            .addOnSuccessListener {
                                Log.d("AuthViewModel", "Dati Firestore eliminati con successo")

                                // Elimina l'immagine profilo se esiste
                                val currentPhotoUrl = userData.value.photoUrl
                                if (currentPhotoUrl.isNotEmpty() && currentPhotoUrl.startsWith("/")) {
                                    deleteProfileImage(currentPhotoUrl)
                                }

                                // Reset user data
                                userData.value = UserData()
                                onSuccess()
                            }
                            .addOnFailureListener { firestoreException ->
                                Log.e("AuthViewModel", "Errore nell'eliminazione dati Firestore (account già eliminato)", firestoreException)
                                // Account Auth già eliminato, continua comunque
                                userData.value = UserData()
                                onSuccess()
                            }
                    }
                    .addOnFailureListener { authException ->
                        Log.e("AuthViewModel", "Errore nell'eliminazione account Auth", authException)

                        // Controlla se è un errore di riautenticazione
                        if (authException is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                            Log.d("AuthViewModel", "Richiesta riautenticazione")
                            onNeedReauth()
                        } else {
                            onFailure(authException)
                        }
                    }
            }

            // Se è fornita una password, riautentica prima
            if (password != null) {
                val email = user.email
                if (email != null) {
                    val credential = com.google.firebase.auth.EmailAuthProvider
                        .getCredential(email, password)

                    user.reauthenticate(credential)
                        .addOnSuccessListener {
                            Log.d("AuthViewModel", "Riautenticazione riuscita, procedo con eliminazione")
                            performDeletion()
                        }
                        .addOnFailureListener { reauthException ->
                            Log.e("AuthViewModel", "Errore nella riautenticazione", reauthException)
                            onFailure(reauthException)
                        }
                } else {
                    onFailure(Exception("Email utente non disponibile"))
                }
            } else {
                // Primo tentativo senza riautenticazione
                performDeletion()
            }
        } else {
            Log.e("AuthViewModel", "Tentativo di eliminazione senza utente autenticato")
            onFailure(Exception("Utente non autenticato"))
        }
    }

    private fun deleteProfileImage(imagePath: String) {
        try {
            if (imagePath.startsWith("/") && File(imagePath).exists()) {
                val deleted = File(imagePath).delete()
                Log.d("AuthViewModel", "Immagine profilo eliminata: $deleted")
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Errore nell'eliminazione immagine profilo", e)
        }
    }

    fun logout() {
        auth.signOut()
        userData.value = UserData() // Reset user data
        Log.d("AuthViewModel", "Logout effettuato")
    }

    // Metodo per testare la connessione Firebase (rimuovi in produzione)
    fun testFirebaseConnection(callback: (Boolean, String) -> Unit) {
        val userId = getCurrentUserId()
        if (userId != null) {
            Log.d("AuthViewModel", "Test connessione Firebase per user: $userId")

            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Log.d("AuthViewModel", "Test Firebase: documento trovato")
                        callback(true, "Connessione OK - Documento trovato")
                    } else {
                        Log.d("AuthViewModel", "Test Firebase: documento non trovato")
                        callback(false, "Documento utente non trovato")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("AuthViewModel", "Test Firebase fallito", exception)
                    callback(false, "Errore: ${exception.message}")
                }
        } else {
            callback(false, "User ID null")
        }
    }
}