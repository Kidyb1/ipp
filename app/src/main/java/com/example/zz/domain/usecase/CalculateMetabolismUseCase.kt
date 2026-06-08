package com.example.zz.domain.usecase

import com.example.zz.domain.model.ActivityLevel
import com.example.zz.domain.model.Gender
import com.example.zz.domain.model.UserGoal
import com.example.zz.domain.model.UserProfile
import kotlin.math.roundToInt

/**
 * Use case obliczający zapotrzebowanie metaboliczne na podstawie wzoru Mifflin-St Jeor.
 * Oblicza BMR, TDEE oraz sugerowane makroskładniki.
 */
class CalculateMetabolismUseCase {

    operator fun invoke(profile: UserProfile): UserProfile {
        // 1. Obliczanie BMR (Podstawowa przemiana materii)
        val bmr = if (profile.gender == Gender.MALE) {
            (10 * profile.currentWeight) + (6.25 * profile.height) - (5 * profile.age) + 5
        } else {
            (10 * profile.currentWeight) + (6.25 * profile.height) - (5 * profile.age) - 161
        }

        // 2. TDEE (Całkowite zapotrzebowanie)
        val tdee = (bmr * profile.activityLevel.factor).roundToInt()

        // 3. Określenie celu (Goal)
        val goal = when {
            profile.currentWeight > profile.targetWeight + 0.5 -> UserGoal.REDUKCJA
            profile.currentWeight < profile.targetWeight - 0.5 -> UserGoal.MASA
            else -> UserGoal.UTRZYMANIE
        }

        // 4. Korekta kalorii pod cel z uwzględnieniem tempa
        val dailyAdjustment = (profile.dietPace.weeklyChangeKg * 7700 / 7).roundToInt()
        
        val targetCalories = when (goal) {
            UserGoal.REDUKCJA -> tdee - dailyAdjustment
            UserGoal.MASA -> tdee + (dailyAdjustment * 0.5).roundToInt() // Masa zwykle wolniej niż redukcja tłuszczu
            UserGoal.UTRZYMANIE -> tdee
        }

        // 5. Obliczanie makroskładników
        // Białko: 2g na kg masy ciała (dla sportowców/osób aktywnych)
        val proteinGrams = (profile.currentWeight * 2.0).roundToInt()
        val proteinKcal = proteinGrams * 4

        // Tłuszcze: 25% całkowitego zapotrzebowania
        val fatKcal = (targetCalories * 0.25).roundToInt()
        val fatGrams = fatKcal / 9

        // Węglowodany: Reszta kalorii
        val carbKcal = targetCalories - proteinKcal - fatKcal
        val carbGrams = carbKcal / 4

        return profile.copy(
            targetCalories = targetCalories,
            proteinGrams = proteinGrams,
            fatGrams = fatGrams,
            carbGrams = carbGrams,
            goal = goal
        )
    }
}
