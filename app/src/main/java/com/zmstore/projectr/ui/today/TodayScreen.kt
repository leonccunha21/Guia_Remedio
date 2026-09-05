package com.zmstore.projectr.ui.today

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zmstore.projectr.data.model.DoseStatus
import com.zmstore.projectr.data.model.Medication
import com.zmstore.projectr.ui.MainViewModel
import com.zmstore.projectr.ui.theme.MedicleanDarkGreen
import com.zmstore.projectr.ui.theme.MedicleanTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val medications by viewModel.medications.collectAsState()
    val history by viewModel.doseHistory.collectAsState()
    val now = System.currentTimeMillis()
    val start = remember { java.util.Calendar.getInstance().apply { set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis }
    val active = medications.filter { it.isActive && (it.treatmentStartDate == 0L || it.treatmentStartDate <= now) && (it.treatmentEndDate == 0L || it.treatmentEndDate >= start) }
    val completedIds = history.filter { it.timestamp >= start && it.status in listOf(DoseStatus.TAKEN.name, DoseStatus.LATE.name, DoseStatus.SKIPPED.name) }.map { it.medicationId }.toSet()

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("HOJE", fontWeight = FontWeight.Black) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") } }) }, containerColor = Color.White) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Linha do tempo das doses", style = MaterialTheme.typography.titleMedium, color = MedicleanDarkGreen, fontWeight = FontWeight.Bold) }
            if (active.isEmpty()) item { Text("Nenhuma dose programada para hoje.", modifier = Modifier.padding(24.dp), color = Color.Gray) }
            items(active, key = { it.id }) { medication ->
                TodayDoseCard(medication, medication.id in completedIds, viewModel)
            }
        }
    }
}

@Composable
private fun TodayDoseCard(medication: Medication, completed: Boolean, viewModel: MainViewModel) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if (completed) Color(0xFFE9F7F4) else Color(0xFFF7F9F8))) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = MedicleanTeal)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(medication.name, fontWeight = FontWeight.Black, color = MedicleanDarkGreen)
                    Text(medication.customTimes?.takeIf { it.isNotBlank() } ?: "A cada ${medication.intervalHours}h", style = MaterialTheme.typography.bodySmall)
                }
                if (completed) Text("REGISTRADO", color = MedicleanTeal, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
            }
            if (!completed) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.registerDose(medication, DoseStatus.TAKEN) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MedicleanTeal)) { Text("Tomei") }
                OutlinedButton(onClick = { viewModel.registerDose(medication, DoseStatus.SNOOZED) }, modifier = Modifier.weight(1f)) { Text("Adiar") }
                TextButton(onClick = { viewModel.registerDose(medication, DoseStatus.SKIPPED) }, modifier = Modifier.weight(1f)) { Text("Ignorar") }
            }
        }
    }
}
