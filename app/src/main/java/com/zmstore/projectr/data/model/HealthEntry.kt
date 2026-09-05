package com.zmstore.projectr.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_entries", indices = [androidx.room.Index("ownerId")])
data class HealthEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerId: String = "legacy",
    val profileId: Int = 0,
    val type: String = HealthEntryType.SYMPTOM.name,
    val primaryValue: String = "",
    val secondaryValue: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class HealthEntryType { SYMPTOM, BLOOD_PRESSURE, GLUCOSE, WELLBEING }
