package com.zmstore.projectr.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.zmstore.projectr.R
import kotlinx.coroutines.launch
import com.zmstore.projectr.data.model.Medication
import com.zmstore.projectr.data.model.Profile
import com.zmstore.projectr.ui.MainViewModel
import com.zmstore.projectr.ui.theme.*

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigateToCamera: () -> Unit,
    onNavigateToDetail: (Int, String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToMedicationList: () -> Unit,
    onNavigateToAlarms: () -> Unit
) {
    val medications by viewModel.medications.collectAsState()
    val userPrefs by viewModel.userPreferences.collectAsState()
    val profiles by viewModel.allProfiles.collectAsState()
    val selectedProfile by viewModel.selectedProfile.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var medicationToConfirm by remember { mutableStateOf<Medication?>(null) }
    var confirmationNote by remember { mutableStateOf("") }
    var showProfileMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        viewModel.updateSearchQuery("")
        viewModel.updateSelectedCategory("Todos")
        viewModel.stockAlert.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.streakAlert.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
        }
    }

    val speechRecognizerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            if (spokenText.isNotBlank()) {
                viewModel.confirmDoseByVoice(spokenText) { message, success ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            }
        }
    }

    if (medicationToConfirm != null) {
        AlertDialog(
            onDismissRequest = { medicationToConfirm = null; confirmationNote = "" },
            icon = { 
                Surface(
                    shape = CircleShape,
                    color = MedicleanTeal.copy(alpha = 0.1f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedicleanTeal, modifier = Modifier.size(32.dp))
                    }
                }
            },
            title = { 
                Text(
                    "Confirmar Dose", 
                    style = MaterialTheme.typography.headlineSmall, 
                    fontWeight = FontWeight.Black, 
                    color = MedicleanDarkGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Você tomou ${medicationToConfirm?.name}?", 
                        style = MaterialTheme.typography.bodyLarge,
                        color = MedicleanDarkGreen.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = confirmationNote,
                        onValueChange = { confirmationNote = it },
                        placeholder = { Text("Alguma observação? (ex: com água)", color = MedicleanDarkGreen.copy(alpha = 0.3f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MedicleanDarkGreen,
                            unfocusedTextColor = MedicleanDarkGreen,
                            focusedContainerColor = MedicleanMint.copy(alpha = 0.05f),
                            unfocusedContainerColor = MedicleanMint.copy(alpha = 0.05f),
                            focusedBorderColor = MedicleanTeal,
                            unfocusedBorderColor = Color.Transparent,
                            focusedLabelColor = MedicleanTeal,
                            unfocusedLabelColor = MedicleanDarkGreen.copy(alpha = 0.6f)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        medicationToConfirm?.let { med ->
                            viewModel.confirmDose(med.id, med.name, confirmationNote.ifBlank { null })
                        }
                        medicationToConfirm = null
                        confirmationNote = ""
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicleanTeal)
                ) {
                    Text("SIM, ESTÁ TOMADO", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { medicationToConfirm = null; confirmationNote = "" },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("CANCELAR", color = MedicleanDarkGreen.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(32.dp),
            tonalElevation = 0.dp
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToDetail(-1, "") },
                containerColor = MedicleanTeal,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(bottom = 60.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.home_fab_add_content),
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .background(Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val adherence by viewModel.todaysAdherence.collectAsState()

                HomeHeader(
                    selectedProfile = selectedProfile,
                    onOpenDrawer = onOpenDrawer,
                    onProfileClick = { showProfileMenu = true },
                    emergencyContact = userPrefs.emergencyContact,
                    userName = userPrefs.name
                )

                // Resumo do Dia - Foco em acessibilidade
                HealthDashboard(adherence = adherence)

                // Botão de ação principal - Grande e fácil de ver
                Button(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToDetail(-1, "") 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .height(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicleanTeal)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.home_btn_add_big), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                }

                if (medications.isEmpty()) {
                    EmptyState(false)
                } else {
                    Text(
                        text = stringResource(R.string.home_next_medications),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = MedicleanDarkGreen.copy(alpha = 0.6f)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(medications, key = { it.id }) { medication ->
                            MedicationCard(
                                medication = medication,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                countdown = viewModel.getMedicationCountdown(medication),
                                onConfirm = { medicationToConfirm = it },
                                onEdit = { onNavigateToDetail(it.id, it.name) },
                                onDelete = { viewModel.deleteMedication(it) }
                            )
                        }
                    }
                }
            }

            if (showProfileMenu) {
                ProfileSelectionOverlay(
                    profiles = profiles,
                    onProfileSelected = {
                        viewModel.selectProfile(it)
                        showProfileMenu = false
                    },
                    onDismiss = { showProfileMenu = false }
                )
            }
        }
    }
}

@Composable
fun HealthDashboard(adherence: Pair<Int, Int>) {
    val (taken, total) = adherence
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MedicleanMint,
        border = BorderStroke(2.dp, MedicleanTeal.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    stringResource(R.string.home_doses_today),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MedicleanDarkGreen.copy(alpha = 0.6f)
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = taken.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = MedicleanTeal
                    )
                    Text(
                        text = " / $total",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MedicleanDarkGreen.copy(alpha = 0.4f),
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                }
            }
            
            if (total > 0 && taken == total) {
                Surface(
                    shape = CircleShape,
                    color = MedicleanTeal,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (false) Color(0xFF1E2A28) else MedicleanWhite,
        modifier = modifier.height(70.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                color = MedicleanTeal.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MedicleanTeal, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                label, 
                fontWeight = FontWeight.Bold, 
                color = MedicleanDarkGreen, 
                fontSize = 13.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun HomeHeader(
    selectedProfile: Profile?,
    onOpenDrawer: () -> Unit,
    onProfileClick: () -> Unit,
    emergencyContact: String? = null,
    userName: String = ""
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onOpenDrawer()
            },
            modifier = Modifier
                .background(if (false) Color.White.copy(alpha = 0.08f) else Color.White, RoundedCornerShape(16.dp))
                .size(48.dp)
                .border(1.dp, if (false) Color.White.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(16.dp))
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MedicleanTeal, modifier = Modifier.size(26.dp))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val displayName = if (!selectedProfile?.name.isNullOrBlank() && selectedProfile.name != "Meu Perfil") {
                selectedProfile.name
            } else if (userName.isNotBlank()) {
                userName
            } else {
                "Usuário"
            }

            Text(
                text = "Olá, $displayName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MedicleanDarkGreen
            )
            
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (!emergencyContact.isNullOrBlank()) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$emergencyContact"))
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Cadastre um contato de emergência no seu perfil primeiro!", Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MedicleanError),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.Emergency, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("SOS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.White)
            }
        }

        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onProfileClick()
            },
            shape = RoundedCornerShape(16.dp),
            color = if (false) Color.White.copy(alpha = 0.08f) else Color.White,
            modifier = Modifier.size(48.dp),
            shadowElevation = 4.dp,
            border = BorderStroke(2.dp, selectedProfile?.let { Color(it.color) } ?: MedicleanTeal)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = selectedProfile?.let { Color(it.color) } ?: MedicleanTeal,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("O que você procura?", color = MedicleanDarkGreen.copy(alpha = 0.4f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MedicleanTeal) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MedicleanDarkGreen,
                unfocusedTextColor = MedicleanDarkGreen,
                focusedContainerColor = if (false) Color(0xFF1A2624) else MedicleanWhite,
                unfocusedContainerColor = if (false) Color(0xFF1A2624) else MedicleanWhite,
                focusedBorderColor = MedicleanTeal,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = MedicleanTeal
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val categories = listOf("Todos", "Uso Contínuo", "Vitamina")
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategoryChange(category) },
                    label = { 
                        Text(
                            category, 
                            fontWeight = if(selectedCategory == category) FontWeight.Black else FontWeight.Medium,
                            fontSize = 13.sp
                        ) 
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MedicleanTeal,
                        selectedLabelColor = Color.White,
                        containerColor = if (false) Color(0xFF1A1A1A) else MedicleanWhite,
                        labelColor = MedicleanDarkGreen
                    ),
                    border = null
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MedicationCard(
    medication: Medication,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    countdown: String,
    onConfirm: (Medication) -> Unit,
    onEdit: (Medication) -> Unit,
    onDelete: (Medication) -> Unit
) {
    with(sharedTransitionScope) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEdit(medication) },
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp,
            border = BorderStroke(2.dp, Color(0xFFF0F0F0))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(medication.iconColor).copy(alpha = 0.1f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (medication.imageUrl != null) {
                                val painter = rememberAsyncImagePainter(medication.imageUrl)
                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = when(medication.iconType) {
                                        "capsule" -> Icons.Default.Adjust
                                        "drops" -> Icons.Default.WaterDrop
                                        "liquid" -> Icons.Default.Vaccines
                                        else -> Icons.Default.Medication
                                    },
                                    contentDescription = null,
                                    tint = Color(medication.iconColor),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = medication.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MedicleanDarkGreen
                        )
                        Text(
                            text = medication.dosage,
                            style = MaterialTheme.typography.titleMedium,
                            color = MedicleanDarkGreen.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        val isLate = countdown == "Atrasado"
                        Text(
                            text = if (isLate) stringResource(R.string.home_status_late) else stringResource(R.string.home_status_next),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = if (isLate) MedicleanError else MedicleanDarkGreen.copy(alpha = 0.4f)
                        )
                        Text(
                            text = countdown,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = if (isLate) MedicleanError else MedicleanTeal
                        )
                    }

                    if (medication.isActive) {
                        val haptic = LocalHapticFeedback.current
                        Button(
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onConfirm(medication) 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicleanTeal),
                            modifier = Modifier.height(60.dp),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Text("TOMAR AGORA", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(isFiltering: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            color = MedicleanTeal.copy(alpha = 0.05f),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isFiltering) Icons.Default.SearchOff else Icons.Default.MedicalServices,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MedicleanTeal.copy(alpha = 0.3f)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (isFiltering) "Sem resultados" else "Sua jornada começa aqui",
            style = MaterialTheme.typography.headlineSmall,
            color = MedicleanDarkGreen,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isFiltering) "Tente ajustar sua busca" else "Adicione seu primeiro remédio para começar o acompanhamento.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MedicleanDarkGreen.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ProfileSelectionOverlay(
    profiles: List<Profile>,
    onProfileSelected: (Profile) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = if (false) Color(0xFF121A18) else MedicleanWhite,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), CircleShape)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Selecione o Perfil",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MedicleanDarkGreen
                )
                Spacer(modifier = Modifier.height(20.dp))
                profiles.forEach { profile ->
                    Surface(
                        onClick = { onProfileSelected(profile) },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(16.dp),
                                color = Color(profile.color),
                                shape = CircleShape,
                                border = BorderStroke(2.dp, Color.White)
                            ) {}
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                profile.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MedicleanDarkGreen
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
