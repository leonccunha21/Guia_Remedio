package com.zmstore.projectr.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "dose_history", indices = [Index("ownerId")])
data class DoseHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicationId: Int = 0,
    val medicationName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null,
    val ownerId: String = "legacy",
    val status: String = DoseStatus.TAKEN.name,
    val scheduledTimestamp: Long = 0L
)

enum class DoseStatus { TAKEN, LATE, SKIPPED, SNOOZED }
