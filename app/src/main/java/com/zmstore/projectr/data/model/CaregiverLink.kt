package com.zmstore.projectr.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "caregiver_links", indices = [androidx.room.Index("ownerId")])
data class CaregiverLink(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerId: String = "legacy",
    val caregiverUid: String = "",
    val caregiverName: String = "",
    val notifyMissedDoses: Boolean = true,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
