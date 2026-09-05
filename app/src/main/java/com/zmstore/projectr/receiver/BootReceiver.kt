package com.zmstore.projectr.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zmstore.projectr.data.repository.MedicationRepository
import com.zmstore.projectr.util.MedicationAlarmHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Restaura lembretes depois que o aparelho reinicia ou o aplicativo é atualizado. */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: MedicationRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in supportedActions) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.getActiveMedicationsOnce().forEach { medication ->
                    MedicationAlarmHelper.scheduleAlarm(context, medication)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        val supportedActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED
        )
    }
}
