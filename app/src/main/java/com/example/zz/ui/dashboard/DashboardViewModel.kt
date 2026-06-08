package com.example.zz.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zz.domain.model.WeightEntry
import com.example.zz.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class DashboardViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val userProfile = userRepository.getUserProfile().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun addWeightEntry(weight: Double) {
        viewModelScope.launch {
            val currentProfile = userProfile.value ?: return@launch
            val newEntry = WeightEntry(date = System.currentTimeMillis(), weight = weight)
            val updatedHistory = currentProfile.weightHistory + newEntry
            
            userRepository.saveUserProfile(
                currentProfile.copy(
                    currentWeight = weight,
                    weightHistory = updatedHistory
                )
            )
        }
    }
}
