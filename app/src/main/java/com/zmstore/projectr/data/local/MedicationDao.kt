package com.zmstore.projectr.data.local

import androidx.room.*
import com.zmstore.projectr.data.model.DoseHistory
import com.zmstore.projectr.data.model.Medication
import com.zmstore.projectr.data.model.Profile
import com.zmstore.projectr.data.model.HealthEntry
import com.zmstore.projectr.data.model.CaregiverLink
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE ownerId = :ownerId ORDER BY name ASC")
    fun getAllMedications(ownerId: String): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE ownerId = :ownerId AND isActive = 1")
    suspend fun getActiveMedicationsOnce(ownerId: String): List<Medication>

    @Query("SELECT * FROM medications WHERE ownerId = :ownerId ORDER BY name ASC")
    suspend fun getAllMedicationsOnce(ownerId: String): List<Medication>

    @Query("SELECT * FROM medications WHERE ownerId = :ownerId AND profileId = :profileId ORDER BY name ASC")
    fun getMedicationsByProfile(ownerId: String, profileId: Int): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE ownerId = :ownerId AND id = :id")
    suspend fun getMedicationById(ownerId: String, id: Int): Medication?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)

    // Profiles
    @Query("SELECT * FROM profiles WHERE ownerId = :ownerId ORDER BY isDefault DESC, name ASC")
    fun getAllProfiles(ownerId: String): Flow<List<Profile>>

    @Query("SELECT * FROM profiles WHERE ownerId = :ownerId ORDER BY isDefault DESC, name ASC")
    suspend fun getAllProfilesOnce(ownerId: String): List<Profile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile): Long

    @Update
    suspend fun updateProfile(profile: Profile)

    @Delete
    suspend fun deleteProfile(profile: Profile)

    // History
    @Query("SELECT * FROM dose_history WHERE ownerId = :ownerId ORDER BY timestamp DESC")
    fun getAllDoseHistory(ownerId: String): Flow<List<DoseHistory>>

    @Query("SELECT * FROM dose_history WHERE ownerId = :ownerId ORDER BY timestamp DESC")
    suspend fun getAllDoseHistoryOnce(ownerId: String): List<DoseHistory>

    @Query("SELECT * FROM dose_history WHERE ownerId = :ownerId AND medicationId IN (SELECT id FROM medications WHERE ownerId = :ownerId AND profileId = :profileId) ORDER BY timestamp DESC")
    fun getDoseHistoryByProfile(ownerId: String, profileId: Int): Flow<List<DoseHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseHistory(doseHistory: DoseHistory)

    @Delete
    suspend fun deleteDoseHistory(doseHistory: DoseHistory)

    @Query("DELETE FROM dose_history WHERE ownerId = :ownerId AND medicationId = :medicationId")
    suspend fun deleteHistoryByMedication(ownerId: String, medicationId: Int)

    @Query("DELETE FROM dose_history WHERE ownerId = :ownerId")
    suspend fun clearAllHistory(ownerId: String)

    @Query("SELECT * FROM medications WHERE ownerId = :ownerId AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchMedications(ownerId: String, query: String): Flow<List<Medication>>

    @Query("UPDATE medications SET ownerId = :ownerId WHERE ownerId = 'legacy'")
    suspend fun claimLegacyMedications(ownerId: String)

    @Query("UPDATE dose_history SET ownerId = :ownerId WHERE ownerId = 'legacy'")
    suspend fun claimLegacyHistory(ownerId: String)

    @Query("UPDATE profiles SET ownerId = :ownerId WHERE ownerId = 'legacy'")
    suspend fun claimLegacyProfiles(ownerId: String)

    @Transaction
    suspend fun claimLegacyData(ownerId: String) {
        claimLegacyMedications(ownerId)
        claimLegacyHistory(ownerId)
        claimLegacyProfiles(ownerId)
    }

    @Query("DELETE FROM medications WHERE ownerId = :ownerId")
    suspend fun deleteAllMedications(ownerId: String)

    @Query("DELETE FROM dose_history WHERE ownerId = :ownerId")
    suspend fun deleteAllHistory(ownerId: String)

    @Query("DELETE FROM profiles WHERE ownerId = :ownerId")
    suspend fun deleteAllProfiles(ownerId: String)

    @Query("DELETE FROM health_entries WHERE ownerId = :ownerId")
    suspend fun deleteAllHealthEntries(ownerId: String)

    @Query("DELETE FROM caregiver_links WHERE ownerId = :ownerId")
    suspend fun deleteAllCaregiverLinks(ownerId: String)

    @Transaction
    suspend fun wipeUserData(ownerId: String) {
        deleteAllMedications(ownerId)
        deleteAllHistory(ownerId)
        deleteAllProfiles(ownerId)
        deleteAllHealthEntries(ownerId)
        deleteAllCaregiverLinks(ownerId)
    }

    @Query("SELECT * FROM health_entries WHERE ownerId = :ownerId ORDER BY timestamp DESC")
    fun getHealthEntries(ownerId: String): Flow<List<HealthEntry>>

    @Query("SELECT * FROM health_entries WHERE ownerId = :ownerId ORDER BY timestamp DESC")
    suspend fun getHealthEntriesOnce(ownerId: String): List<HealthEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthEntry(entry: HealthEntry): Long

    @Delete
    suspend fun deleteHealthEntry(entry: HealthEntry)

    @Query("SELECT * FROM caregiver_links WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getCaregiverLinks(ownerId: String): Flow<List<CaregiverLink>>

    @Query("SELECT * FROM caregiver_links WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    suspend fun getCaregiverLinksOnce(ownerId: String): List<CaregiverLink>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaregiverLink(link: CaregiverLink): Long

    @Delete
    suspend fun deleteCaregiverLink(link: CaregiverLink)
}
