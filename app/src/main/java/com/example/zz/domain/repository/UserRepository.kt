package com.example.zz.domain.repository

import com.example.zz.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Interfejs repozytorium użytkownika - definiuje operacje na danych profilu.
 */
interface UserRepository {
    fun getUserProfile(): Flow<UserProfile>
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun isUserOnboarded(): Flow<Boolean>
}
