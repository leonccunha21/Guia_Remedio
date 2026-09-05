package com.zmstore.projectr.ui.caregiver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zmstore.projectr.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val links by viewModel.caregiverLinks.collectAsState()
    val alerts by viewModel.caregiverAlerts.collectAsState()
    val user by viewModel.currentUser.collectAsState(initial = null)
    var name by remember { mutableStateOf("") }
    var uid by remember { mutableStateOf("") }
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("CUIDADORES", fontWeight = FontWeight.Black) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") } }) }, containerColor = Color.White) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Seu código: ${user?.uid ?: "entre com uma conta"}", style = MaterialTheme.typography.bodySmall) }
            item { Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Adicionar cuidador", fontWeight = FontWeight.Bold); OutlinedTextField(name, { name = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(uid, { uid = it }, label = { Text("Código da conta do cuidador") }, modifier = Modifier.fillMaxWidth()); Button(onClick = { if (name.isNotBlank() && uid.isNotBlank()) { viewModel.insertCaregiver(name, uid); name = ""; uid = "" } }, modifier = Modifier.fillMaxWidth()) { Text("VINCULAR") } } } }
            items(links, key = { it.id }) { link -> Card { Row(Modifier.fillMaxWidth().padding(14.dp)) { Column(Modifier.weight(1f)) { Text(link.caregiverName, fontWeight = FontWeight.Bold); Text(link.caregiverUid, style = MaterialTheme.typography.bodySmall) }; TextButton(onClick = { viewModel.deleteCaregiver(link) }) { Text("Remover") } } } }
            item { HorizontalDivider(); Text("Alertas recebidos", fontWeight = FontWeight.Bold) }
            if (alerts.isEmpty()) item { Text("Nenhum alerta recebido.", color = Color.Gray) }
            items(alerts) { alert -> Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E5))) { Column(Modifier.padding(14.dp)) { Text(alert.patientName, fontWeight = FontWeight.Bold); Text("${alert.medicationName}: ${alert.status}") } } }
        }
    }
}
