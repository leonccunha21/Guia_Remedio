package com.zmstore.projectr.ui.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zmstore.projectr.R
import com.zmstore.projectr.ui.MainViewModel
import com.zmstore.projectr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val userPrefs by viewModel.userPreferences.collectAsState()
    val context = LocalContext.current
    
    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var emergency by remember { mutableStateOf("") }
    var geminiApiKey by remember { mutableStateOf("") }
    var isBiometricEnabled by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Sync local state when prefs load
    LaunchedEffect(userPrefs) {
        name = userPrefs.name
        weight = userPrefs.weight
        height = userPrefs.height
        emergency = userPrefs.emergencyContact
        geminiApiKey = userPrefs.geminiApiKey
        isBiometricEnabled = userPrefs.isBiometricEnabled
    }

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.White,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "MEUS DADOS", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp), 
                        color = MedicleanDarkGreen
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(8.dp).background(if (false) Color.White.copy(alpha = 0.1f) else Color.White, RoundedCornerShape(12.dp)).size(40.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MedicleanTeal)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Personal Info Section
            ProfileSectionHeader("INFORMAÇÕES PESSOAIS", Icons.Default.Person)
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFE8ECEB))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PremiumTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nome Completo",
                        icon = Icons.Default.Person
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PremiumTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = "Peso (kg)",
                            modifier = Modifier.weight(1f),
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                        PremiumTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = "Altura (cm)",
                            modifier = Modifier.weight(1f),
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    }
                }
            }

            // Emergency Section
            ProfileSectionHeader("SEGURANÇA E EMERGÊNCIA", Icons.Default.Emergency)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MedicleanTeal.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, MedicleanTeal.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PremiumTextField(
                        value = emergency,
                        onValueChange = { emergency = it },
                        label = "Contato de Emergência",
                        icon = Icons.Default.Emergency,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                    )
                    
                    Text(
                        text = "Este contato será exibido em situações críticas para facilitar o socorro médico.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedicleanDarkGreen.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // AI Settings Section
            ProfileSectionHeader("INTELIGÊNCIA ARTIFICIAL", Icons.Default.AutoAwesome)
 
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFE8ECEB))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PremiumTextField(
                        value = geminiApiKey,
                        onValueChange = { geminiApiKey = it },
                        label = "Chave de API Gemini",
                        icon = Icons.Default.VpnKey,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                    )
                    
                    Text(
                        text = "A chave de API permite que o assistente analise seus medicamentos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedicleanDarkGreen.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                    
                    androidx.compose.material3.HorizontalDivider(color = MedicleanTeal.copy(alpha = 0.1f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Proteção Biométrica",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Black,
                                color = MedicleanDarkGreen
                            )
                            Text(
                                text = "Exigir digital ao abrir o app",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedicleanDarkGreen.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = { isBiometricEnabled = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = MedicleanTeal)
                        )
                    }
                }
            }

            // Cloud Backup Section (Premium info)
            ProfileSectionHeader("BACKUP NA NUVEM", Icons.Default.Cloud)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFE8ECEB))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (userPrefs.isPremium) "Seu plano é VIP — seus dados são sincronizados na nuvem de forma automática." else "Usuários gratuitos têm backup apenas local. Para salvar na nuvem, ative o plano VIP.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedicleanDarkGreen.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )

                    if (!userPrefs.isPremium) {
                        Text(
                            text = "Observação: o Firebase gratuito é usado. Imagens e backups na nuvem dependem de autenticação e podem ter limites. Ao não ser VIP, seus dados podem não ser restaurados se o app for removido.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedicleanDarkGreen.copy(alpha = 0.5f)
                        )

                        val activity = (context as? android.app.Activity)
                        val billingManager = remember { com.zmstore.projectr.billing.BillingManager(context, viewModel) }
                        DisposableEffect(billingManager) {
                            onDispose { billingManager.disconnect() }
                        }

                        Button(
                            onClick = { activity?.let { act -> billingManager.launchPurchase(act, com.zmstore.projectr.billing.BillingManager.SKU_MONTHLY) } },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicleanTeal),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Ativar VIP (Assinatura Mensal)", fontWeight = FontWeight.Black)
                        }

                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = { activity?.let { act -> billingManager.launchPurchase(act, com.zmstore.projectr.billing.BillingManager.SKU_YEARLY) } },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicleanTeal),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Ativar VIP (Assinatura Anual)", fontWeight = FontWeight.Black)
                        }

                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = { activity?.let { act -> billingManager.launchPurchase(act, com.zmstore.projectr.billing.BillingManager.SKU_LIFETIME) } },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicleanTeal),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Ativar VIP (Acesso Vitalício)", fontWeight = FontWeight.Black)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.setPremium(false) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MedicleanTeal)
                        ) {
                            Icon(Icons.Default.StarBorder, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Desativar VIP (Simular)", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Button(
                onClick = { 
                    viewModel.updateProfile(name, weight, height, emergency, geminiApiKey, isBiometricEnabled)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MedicleanTeal)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("SALVAR ALTERAÇÕES", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }

            // Danger Zone Section
            ProfileSectionHeader(stringResource(R.string.profile_danger_zone), Icons.Default.Warning)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MedicleanError.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, MedicleanError.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.profile_delete_account_label),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Black,
                        color = MedicleanError
                    )
                    Text(
                        text = stringResource(R.string.profile_delete_account_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MedicleanError.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MedicleanError),
                        border = BorderStroke(1.dp, MedicleanError)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.profile_delete_account_btn), fontWeight = FontWeight.Black)
                    }
                }
            }

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Versão ${com.zmstore.projectr.BuildConfig.VERSION_NAME} • RemeDio Certo Premium",
                    style = MaterialTheme.typography.labelSmall,
                    color = MedicleanDarkGreen.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Black
                )
                TextButton(onClick = { 
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://sites.google.com/view/projectr-privacy-policy"))
                    context.startActivity(intent)
                }) {
                    Text("Política de Privacidade", style = MaterialTheme.typography.labelSmall, color = MedicleanTeal, fontWeight = FontWeight.Black)
                }
            }
        }
    }

    // Listen for purchase events and show feedback
    LaunchedEffect(Unit) {
        viewModel.purchaseEvent.collect { (success, message) ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_account_title)) },
            text = { Text(stringResource(R.string.delete_account_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteAccount { success ->
                            if (success) onBack() // Retorna para a home/login
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicleanError)
                ) {
                    Text(stringResource(R.string.delete_account_confirm_btn), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.delete_account_cancel_btn))
                }
            }
        )
    }
}

@Composable
fun ProfileSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
        Icon(icon, contentDescription = null, tint = MedicleanTeal, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
            fontWeight = FontWeight.Black,
            color = MedicleanTeal
        )
    }
}

@Composable
private fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MedicleanDarkGreen.copy(alpha = 0.4f), fontWeight = FontWeight.Bold) },
        modifier = modifier.fillMaxWidth(),
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, tint = MedicleanTeal) } },
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (keyboardType == androidx.compose.ui.text.input.KeyboardType.Password) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MedicleanDarkGreen,
            unfocusedTextColor = MedicleanDarkGreen,
            focusedContainerColor = if (false) Color.White.copy(alpha = 0.05f) else Color.White,
            unfocusedContainerColor = if (false) Color.White.copy(alpha = 0.05f) else Color.White,
            focusedBorderColor = MedicleanTeal,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = MedicleanTeal
        )
    )
}

