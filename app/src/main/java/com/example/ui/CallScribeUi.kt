package com.example.ui

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScribeMainScreen(viewModel: CallScribeViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("live") }

    // Collect data reactively from Room DB state flows
    val clients by viewModel.clientProfiles.collectAsState()
    val tasks by viewModel.projectTasks.collectAsState()
    val records by viewModel.callRecords.collectAsState()
    val smsList by viewModel.smsMessages.collectAsState()

    // Setup permission launcher
    val requestMultiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.isAudioPermissionGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        viewModel.isPhoneStatePermissionGranted = permissions[Manifest.permission.READ_PHONE_STATE] == true
        viewModel.isSmsPermissionGranted = permissions[Manifest.permission.RECEIVE_SMS] == true || permissions[Manifest.permission.READ_SMS] == true
        
        Toast.makeText(context, "Permissions Updated Successfully", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Shield Icon",
                            tint = SecurityGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "CALLSCRIBE AI",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = TextPrimary
                        )
                    }
                },
                actions = {
                    // Privacy indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SecurityGreenDark.copy(alpha = 0.2f))
                            .border(1.dp, SecurityGreen.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SecurityGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SECURE LOCAL MODE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = SecurityGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBg,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ObsidianBg,
                tonalElevation = 8.dp,
                modifier = Modifier.border(0.5.dp, BorderSlate, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = currentTab == "live",
                    onClick = { currentTab = "live" },
                    icon = { Icon(if (currentTab == "live") Icons.Filled.RecordVoiceOver else Icons.Outlined.RecordVoiceOver, contentDescription = "Live Call") },
                    label = { Text("SafeCall Live", fontSize = 11.sp, fontFamily = FontFamily.SansSerif) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBg,
                        selectedTextColor = SecurityGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = SecurityGreen
                    ),
                    modifier = Modifier.testTag("nav_tab_live")
                )
                NavigationBarItem(
                    selected = currentTab == "vault",
                    onClick = { currentTab = "vault" },
                    icon = { Icon(if (currentTab == "vault") Icons.Filled.Lock else Icons.Outlined.Lock, contentDescription = "Vault") },
                    label = { Text("Secure Vault", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBg,
                        selectedTextColor = SecurityGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = SecurityGreen
                    ),
                    modifier = Modifier.testTag("nav_tab_vault")
                )
                NavigationBarItem(
                    selected = currentTab == "crm",
                    onClick = { currentTab = "crm" },
                    icon = { Icon(if (currentTab == "crm") Icons.Filled.BusinessCenter else Icons.Outlined.BusinessCenter, contentDescription = "CRM") },
                    label = { Text("CRM/Tasks", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBg,
                        selectedTextColor = SecurityGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = SecurityGreen
                    ),
                    modifier = Modifier.testTag("nav_tab_crm")
                )
                NavigationBarItem(
                    selected = currentTab == "sms",
                    onClick = { currentTab = "sms" },
                    icon = { Icon(if (currentTab == "sms") Icons.Filled.PriorityHigh else Icons.Outlined.PriorityHigh, contentDescription = "SMS") },
                    label = { Text("Priority SMS", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBg,
                        selectedTextColor = SecurityGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = SecurityGreen
                    ),
                    modifier = Modifier.testTag("nav_tab_sms")
                )
            }
        },
        containerColor = ObsidianBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ObsidianBg)
        ) {
            // Permissions and setup quick notifier at the top of everything
            PermissionsBanner(
                viewModel = viewModel,
                onRequestPermissions = {
                    requestMultiplePermissionsLauncher.launch(
                        arrayOf(
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.READ_SMS
                        )
                    )
                }
            )

            // Dynamic Main Window Area based on tab
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                when (currentTab) {
                    "live" -> LiveCallTab(viewModel, clients, tasks)
                    "vault" -> SecureVaultTab(viewModel, records, clients)
                    "crm" -> CrmAndTasksTab(viewModel, clients, tasks)
                    "sms" -> SmsPrioritizerTab(viewModel, smsList, clients)
                }
            }
        }
    }
}

