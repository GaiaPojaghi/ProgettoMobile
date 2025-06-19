package com.example.fitjourney.ui.viewModel

import androidx.compose.runtime.mutableStateOf
import com.example.fitjourney.data.UserData
import com.example.fitjourney.util.viewModel.BaseViewModel
import android.util.Log
import java.io.File
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException

class AuthViewModel : BaseViewModel() {

    val userData = mutableStateOf(UserData())

    fun isLoggedIn(): Boolean = isUserLoggedIn()

    fun getCurrentUserName(): String? = getCurrentUserEmail()

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
                            loadUserData()
                            onSuccess()
                        }
                        .addOnFailureListener(onFailure)
                } else {
                    onSuccess()
                }
            }
            .addOnFailureListener(onFailure)
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
            .addOnFailureListener(onFailure)
    }

    fun loadUserData() {
        val userId = getCurrentUserId()
        if (userId != null) {
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
                    }
                }
                .addOnFailureListener {
                    Log.e("AuthViewModel", "Errore nel caricamento dati utente", it)
                }
        }
    }

    fun updateUserData(
        updatedUserData: UserData,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getCurrentUserId()
        if (userId != null) {
            val userDataMap = mapOf(
                "nome" to updatedUserData.nome,
                "cognome" to updatedUserData.cognome,
                "dataNascita" to updatedUserData.dataNascita,
                "email" to updatedUserData.email,
                "telefono" to updatedUserData.telefono,
                "sesso" to updatedUserData.sesso,
                "photoUrl" to updatedUserData.photoUrl
            )

            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        firestore.collection("users").document(userId)
                            .update(userDataMap)
                            .addOnSuccessListener {
                                userData.value = updatedUserData
                                onSuccess()
                            }
                            .addOnFailureListener(onFailure)
                    } else {
                        firestore.collection("users").document(userId)
                            .set(userDataMap)
                            .addOnSuccessListener {
                                userData.value = updatedUserData
                                onSuccess()
                            }
                            .addOnFailureListener(onFailure)
                    }
                }
                .addOnFailureListener(onFailure)
        } else {
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
            val performDeletion = {
                user.delete()
                    .addOnSuccessListener {
                        firestore.collection("users").document(userId)
                            .delete()
                            .addOnSuccessListener {
                                if (userData.value.photoUrl.startsWith("/")) {
                                    deleteProfileImage(userData.value.photoUrl)
                                }
                                userData.value = UserData()
                                onSuccess()
                            }
                            .addOnFailureListener {
                                userData.value = UserData()
                                onSuccess()
                            }
                    }
                    .addOnFailureListener { authException ->
                        if (authException is FirebaseAuthRecentLoginRequiredException) {
                            onNeedReauth()
                        } else {
                            onFailure(authException)
                        }
                    }
            }

            if (password != null) {
                val email = user.email
                if (email != null) {
                    val credential = EmailAuthProvider.getCredential(email, password)
                    user.reauthenticate(credential)
                        .addOnSuccessListener { performDeletion() }
                        .addOnFailureListener(onFailure)
                } else {
                    onFailure(Exception("Email utente non disponibile"))
                }
            } else {
                performDeletion()
            }
        } else {
            onFailure(Exception("Utente non autenticato"))
        }
    }

    private fun deleteProfileImage(imagePath: String) {
        try {
            if (imagePath.startsWith("/") && File(imagePath).exists()) {
                File(imagePath).delete()
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Errore eliminazione immagine profilo", e)
        }
    }

    fun logout() {
        auth.signOut()
        userData.value = UserData()
    }
}