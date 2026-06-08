package com.example.zz.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zz.domain.model.WeightEntry
import com.example.zz.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentTipIndex = MutableStateFlow(0)
    
    val userProfile = userRepository.getUserProfile().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val currentTip = combine(userProfile, _currentTipIndex) { profile, index ->
        if (profile == null) return@combine ""
        val tips = getTipsForGoal(profile.goal)
        tips[index % tips.size]
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun refreshTip() {
        val profile = userProfile.value ?: return
        val tips = getTipsForGoal(profile.goal)
        _currentTipIndex.value = (0 until tips.size).random()
    }

    private fun getTipsForGoal(goal: com.example.zz.domain.model.UserGoal): List<String> {
        return when (goal) {
            com.example.zz.domain.model.UserGoal.REDUKCJA -> listOf(
                "Pierwsze dni redukcji! Pamiętaj o picu dużej ilości wody i wyspaniu się.",
                "Głód na redukcji to norma, ale jeśli jest nie do zniesienia, zwiększ ilość warzyw.",
                "Pamiętaj o treningu siłowym - pomaga utrzymać masę mięśniową.",
                "Nie polegaj tylko na wadze. Rób zdjęcia sylwetki i mierz obwody.",
                "Dwa tygodnie za Tobą? Jeśli czujesz zmęczenie, rozważ lekki 'refeed'."
            )
            com.example.zz.domain.model.UserGoal.MASA -> listOf(
                "Budujemy masę! Skup się na progresji ciężarowej na treningu.",
                "Zadbaj o odpowiednią ilość białka, to budulec Twoich mięśni.",
                "Nie bój się węglowodanów, one dadzą Ci siłę na ciężkie treningi.",
                "Nadmiar kalorii nie musi oznaczać 'śmieciowego' jedzenia.",
                "Skup się na jakości ruchu, a nie tylko na samym ciężarze."
            )
            com.example.zz.domain.model.UserGoal.UTRZYMANIE -> listOf(
                "Utrzymanie wagi to świetny czas na poprawę techniki w ćwiczeniach.",
                "Słuchaj swojego organizmu i dostosowuj kalorie do aktywności.",
                "Balans jest kluczem. Ciesz się jedzeniem i ruchem.",
                "Stabilizacja wagi to też postęp. Gratulacje!",
                "Wykorzystaj ten czas na budowanie zdrowych nawyków, które zostaną na lata."
            )
        }
    }

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
