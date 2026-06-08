package com.example.zz.data.repository

import android.content.Context
import com.example.zz.domain.model.UserProfile
import com.example.zz.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(private val context: Context) : UserRepository {

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val userCollection by lazy { firestore?.collection("users") }

    override fun getUserProfile(): Flow<UserProfile> = callbackFlow {
        val currentAuth = auth
        val currentCollection = userCollection
        val uid = currentAuth?.currentUser?.uid
        
        if (uid != null && currentCollection != null) {
            val listenerRegistration = currentCollection.document(uid).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("UserRepository", "Błąd Firestore (getUserProfile): ${error.message}", error)
                    trySend(UserProfile())
                    return@addSnapshotListener
                }
                
                val profile = snapshot?.toObject(UserProfile::class.java) ?: UserProfile()
                trySend(profile)
            }
            awaitClose { listenerRegistration.remove() }
        } else {
            trySend(UserProfile())
            awaitClose { /* Nic do usunięcia */ }
        }
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        val uid = auth?.currentUser?.uid
        if (uid != null && userCollection != null) {
            try {
                userCollection!!.document(uid).set(profile).await()
                android.util.Log.d("UserRepository", "Profil zapisany pomyślnie dla UID: $uid")
            } catch (e: Exception) {
                android.util.Log.e("UserRepository", "Błąd zapisu profilu: ${e.message}", e)
                throw e
            }
        }
    }

    override suspend fun isUserOnboarded(): Flow<Boolean> {
        return getUserProfile().map { it.age > 0 }
    }
    
    fun getAuthStateFlow(): Flow<Boolean> = callbackFlow {
        val currentAuth = auth
        val listener = if (currentAuth != null) {
            val l = FirebaseAuth.AuthStateListener { a ->
                trySend(a.currentUser != null)
            }
            currentAuth.addAuthStateListener(l)
            l
        } else {
            trySend(false)
            null
        }
        
        awaitClose { 
            listener?.let { currentAuth?.removeAuthStateListener(it) }
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth?.currentUser != null
    }

    fun logout() {
        auth?.signOut()
    }
}
