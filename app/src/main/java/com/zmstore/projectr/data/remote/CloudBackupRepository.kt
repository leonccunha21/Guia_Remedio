package com.zmstore.projectr.data.remote

import com.zmstore.projectr.data.model.DoseHistory
import com.zmstore.projectr.data.model.Medication
import com.zmstore.projectr.data.model.Profile
import com.zmstore.projectr.data.model.HealthEntry
import com.zmstore.projectr.data.model.CaregiverLink
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

import com.google.firebase.database.FirebaseDatabase
import com.zmstore.projectr.data.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class CloudBackupRepository @Inject constructor(
    private val authRepository: AuthRepository
) {
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun syncMedications(medications: List<Medication>) {
        val user = authRepository.currentUser ?: return
        if (user.isAnonymous) return
        
        try {
            database.child("users").child(user.uid).child("medications").setValue(medications).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncProfiles(profiles: List<Profile>) {
        val user = authRepository.currentUser ?: return
        if (user.isAnonymous) return
        
        try {
            database.child("users").child(user.uid).child("profiles").setValue(profiles).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncHistory(history: List<DoseHistory>) {
        val user = authRepository.currentUser ?: return
        if (user.isAnonymous) return
        
        try {
            database.child("users").child(user.uid).child("history").setValue(history).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncHealth(entries: List<HealthEntry>) = syncNode("health", entries)
    suspend fun syncCaregivers(links: List<CaregiverLink>) = syncNode("caregivers", links)

    private suspend fun syncNode(node: String, value: Any) {
        val user = authRepository.currentUser ?: return
        if (user.isAnonymous) return
        try {
            database.child("users").child(user.uid).child(node).setValue(value).await()
        } catch (_: Exception) {
            // O dado continua seguro no Room e será sincronizado na próxima alteração.
        }
    }

    suspend fun deleteUserData(): Result<Unit> {
        val user = authRepository.currentUser ?: return Result.failure(IllegalStateException("Usuário não autenticado"))
        if (user.isAnonymous) return Result.success(Unit)
        return try {
            database.child("users").child(user.uid).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreAll(): Result<CloudSnapshot> {
        val user = authRepository.currentUser
            ?: return Result.failure(IllegalStateException("Usuário não autenticado"))
        if (user.isAnonymous) return Result.success(CloudSnapshot())
        return try {
            val root = database.child("users").child(user.uid).get().await()
            Result.success(
                CloudSnapshot(
                    medications = root.child("medications").children.mapNotNull { it.getValue(Medication::class.java) },
                    profiles = root.child("profiles").children.mapNotNull { it.getValue(Profile::class.java) },
                    history = root.child("history").children.mapNotNull { it.getValue(DoseHistory::class.java) },
                    health = root.child("health").children.mapNotNull { it.getValue(HealthEntry::class.java) },
                    caregivers = root.child("caregivers").children.mapNotNull { it.getValue(CaregiverLink::class.java) }
                )
            )
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun notifyCaregivers(links: List<CaregiverLink>, medicationName: String, status: String) {
        val patient = authRepository.currentUser ?: return
        if (patient.isAnonymous) return
        links.filter { it.isActive && it.notifyMissedDoses && it.caregiverUid.isNotBlank() }.forEach { link ->
            try {
                database.child("caregiverAlerts").child(link.caregiverUid).push().setValue(
                    CaregiverAlert(
                        patientUid = patient.uid,
                        patientName = patient.displayName ?: patient.email ?: "Paciente",
                        medicationName = medicationName,
                        status = status
                    )
                ).await()
            } catch (_: Exception) { }
        }
    }

    fun caregiverAlerts(): Flow<List<CaregiverAlert>> = callbackFlow {
        val uid = authRepository.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val ref = database.child("caregiverAlerts").child(uid)
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                trySend(snapshot.children.mapNotNull { it.getValue(CaregiverAlert::class.java) }.sortedByDescending { it.timestamp })
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) { trySend(emptyList()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getCloudStatus(): Flow<String> = flow {
        val user = authRepository.currentUser
        if (user == null || user.isAnonymous) {
            emit("Backup na Nuvem Desativado (Modo Convidado)")
        } else {
            emit("Sincronizando com a Nuvem...")
        }
    }

    suspend fun uploadImage(filename: String, base64: String) {
        val user = authRepository.currentUser ?: return
        if (user.isAnonymous) return
        try {
            database.child("users").child(user.uid).child("images").child(filename).setValue(base64).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class CloudSnapshot(
    val medications: List<Medication> = emptyList(),
    val profiles: List<Profile> = emptyList(),
    val history: List<DoseHistory> = emptyList(),
    val health: List<HealthEntry> = emptyList(),
    val caregivers: List<CaregiverLink> = emptyList()
)

data class CaregiverAlert(
    val patientUid: String = "",
    val patientName: String = "",
    val medicationName: String = "",
    val status: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
