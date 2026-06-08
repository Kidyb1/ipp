package com.example.zz.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Gender {
    MALE, FEMALE
}

@Serializable
enum class UserGoal {
    REDUKCJA, UTRZYMANIE, MASA
}

@Serializable
enum class ActivityLevel(val factor: Double) {
    SEDENTARY(1.2),    // Siedzący tryb życia
    LIGHT(1.375),      // Lekka aktywność (1-2 treningi)
    MODERATE(1.55),    // Średnia aktywność (3-4 treningi)
    ACTIVE(1.725),     // Wysoka aktywność (codzienne treningi)
    VERY_ACTIVE(1.9)   // Bardzo wysoka (praca fizyczna + treningi)
}

@Serializable
data class WeightEntry(
    val date: Long = 0L, // Timestamp
    val weight: Double = 0.0
)

@Serializable
enum class DietPace(val label: String, val weeklyChangeKg: Double) {
    CONSERVATIVE("Zachowawcze (0.25kg/tydz)", 0.25),
    MODERATE("Zrównoważone (0.5kg/tydz)", 0.5),
    AGGRESSIVE("Agresywne (0.8kg/tydz)", 0.8)
}

@Serializable
data class UserProfile(
    val name: String = "",
    val age: Int = 0,
    val height: Double = 0.0,
    val currentWeight: Double = 0.0,
    val targetWeight: Double = 0.0,
    val gender: Gender = Gender.MALE,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val dietPace: DietPace = DietPace.MODERATE,
    val targetCalories: Int = 0,
    val proteinGrams: Int = 0,
    val fatGrams: Int = 0,
    val carbGrams: Int = 0,
    val goal: UserGoal = UserGoal.UTRZYMANIE,
    val weightHistory: List<WeightEntry> = emptyList(),
    val createdAt: Long = 0L // Timestamp utworzenia profilu
)
