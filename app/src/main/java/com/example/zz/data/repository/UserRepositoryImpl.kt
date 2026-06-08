package com.example.zz.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.zz.domain.model.UserProfile
import com.example.zz.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(private val context: Context) : UserRepository {

    private val auth: FirebaseAuth? by lazy {
        try {
            com.google.firebase.FirebaseApp.getInstance()
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            com.google.firebase.FirebaseApp.getInstance()
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val userCollection by lazy { firestore?.collection("users") }

    override fun getUserProfile(): Flow<UserProfile> = callbackFlow {
        val uid = auth?.currentUser?.uid
        val currentCollection = userCollection
        
        var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

        if (uid != null && currentCollection != null) {
            listenerRegistration = currentCollection.document(uid).addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                val profile = snapshot?.toObject(UserProfile::class.java) ?: UserProfile()
                trySend(profile)
            }
        } else {
            trySend(UserProfile())
        }
        
        awaitClose { 
            listenerRegistration?.remove() 
        }
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        val uid = auth?.currentUser?.uid
        if (uid != null && userCollection != null) {
            userCollection?.document(uid)?.set(profile)?.await()
        }
    }

    override suspend fun isUserOnboarded(): Flow<Boolean> {
        return getUserProfile().map { it.age > 0 }
    }
    
    fun isUserLoggedIn(): Boolean {
        return auth?.currentUser != null
    }

    fun logout() {
        auth?.signOut()
    }
}