@Composable
fun PermissionsBanner(viewModel: CallScribeViewModel, onRequestPermissions: () -> Unit) {
    val allGranted = viewModel.isAudioPermissionGranted && 
                     viewModel.isPhoneStatePermissionGranted && 
                     viewModel.isSmsPermissionGranted

    AnimatedVisibility(
        visible = !allGranted,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .border(1.dp, UrgentOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning icon",
                        tint = UrgentOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "System Permissions Required",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "To enable automatic call-sensing, real-time microphone transcription, and urgent SMS prioritizing offline, please grant system authorizations.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Permission Indicators
                    PermissionChip("Microphone", viewModel.isAudioPermissionGranted)
                    PermissionChip("Phone Calls", viewModel.isPhoneStatePermissionGranted)
                    PermissionChip("SMS Access", viewModel.isSmsPermissionGranted)
                }

                Button(
                    onClick = onRequestPermissions,
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("grant_permissions_button")
                ) {
                    Text("Grant Required Access", color = ObsidianBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PermissionChip(name: String, granted: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (granted) SecurityGreenDark.copy(alpha = 0.2f) else CriticalRed.copy(alpha = 0.15f))
            .border(0.5.dp, if (granted) SecurityGreen else CriticalRed, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (granted) SecurityGreen else CriticalRed)
        )
        Text(
            text = name,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (granted) SecurityGreen else CriticalRed
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveCallTab(
    viewModel: CallScribeViewModel,
    clients: List<ClientProfile>,
    tasks: List<ProjectTask>
) {
    val context = LocalContext.current
    var selectedClientForCall by remember { mutableStateOf<ClientProfile?>(null) }
    var showSimulatorLauncher by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Session Card
        item {
            if (viewModel.isCallActive) {
                ActiveCallVisualizer(viewModel)
            } else if (viewModel.isCallProcessing) {
                CallProcessingCard()
            } else {
                NoActiveSessionCard(
                    clients = clients,
                    selectedClient = selectedClientForCall,
                    onSelectClient = { selectedClientForCall = it },
                    onStartSimulatedCall = { client ->
                        viewModel.startSimulatedCall(
                            callerName = client?.name ?: "Unknown Caller",
                            phoneNumber = client?.phone ?: "+1 555-0199"
                        )
                    }
                )
            }
        }

        // Live Demo Quick Simulator Trigger Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSlate.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Outlined.FlashOn, contentDescription = "Simulate icon", tint = SecurityGreen)
                            Text("Real-Time Simulator Controllers", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        IconButton(onClick = { showSimulatorLauncher = !showSimulatorLauncher }) {
                            Icon(
                                imageVector = if (showSimulatorLauncher) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Simulator",
                                tint = TextSecondary
                            )
                        }
                    }

                    AnimatedVisibility(visible = showSimulatorLauncher) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Use these quick presets to generate incoming private phone call dialogues or trigger SMS context classifiers in real-time.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                maxItemsInEachRow = 2
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.startSimulatedCall("Alice Vance", "+1 555-0101")
                                        Toast.makeText(context, "Secure Live Session started: Alice Vance", Toast.LENGTH_SHORT).show()
                                    },
                                    enabled = !viewModel.isCallActive && !viewModel.isCallProcessing,
                                    colors = ButtonDefaults.buttonColors(containerColor = SecurityGreenDark),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 4.dp)
                                        .testTag("simulate_alice_call_button")
                                ) {
                                    Text("Call Alice", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.startSimulatedCall("Marcus Aurelius", "+1 555-0102")
                                        Toast.makeText(context, "Secure Live Session started: Marcus Aurelius", Toast.LENGTH_SHORT).show()
                                    },
                                    enabled = !viewModel.isCallActive && !viewModel.isCallProcessing,
                                    colors = ButtonDefaults.buttonColors(containerColor = SecurityGreenDark),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 4.dp)
                                        .testTag("simulate_marcus_call_button")
                                ) {
                                    Text("Call Marcus", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            HorizontalDivider(color = BorderSlate, thickness = 0.5.dp)

                            // Quick SMS Injector Presets
                            Text("Simulate Priority SMS Ingestion:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                SmsSimulationRow(
                                    title = "Acme Server Crash (Critical Score 9/10)",
                                    onClick = {
                                        viewModel.simulateIncomingSms(
                                            senderNumber = "+1 555-0101",
                                            body = "CRITICAL: Alice here. Acme server crashed due to buffer overflows. Is our firewall secure?"
                                        )
                                        Toast.makeText(context, "Simulated SMS received from Alice Vance", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                SmsSimulationRow(
                                    title = "Marcus Time Change (Normal Score 4/10)",
                                    onClick = {
                                        viewModel.simulateIncomingSms(
                                            senderNumber = "+1 555-0102",
                                            body = "Hey, let's delay our Tuesday dashboard alignment to 4 PM. Have some tasks to finish."
                                        )
                                        Toast.makeText(context, "Simulated SMS received from Marcus Aurelius", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmsSimulationRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(GlassWhite)
            .clickable(onClick = onClick)
            .border(0.5.dp, BorderSlate, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 11.sp, color = TextPrimary)
        Icon(Icons.Default.ArrowForward, contentDescription = "Send", tint = SecurityGreen, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun NoActiveSessionCard(
    clients: List<ClientProfile>,
    selectedClient: ClientProfile?,
    onSelectClient: (ClientProfile?) -> Unit,
    onStartSimulatedCall: (ClientProfile?) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Secure Shield graphic placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SecurityGreen.copy(alpha = 0.08f))
                    .border(2.dp, SecurityGreen.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Shield Guard",
                    tint = SecurityGreen,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = "Secure Call Transcription Active",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Privacy Shield fully protects all sessions. Voice-to-text transcriptions, semantic summaries, and client links are processed inside end-to-end encrypted storage.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            HorizontalDivider(color = BorderSlate)

            // Select Target Profile to simulate Call
            Text(
                text = "Select Client Profile to initiate secure Call Session:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.Start)
            )

            if (clients.isEmpty()) {
                Text(
                    text = "No client profiles found. Use 'CRM/Tasks' tab to add clients first, or use Quick Preset buttons below.",
                    fontSize = 11.sp,
                    color = UrgentOrange,
                    textAlign = TextAlign.Center
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GlassWhite)
                        .border(0.5.dp, BorderSlate, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .clickable {
                            // Cyling through clients for demo simplicity
                            val index = clients.indexOf(selectedClient)
                            val nextIndex = (index + 1) % clients.size
                            onSelectClient(clients[nextIndex])
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedClient?.name ?: "Unknown Caller (Generic Sandbox)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = selectedClient?.company ?: "+1 555-0199",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Cycle Client", fontSize = 10.sp, color = SecurityGreen)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = SecurityGreen)
                    }
                }
            }

            Button(
                onClick = { onStartSimulatedCall(selectedClient) },
                colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_secure_call_button")
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call Icon", tint = ObsidianBg)
                    Text("Start Secure Live Call", color = ObsidianBg, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ActiveCallVisualizer(viewModel: CallScribeViewModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, SecurityGreen, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pulsing Dot
                    Canvas(modifier = Modifier.size(16.dp)) {
                        drawCircle(color = SecurityGreen.copy(alpha = 0.2f), radius = 16.dp.toPx() * scale)
                        drawCircle(color = SecurityGreen, radius = 6.dp.toPx())
                    }
                    Text(
                        text = "LIVE PRIVACY TRANSCRIPTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecurityGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Call Duration Counter
                Text(
                    text = formatDuration(viewModel.activeCallDurationSeconds),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            HorizontalDivider(color = BorderSlate)

            // Client Info Panel
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassWhite)
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SecurityGreenDark.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Person", tint = SecurityGreen)
                }

                Column {
                    Text(
                        text = viewModel.activeCallerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${viewModel.activeClientProfile?.company ?: "Unknown Company"} (${viewModel.activeCallerPhone})",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (viewModel.activeClientProfile != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SecurityGreen.copy(alpha = 0.1f))
                            .border(0.5.dp, SecurityGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Profile Synced", fontSize = 9.sp, color = SecurityGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Real-Time Transcript Text Box
            Text(
                text = "Encrypted Dialogue Transcript Stream:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObsidianBg)
                    .border(1.dp, BorderSlate, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                if (viewModel.activeCallTranscript.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = SecurityGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Awaiting secure voice transmission signals...",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = false
                    ) {
                        item {
                            Text(
                                text = viewModel.activeCallTranscript,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Real-Time Manual Signal Injector for Demo/Testing
            var customSpeechText by remember { mutableStateOf("") }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customSpeechText,
                    onValueChange = { customSpeechText = it },
                    placeholder = { Text("Inject spoken words manually...", fontSize = 11.sp, color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = SecurityGreen,
                        unfocusedBorderColor = BorderSlate,
                        focusedContainerColor = ObsidianBg,
                        unfocusedContainerColor = ObsidianBg
                    ),
                    textStyle = TextStyle(fontSize = 11.sp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )

                Button(
                    onClick = {
                        if (customSpeechText.trim().isNotEmpty()) {
                            viewModel.injectCustomSpeech("User", customSpeechText)
                            customSpeechText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen.copy(alpha = 0.2f), contentColor = SecurityGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .border(1.dp, SecurityGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Text("Inject Speech", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // End Call Trigger Button
            Button(
                onClick = { viewModel.endSimulatedCall() },
                colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("end_secure_call_button")
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = TextPrimary)
                    Text("End & Process Call Privately", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CallProcessingCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SecurityGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = SecurityGreen, modifier = Modifier.size(48.dp))
            Text(
                text = "Processing Offline Summaries...",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Secure local AI modules are computing transcription context schemas, extracting client project action items, and locking storage envelopes.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CustomFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) SecurityGreen else CardSlate)
            .border(0.5.dp, if (selected) SecurityGreen else BorderSlate, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) ObsidianBg else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SentimentBadge(sentiment: String) {
    val (bgColor, textColor, text) = when (sentiment.lowercase()) {
        "positive" -> Triple(SecurityGreen.copy(alpha = 0.15f), SecurityGreen, "😊 Positive")
        "negative" -> Triple(CriticalRed.copy(alpha = 0.15f), CriticalRed, "😟 Negative")
        else -> Triple(TextSecondary.copy(alpha = 0.15f), TextSecondary, "😐 Neutral")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

fun getHighlightedTranscript(transcript: String, highlights: List<String>): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        append(transcript)
        
        val activeHighlights = highlights.filter { it.isNotBlank() }
        if (activeHighlights.isNotEmpty()) {
            activeHighlights.forEach { phrase ->
                var startIndex = transcript.indexOf(phrase, ignoreCase = true)
                while (startIndex != -1) {
                    val endIndex = startIndex + phrase.length
                    addStyle(
                        style = androidx.compose.ui.text.SpanStyle(
                            background = Color(0xFFE6C300), // Elegant high-contrast amber yellow highlight
                            color = Color(0xFF1E1E1E),
                            fontWeight = FontWeight.Bold
                        ),
                        start = startIndex,
                        end = endIndex
                    )
                    startIndex = transcript.indexOf(phrase, startIndex + 1, ignoreCase = true)
                }
            }
        }
    }
}

@Composable
fun SecureVaultTab(
    viewModel: CallScribeViewModel,
    records: List<CallRecord>,
    clients: List<ClientProfile>
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTagFilter by remember { mutableStateOf<String?>(null) }

    val filteredRecords = records.filter { record ->
        val client = clients.find { it.id == record.clientProfileId }
        val matchesClient = client?.name?.contains(searchQuery, ignoreCase = true) == true
        val matchesCompany = client?.company?.contains(searchQuery, ignoreCase = true) == true
        val matchesPhone = record.phoneNumber.contains(searchQuery)
        val matchesTranscript = record.transcript.contains(searchQuery, ignoreCase = true)
        val matchesTags = record.tagsRaw.contains(searchQuery, ignoreCase = true)
        val matchesHighlights = record.highlightedPhrases.contains(searchQuery, ignoreCase = true)
        val matchesSentiment = record.sentiment.contains(searchQuery, ignoreCase = true)
        
        val matchesSearch = searchQuery.isEmpty() || matchesClient || matchesCompany || matchesPhone || matchesTranscript || matchesTags || matchesHighlights || matchesSentiment
        val matchesTagFilter = selectedTagFilter == null || record.tagsList.contains(selectedTagFilter)

        matchesSearch && matchesTagFilter
    }

    val allUniqueTags = records.flatMap { it.tagsList }.filter { it.isNotEmpty() }.distinct()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Heading Vault Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Encrypted Call Vault",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${records.size} total calls locked in E2E database",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.Https,
                contentDescription = "Safe Vault",
                tint = SecurityGreen,
                modifier = Modifier.size(24.dp)
            )
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search logs, tags, keywords, sentiment...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vault_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = SecurityGreen,
                unfocusedBorderColor = BorderSlate,
                focusedContainerColor = CardSlate,
                unfocusedContainerColor = CardSlate
            ),
            shape = RoundedCornerShape(10.dp)
        )

        // Custom horizontal scrollable tag pills
        if (allUniqueTags.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    CustomFilterChip(
                        selected = selectedTagFilter == null,
                        label = "All Logs",
                        onClick = { selectedTagFilter = null }
                    )
                }
                items(allUniqueTags) { tag ->
                    CustomFilterChip(
                        selected = selectedTagFilter == tag,
                        label = "#$tag",
                        onClick = { selectedTagFilter = if (selectedTagFilter == tag) null else tag }
                    )
                }
            }
        }

        // Records List
        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardSlate)
                    .border(0.5.dp, BorderSlate, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = "No keys", tint = TextSecondary, modifier = Modifier.size(40.dp))
                    Text("No Encrypted Sessions Found", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Sessions matching filter or search query appear here.", fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRecords) { record ->
                    val client = clients.find { it.id == record.clientProfileId }
                    VaultRecordItem(
                        record = record,
                        client = client,
                        viewModel = viewModel,
                        onExportCrm = {
                            viewModel.exportToCrm(record)
                            Toast.makeText(context, "Exported securely to local CRM module", Toast.LENGTH_SHORT).show()
                        },
                        onExportJira = {
                            viewModel.exportToProjectTasks(record)
                            Toast.makeText(context, "Exported action items into project Tasks board", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            viewModel.deleteCallRecord(record.id)
                            Toast.makeText(context, "Secure session destroyed permanently", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VaultRecordItem(
    record: CallRecord,
    client: ClientProfile?,
    viewModel: CallScribeViewModel,
    onExportCrm: () -> Unit,
    onExportJira: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (expanded) SecurityGreen.copy(alpha = 0.5f) else BorderSlate,
                RoundedCornerShape(12.dp)
            )
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = client?.name ?: "Unknown Client",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${client?.company ?: "Manual Caller"} • ${formatDuration(record.durationSeconds)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    
                    // Sentiment & Quick Tags under metadata line
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SentimentBadge(sentiment = record.sentiment)
                        
                        record.tagsList.take(3).forEach { tag ->
                            if (tag.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BorderSlate)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("#$tag", fontSize = 8.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Exported Status Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (record.exportedStatus) {
                                    "Local Only" -> BorderSlate
                                    else -> SecurityGreenDark.copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = record.exportedStatus,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (record.exportedStatus) {
                                "Local Only" -> TextSecondary
                                else -> SecurityGreen
                            }
                        )
                    }

                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Brief summary shown initially
            Text(
                text = record.summary,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = if (expanded) 10 else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp)
            )

            // Expanded panel details
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = BorderSlate)

                    // Action items
                    Text("Action Items Extracted:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecurityGreen)
                    if (record.actionItemsList.isEmpty()) {
                        Text("No specific action items identified during session.", fontSize = 11.sp, color = TextSecondary)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            record.actionItemsList.forEach { item ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Item",
                                        tint = SecurityGreen,
                                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                    )
                                    Text(item, fontSize = 11.sp, color = TextPrimary)
                                }
                            }
                        }
                    }

                    Divider(color = BorderSlate)

                    // Custom Tags Section
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Custom Applied Tags:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecurityGreen)
                        
                        val tags = record.tagsList.filter { it.isNotEmpty() }
                        if (tags.isEmpty()) {
                            Text("No custom tags applied yet.", fontSize = 11.sp, color = TextSecondary)
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Column {
                                    tags.chunked(4).forEach { rowTags ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        ) {
                                            rowTags.forEach { tag ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(SecurityGreen.copy(alpha = 0.12f))
                                                        .border(0.5.dp, SecurityGreen.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                                ) {
                                                    Text("#$tag", fontSize = 10.sp, color = SecurityGreen, fontWeight = FontWeight.Bold)
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove Tag",
                                                        tint = SecurityGreen,
                                                        modifier = Modifier
                                                            .size(12.dp)
                                                            .clickable { viewModel.removeTagFromCallRecord(record, tag) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Add Custom Tag Input Field
                        var newTagText by remember { mutableStateOf("") }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = newTagText,
                                onValueChange = { newTagText = it },
                                placeholder = { Text("Enter custom tag (e.g., Support, Urgent)...", fontSize = 11.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = SecurityGreen,
                                    unfocusedBorderColor = BorderSlate,
                                    focusedContainerColor = ObsidianBg,
                                    unfocusedContainerColor = ObsidianBg
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (newTagText.isNotBlank()) {
                                        viewModel.addTagToCallRecord(record, newTagText)
                                        newTagText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text("Add Tag", color = ObsidianBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Preset Toggles
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text("Quick Presets:", fontSize = 10.sp, color = TextSecondary)
                            listOf("Urgent", "Follow-up", "Billing", "Review").forEach { preset ->
                                val isApplied = record.tagsList.contains(preset)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isApplied) SecurityGreen.copy(alpha = 0.2f) else BorderSlate)
                                        .clickable {
                                            if (isApplied) {
                                                viewModel.removeTagFromCallRecord(record, preset)
                                            } else {
                                                viewModel.addTagToCallRecord(record, preset)
                                            }
                                        }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(preset, fontSize = 9.sp, color = if (isApplied) SecurityGreen else TextSecondary, fontWeight = FontWeight.Bold)
                                }
                             }
                        }
                    }

                    HorizontalDivider(color = BorderSlate)

                    // Keyword Highlight Section
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Custom Highlighted Phrases:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecurityGreen)
                        
                        val highlights = record.highlightedPhrasesList.filter { it.isNotEmpty() }
                        if (highlights.isEmpty()) {
                            Text("No highlighted terms. Tap words in the transcript box below or type phrases here.", fontSize = 11.sp, color = TextSecondary)
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Column {
                                    highlights.chunked(3).forEach { rowPhrases ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        ) {
                                            rowPhrases.forEach { phrase ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(UrgentOrange.copy(alpha = 0.15f))
                                                        .border(0.5.dp, UrgentOrange.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                                ) {
                                                    Text(phrase, fontSize = 10.sp, color = UrgentOrange, fontWeight = FontWeight.Medium)
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove Highlight",
                                                        tint = UrgentOrange,
                                                        modifier = Modifier
                                                            .size(12.dp)
                                                            .clickable { viewModel.removeHighlightFromCallRecord(record, phrase) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Add Highlight Text Input
                        var newHighlightText by remember { mutableStateOf("") }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = newHighlightText,
                                onValueChange = { newHighlightText = it },
                                placeholder = { Text("Highlight specific words/phrases...", fontSize = 11.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = SecurityGreen,
                                    unfocusedBorderColor = BorderSlate,
                                    focusedContainerColor = ObsidianBg,
                                    unfocusedContainerColor = ObsidianBg
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (newHighlightText.isNotBlank()) {
                                        viewModel.addHighlightToCallRecord(record, newHighlightText)
                                        newHighlightText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text("Highlight", color = ObsidianBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Divider(color = BorderSlate)

                    // Dialog transcripts with interactive click-to-highlight capability
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Dialogue Transcript Log:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text(
                            text = "💡 Interactive View: Tap any word in the transcript below to instantly toggle its highlight status!",
                            fontSize = 10.sp,
                            color = SecurityGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ObsidianBg)
                                .border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            val annotatedTranscript = getHighlightedTranscript(record.transcript, record.highlightedPhrasesList)
                            androidx.compose.foundation.text.ClickableText(
                                text = annotatedTranscript,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                ),
                                onClick = { offset ->
                                    val text = record.transcript
                                    if (offset >= 0 && offset < text.length) {
                                        var start = offset
                                        while (start > 0 && text[start - 1].isLetterOrDigit()) {
                                            start--
                                        }
                                        var end = offset
                                        while (end < text.length && text[end].isLetterOrDigit()) {
                                            end++
                                        }
                                        if (start < end) {
                                            val clickedWord = text.substring(start, end).trim()
                                            // Ensure we are selecting a real word of sufficient size
                                            if (clickedWord.length > 1) {
                                                if (record.highlightedPhrasesList.contains(clickedWord)) {
                                                    viewModel.removeHighlightFromCallRecord(record, clickedWord)
                                                } else {
                                                    viewModel.addHighlightToCallRecord(record, clickedWord)
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Divider(color = BorderSlate)

                    // Control Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(containerColor = CriticalRed.copy(alpha = 0.1f)),
                            border = BorderStroke(0.5.dp, CriticalRed),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Destroy Session", color = CriticalRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onExportCrm,
                                colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Export CRM", color = ObsidianBg, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onExportJira,
                                colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Export Tasks Board", color = ObsidianBg, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CrmAndTasksTab(
    viewModel: CallScribeViewModel,
    clients: List<ClientProfile>,
    tasks: List<ProjectTask>
) {
    var showAddClientDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tabs internally between clients and tasks
        var internalSubTab by remember { mutableStateOf("tasks") }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("CRM & Project Sync", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Text("Manage project channels and linked client databases", fontSize = 11.sp, color = TextSecondary)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (internalSubTab == "clients") {
                            showAddClientDialog = true
                        } else {
                            showAddTaskDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = ObsidianBg, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (internalSubTab == "clients") "Add Client" else "Add Task", color = ObsidianBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Segmented control button tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CardSlate)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (internalSubTab == "tasks") SecurityGreen else Color.Transparent)
                    .clickable { internalSubTab = "tasks" }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Project Tasks Board (${tasks.size})",
                    color = if (internalSubTab == "tasks") ObsidianBg else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (internalSubTab == "clients") SecurityGreen else Color.Transparent)
                    .clickable { internalSubTab = "clients" }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Client Profiles Database (${clients.size})",
                    color = if (internalSubTab == "clients") ObsidianBg else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Sublist view
        if (internalSubTab == "clients") {
            ClientsList(clients = clients, onDeleteClient = { viewModel.deleteClientProfile(it) })
        } else {
            TasksList(tasks = tasks, clients = clients, onToggle = { viewModel.toggleTask(it) }, onDelete = { viewModel.deleteProjectTask(it) })
        }
    }

    // Modal dialog overlays
    if (showAddClientDialog) {
        AddClientDialog(
            onDismiss = { showAddClientDialog = false },
            onConfirm = { name, company, email, phone, bio ->
                viewModel.createClientProfile(name, company, email, phone, bio)
                showAddClientDialog = false
            }
        )
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            clients = clients,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, clientId, desc, channel, priority ->
                viewModel.createProjectTask(title, clientId, desc, "Pending", channel, priority)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun ClientsList(clients: List<ClientProfile>, onDeleteClient: (ClientProfile) -> Unit) {
    if (clients.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No clients added yet. Tap Add Client to begin.", color = TextSecondary, fontSize = 12.sp)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(clients) { client ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    modifier = Modifier.fillMaxWidth().border(0.5.dp, BorderSlate, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(client.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Text(client.company, fontSize = 11.sp, color = SecurityGreen)
                            }

                            IconButton(onClick = { onDeleteClient(client) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CriticalRed.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Phone: ${client.phone}", fontSize = 11.sp, color = TextSecondary)
                        Text("Email: ${client.email}", fontSize = 11.sp, color = TextSecondary)
                        Text("Bio: ${client.bio}", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TasksList(
    tasks: List<ProjectTask>,
    clients: List<ClientProfile>,
    onToggle: (ProjectTask) -> Unit,
    onDelete: (ProjectTask) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tasks added yet. Active items sync automatically from calls.", color = TextSecondary, fontSize = 12.sp)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tasks) { task ->
                val client = clients.find { it.id == task.clientProfileId }
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            0.5.dp,
                            if (task.status == "Completed") BorderSlate else SecurityGreen.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Custom Checkbox
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (task.status == "Completed") SecurityGreen else Color.Transparent)
                                .border(1.5.dp, SecurityGreen, CircleShape)
                                .clickable { onToggle(task) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (task.status == "Completed") {
                                Icon(Icons.Default.Check, contentDescription = "Done", tint = ObsidianBg, modifier = Modifier.size(16.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                fontWeight = FontWeight.Bold,
                                color = if (task.status == "Completed") TextSecondary else TextPrimary,
                                fontSize = 13.sp,
                                modifier = Modifier.testTag("task_title_${task.id}")
                            )
                            Text(
                                text = "Client: ${client?.name ?: "Unknown"} • Channel: ${task.projectChannel}",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            if (task.description.isNotEmpty()) {
                                Text(
                                    text = task.description,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        // Priority Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (task.priority) {
                                        "High" -> CriticalRed.copy(alpha = 0.15f)
                                        "Medium" -> UrgentOrange.copy(alpha = 0.15f)
                                        else -> BorderSlate
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.priority,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (task.priority) {
                                    "High" -> CriticalRed
                                    "Medium" -> UrgentOrange
                                    else -> TextSecondary
                                }
                            )
                        }

                        // Delete
                        IconButton(onClick = { onDelete(task) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmsPrioritizerTab(
    viewModel: CallScribeViewModel,
    smsList: List<SmsMessage>,
    clients: List<ClientProfile>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SMS tab header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("SMS Priorities Inbox", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Text("Prioritized by AI based on project channels contexts", fontSize = 11.sp, color = TextSecondary)
            }

            Icon(Icons.Default.MailOutline, contentDescription = "SMS", tint = SecurityGreen, modifier = Modifier.size(24.dp))
        }

        // SMS Inbox list
        if (smsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Text("Secure SMS Priority Queue empty.", color = TextSecondary, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(smsList) { sms ->
                    val client = clients.find { it.id == sms.linkedProjectId }
                    SmsPriorityItem(sms = sms, client = client, onMarkRead = { viewModel.markSmsRead(sms.id) }, onDelete = { viewModel.deleteSms(sms) })
                }
            }
        }
    }
}

@Composable
fun SmsPriorityItem(
    sms: SmsMessage,
    client: ClientProfile?,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                when (sms.priorityLevel) {
                    "Critical" -> CriticalRed.copy(alpha = 0.6f)
                    "Urgent" -> UrgentOrange.copy(alpha = 0.6f)
                    "High" -> SecurityGreen.copy(alpha = 0.4f)
                    else -> BorderSlate
                },
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Priority Tag Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (sms.priorityLevel) {
                                    "Critical" -> CriticalRed
                                    "Urgent" -> UrgentOrange
                                    "High" -> SecurityGreenDark
                                    else -> BorderSlate
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = sms.priorityLevel.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sms.priorityLevel == "Critical" || sms.priorityLevel == "Urgent") ObsidianBg else TextPrimary
                        )
                    }

                    // Urgency Indicator Score
                    Text(
                        text = "Urgency: ${sms.urgencyScore}/10",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            sms.urgencyScore >= 8 -> CriticalRed
                            sms.urgencyScore >= 5 -> UrgentOrange
                            else -> TextSecondary
                        }
                    )
                }

                // Delete Action
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!sms.isRead) {
                        Text(
                            text = "NEW",
                            color = SecurityGreen,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SecurityGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .clickable { onMarkRead() }
                        )
                    }
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onDelete() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body
            Text(
                text = "\"${sms.body}\"",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sender linked info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = "Sender", tint = TextSecondary, modifier = Modifier.size(12.dp))
                Text(
                    text = "From: ${client?.name ?: "Unknown Sender"} (${sms.senderNumber})",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            // AI Context linkage Reasoning
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianBg)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI context",
                    tint = SecurityGreen,
                    modifier = Modifier.size(14.dp).padding(top = 1.dp)
                )
                Text(
                    text = sms.categoryReasoning,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

// Dialog Layouts
@Composable
fun AddClientDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Secure Client Profile", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company Name") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") })
                OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Profile Notes / Bio") })
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotEmpty() && phone.isNotEmpty()) onConfirm(name, company, email, phone, bio) },
                colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen)
            ) {
                Text("Save Client", color = ObsidianBg)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = CardSlate
    )
}

@Composable
fun AddTaskDialog(
    clients: List<ClientProfile>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedClientId by remember { mutableStateOf(clients.firstOrNull()?.id ?: 0) }
    var desc by remember { mutableStateOf("") }
    var channel by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Project Task", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task Title") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                OutlinedTextField(value = channel, onValueChange = { channel = it }, label = { Text("Project Channel Name (e.g. Acme App)") })
                
                // Select Client phone link
                Text("Link to Client:", fontSize = 12.sp, color = TextSecondary)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GlassWhite)
                        .clickable {
                            val idx = clients.indexOfFirst { it.id == selectedClientId }
                            val nextIdx = (idx + 1) % clients.size
                            selectedClientId = clients[nextIdx].id
                        }
                        .padding(12.dp)
                ) {
                    val clientName = clients.find { it.id == selectedClientId }?.name ?: "No Clients Added"
                    Text(clientName, color = TextPrimary)
                }

                // Select priority
                Text("Priority Level:", fontSize = 12.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("High", "Medium", "Low").forEach { p ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (priority == p) SecurityGreen else BorderSlate)
                                .clickable { priority = p }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(p, color = if (priority == p) ObsidianBg else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotEmpty() && selectedClientId > 0) onConfirm(title, selectedClientId, desc, channel, priority) },
                colors = ButtonDefaults.buttonColors(containerColor = SecurityGreen)
            ) {
                Text("Save Task", color = ObsidianBg)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = CardSlate
    )
}

// Helpers
fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
