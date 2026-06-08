package com.example.zz.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZonedDateTime

data class DailyNutrition(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val carbs: Double = 0.0
)

class HealthConnectManager(private val context: Context) {

    private val healthConnectClient by lazy {
        if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else null
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(NutritionRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class)
    )

    suspend fun hasAllPermissions(): Boolean {
        return healthConnectClient?.permissionController?.getGrantedPermissions()?.containsAll(permissions) == true
    }

    suspend fun readDailyNutrition(date: ZonedDateTime): DailyNutrition {
        val client = healthConnectClient ?: return DailyNutrition()
        
        val startOfDay = date.toLocalDate().atStartOfDay(date.zone).toInstant()
        val endOfDay = date.toLocalDate().atTime(23, 59, 59).atZone(date.zone).toInstant()

        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = NutritionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
            )
        )
        
        var totalCalories = 0.0
        var totalProtein = 0.0
        var totalFat = 0.0
        var totalCarbs = 0.0

        response.records.forEach { record ->
            totalCalories += record.energy?.inKilocalories ?: 0.0
            totalProtein += record.protein?.inGrams ?: 0.0
            totalFat += record.totalFat?.inGrams ?: 0.0
            totalCarbs += record.totalCarbohydrate?.inGrams ?: 0.0
        }

        return DailyNutrition(
            calories = totalCalories,
            protein = totalProtein,
            fat = totalFat,
            carbs = totalCarbs
        )
    }

    suspend fun readDailyCalories(date: ZonedDateTime): Double {
        return readDailyNutrition(date).calories
    }

    suspend fun readLatestWeight(): Double? {
        val client = healthConnectClient ?: return null
        
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = WeightRecord::class,
                timeRangeFilter = TimeRangeFilter.after(Instant.now().minusSeconds(60 * 60 * 24 * 30)) // Last 30 days
            )
        )
        
        return response.records.lastOrNull()?.weight?.inKilograms
    }
}
