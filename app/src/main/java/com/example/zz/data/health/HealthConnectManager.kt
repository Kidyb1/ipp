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

    suspend fun readDailyCalories(date: ZonedDateTime): Double {
        val client = healthConnectClient ?: return 0.0
        
        val startOfDay = date.toLocalDate().atStartOfDay(date.zone).toInstant()
        val endOfDay = date.toLocalDate().atTime(23, 59, 59).atZone(date.zone).toInstant()

        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = NutritionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
            )
        )
        
        return response.records.sumOf { it.energy?.inKilocalories ?: 0.0 }
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
