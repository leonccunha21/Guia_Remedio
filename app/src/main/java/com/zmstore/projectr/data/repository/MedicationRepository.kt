package com.zmstore.projectr.data.repository

import com.zmstore.projectr.data.local.MedicationDao
import com.zmstore.projectr.data.model.CaregiverLink
import com.zmstore.projectr.data.model.DoseHistory
import com.zmstore.projectr.data.model.HealthEntry
import com.zmstore.projectr.data.model.Medication
import com.zmstore.projectr.data.model.Profile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val authRepository: AuthRepository? = null
) {
    private fun currentOwnerId(): String = authRepository?.currentUser?.uid ?: TEST_OWNER
    private val ownerFlow: Flow<String> = authRepository?.currentUserFlow
        ?.map { it?.uid ?: SIGNED_OUT_OWNER } ?: flowOf(TEST_OWNER)

    @OptIn(ExperimentalCoroutinesApi::class)
    val allMedications: Flow<List<Medication>> = ownerFlow.flatMapLatest(medicationDao::getAllMedications)
    @OptIn(ExperimentalCoroutinesApi::class)
    val allDoseHistory: Flow<List<DoseHistory>> = ownerFlow.flatMapLatest(medicationDao::getAllDoseHistory)
    @OptIn(ExperimentalCoroutinesApi::class)
    val allProfiles: Flow<List<Profile>> = ownerFlow.flatMapLatest(medicationDao::getAllProfiles)
    @OptIn(ExperimentalCoroutinesApi::class)
    val healthEntries: Flow<List<HealthEntry>> = ownerFlow.flatMapLatest(medicationDao::getHealthEntries)
    @OptIn(ExperimentalCoroutinesApi::class)
    val caregiverLinks: Flow<List<CaregiverLink>> = ownerFlow.flatMapLatest(medicationDao::getCaregiverLinks)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMedicationsByProfile(profileId: Int): Flow<List<Medication>> =
        ownerFlow.flatMapLatest { medicationDao.getMedicationsByProfile(it, profileId) }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getDoseHistoryByProfile(profileId: Int): Flow<List<DoseHistory>> =
        ownerFlow.flatMapLatest { medicationDao.getDoseHistoryByProfile(it, profileId) }

    suspend fun claimLegacyData() {
        val ownerId = currentOwnerId()
        if (ownerId != SIGNED_OUT_OWNER) medicationDao.claimLegacyData(ownerId)
    }

    suspend fun getMedicationById(id: Int): Medication? = medicationDao.getMedicationById(currentOwnerId(), id)
    suspend fun getActiveMedicationsOnce(): List<Medication> = medicationDao.getActiveMedicationsOnce(currentOwnerId())
    suspend fun getAllMedicationsOnce(): List<Medication> = medicationDao.getAllMedicationsOnce(currentOwnerId())
    suspend fun getAllProfilesOnce(): List<Profile> = medicationDao.getAllProfilesOnce(currentOwnerId())
    suspend fun getAllDoseHistoryOnce(): List<DoseHistory> = medicationDao.getAllDoseHistoryOnce(currentOwnerId())
    suspend fun getHealthEntriesOnce(): List<HealthEntry> = medicationDao.getHealthEntriesOnce(currentOwnerId())
    suspend fun getCaregiverLinksOnce(): List<CaregiverLink> = medicationDao.getCaregiverLinksOnce(currentOwnerId())

    suspend fun insertMedication(value: Medication): Long = medicationDao.insertMedication(value.copy(ownerId = currentOwnerId()))
    suspend fun updateMedication(value: Medication) = medicationDao.updateMedication(value.copy(ownerId = currentOwnerId()))
    suspend fun deleteMedication(value: Medication) = medicationDao.deleteMedication(value)
    suspend fun insertDoseHistory(value: DoseHistory) = medicationDao.insertDoseHistory(value.copy(ownerId = currentOwnerId()))
    suspend fun deleteDoseHistory(value: DoseHistory) = medicationDao.deleteDoseHistory(value)
    suspend fun deleteHistoryByMedication(id: Int) = medicationDao.deleteHistoryByMedication(currentOwnerId(), id)
    suspend fun clearAllHistory() = medicationDao.clearAllHistory(currentOwnerId())
    suspend fun insertProfile(value: Profile): Long = medicationDao.insertProfile(value.copy(ownerId = currentOwnerId()))
    suspend fun updateProfile(value: Profile) = medicationDao.updateProfile(value.copy(ownerId = currentOwnerId()))
    suspend fun deleteProfile(value: Profile) = medicationDao.deleteProfile(value)
    suspend fun insertHealthEntry(value: HealthEntry): Long = medicationDao.insertHealthEntry(value.copy(ownerId = currentOwnerId()))
    suspend fun deleteHealthEntry(value: HealthEntry) = medicationDao.deleteHealthEntry(value)
    suspend fun insertCaregiverLink(value: CaregiverLink): Long = medicationDao.insertCaregiverLink(value.copy(ownerId = currentOwnerId()))
    suspend fun deleteCaregiverLink(value: CaregiverLink) = medicationDao.deleteCaregiverLink(value)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun searchMedications(query: String): Flow<List<Medication>> = ownerFlow.flatMapLatest { medicationDao.searchMedications(it, query) }

    companion object {
        const val SIGNED_OUT_OWNER = "signed_out"
        const val TEST_OWNER = "test_owner"
    }
}
