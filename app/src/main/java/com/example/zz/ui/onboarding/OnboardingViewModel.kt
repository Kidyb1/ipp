package com.example.zz.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zz.domain.model.ActivityLevel
import com.example.zz.domain.model.Gender
import com.example.zz.domain.model.UserProfile
import com.example.zz.domain.repository.UserRepository
import com.example.zz.domain.usecase.CalculateMetabolismUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel obsługujący logikę onboardingu.
 * Zarządza stanem profilu w trakcie wypełniania formularza.
 */
class OnboardingViewModel(
    private val userRepository: UserRepository,
    private val calculateMetabolismUseCase: CalculateMetabolismUseCase = CalculateMetabolismUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfile())
    val uiState: StateFlow<UserProfile> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateGender(gender: Gender) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun updateAge(age: String) {
        val ageInt = age.toIntOrNull() ?: 0
        _uiState.update { it.copy(age = ageInt) }
    }

    fun updateHeight(height: String) {
        val heightDouble = height.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(height = heightDouble) }
    }

    fun updateCurrentWeight(weight: String) {
        val weightDouble = weight.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(currentWeight = weightDouble) }
    }

    fun updateTargetWeight(weight: String) {
        val weightDouble = weight.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(targetWeight = weightDouble) }
    }

    fun updateActivityLevel(level: ActivityLevel) {
        _uiState.update { it.copy(activityLevel = level) }
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Obliczamy parametry końcowe przed zapisem
            val calculatedProfile = calculateMetabolismUseCase(_uiState.value)
            userRepository.saveUserProfile(calculatedProfile)
            onSuccess()
        }
    }
}
