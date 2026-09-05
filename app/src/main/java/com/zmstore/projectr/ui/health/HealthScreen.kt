package com.zmstore.projectr.ui.health

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zmstore.projectr.data.model.HealthEntry
import com.zmstore.projectr.data.model.HealthEntryType
import com.zmstore.projectr.ui.MainViewModel
import com.zmstore.projectr.ui.theme.MedicleanTeal
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val entries by viewModel.healthEntries.collectAsState()
    var type by remember { mutableStateOf(HealthEntryType.SYMPTOM) }
    var value by remember { mutableStateOf("") }
    var secondary by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("SAÚDE", fontWeight = FontWeight.Black) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") } }) }, containerColor = Color.White) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(typeLabel(type), {}, readOnly = true, label = { Text("Tipo de registro") }, modifier = Modifier.fillMaxWidth().menuAnchor())
                        ExposedDropdownMenu(expanded, { expanded = false }) { HealthEntryType.entries.forEach { item -> DropdownMenuItem({ Text(typeLabel(item)) }, onClick = { type = item; expanded = false }) } }
                    }
                    OutlinedTextField(value, { value = it }, label = { Text(if (type == HealthEntryType.BLOOD_PRESSURE) "Pressão sistólica" else "Valor ou sintoma") }, modifier = Modifier.fillMaxWidth())
                    if (type == HealthEntryType.BLOOD_PRESSURE) OutlinedTextField(secondary, { secondary = it }, label = { Text("Pressão diastólica") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(note, { note = it }, label = { Text("Observação") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { if (value.isNotBlank()) { viewModel.insertHealthEntry(HealthEntry(type = type.name, primaryValue = value.trim(), secondaryValue = secondary.trim(), note = note.trim())); value = ""; secondary = ""; note = "" } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MedicleanTeal)) { Text("SALVAR REGISTRO") }
                } }
            }
            items(entries, key = { it.id }) { entry ->
                Card { Row(Modifier.fillMaxWidth().padding(14.dp)) { Column(Modifier.weight(1f)) { Text(typeLabel(runCatching { HealthEntryType.valueOf(entry.type) }.getOrDefault(HealthEntryType.SYMPTOM)), fontWeight = FontWeight.Bold); Text(listOf(entry.primaryValue, entry.secondaryValue).filter { it.isNotBlank() }.joinToString(" / ")); if (entry.note.isNotBlank()) Text(entry.note, style = MaterialTheme.typography.bodySmall); Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(entry.timestamp)), style = MaterialTheme.typography.labelSmall, color = Color.Gray) }; TextButton(onClick = { viewModel.deleteHealthEntry(entry) }) { Text("Excluir") } } }
            }
        }
    }
}

private fun typeLabel(type: HealthEntryType) = when(type) { HealthEntryType.SYMPTOM -> "Sintoma"; HealthEntryType.BLOOD_PRESSURE -> "Pressão arterial"; HealthEntryType.GLUCOSE -> "Glicemia"; HealthEntryType.WELLBEING -> "Bem-estar" }
