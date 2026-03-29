package com.example.androidplayground.ui.notifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.androidplayground.navigation.Screen

private val SurfaceColor = Color(0xFFF9F9F9)
private val SurfaceLowest = Color(0xFFFFFFFF)
private val PrimaryGreen = Color(0xFF3CDA84)
private val PrimaryDark = Color(0xFF006D3B)
private val GhostBorder = Color(0x26BBCABC)
private val TextMain = Color(0xFF000000)
private val TextMuted = Color(0xFF49454F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDashboardScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        containerColor = SurfaceColor,
        topBar = {
            TopAppBar(
                title = { Text("Notification Dashboard", fontWeight = FontWeight.Bold, color = TextMain) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
            )
        }
    ) { padding ->
        if (hasNotificationPermission) {
            DashboardContent(
                modifier = Modifier.padding(padding),
                notificationHelper = notificationHelper,
                navController = navController
            )
        } else {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                ) {
                    Text("Grant Notification Permission", color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    modifier: Modifier,
    notificationHelper: NotificationHelper,
    navController: NavController
) {
    var title by remember { mutableStateOf("New Message") }
    var text by remember { mutableStateOf("You have received a new update.") }
    var appName by remember { mutableStateOf("Playground") }

    var selectedChannel by remember { mutableStateOf(NotificationHelper.CHANNEL_DEFAULT) }
    var selectedVisibility by remember { mutableStateOf(NotificationCompat.VISIBILITY_PUBLIC) }

    var hasActionButton by remember { mutableStateOf(false) }
    var actionButtonLabel by remember { mutableStateOf("View Details") }
    var selectedTarget by remember { mutableStateOf("home") }

    val scrollState = rememberScrollState()

    val openFeatureDesc = { key: String ->
        navController.navigate(Screen.FeatureDetail.createRoute("notification_info:$key"))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Content Settings Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceLowest)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Using standard typography but bolding to simulate Manrope if not explicitly declared
            Text("Content", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextMain)

            MinimalistTextField(
                label = "CONTENT TITLE",
                value = title,
                onValueChange = { title = it },
                onInfoClick = { openFeatureDesc("notification_title") }
            )

            MinimalistTextField(
                label = "CONTENT TEXT",
                value = text,
                onValueChange = { text = it },
                onInfoClick = { openFeatureDesc("notification_text") }
            )

            MinimalistTextField(
                label = "APP NAME",
                value = appName,
                onValueChange = { appName = it },
                onInfoClick = { openFeatureDesc("notification_app_name") }
            )
        }

        // Settings Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceLowest)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextMain)
            
            DropdownSetting(
                label = "IMPORTANCE",
                options = listOf("Urgent" to NotificationHelper.CHANNEL_URGENT, "High" to NotificationHelper.CHANNEL_HIGH, "Medium" to NotificationHelper.CHANNEL_DEFAULT, "Low" to NotificationHelper.CHANNEL_LOW),
                selected = selectedChannel,
                onSelect = { selectedChannel = it },
                onInfoClick = { openFeatureDesc("notification_importance") }
            )

            DropdownSetting(
                label = "VISIBILITY",
                options = listOf("Public" to NotificationCompat.VISIBILITY_PUBLIC, "Private" to NotificationCompat.VISIBILITY_PRIVATE, "Secret" to NotificationCompat.VISIBILITY_SECRET),
                selected = selectedVisibility,
                onSelect = { selectedVisibility = it },
                onInfoClick = { openFeatureDesc("notification_lock_visibility") }
            )

            if (selectedVisibility != NotificationCompat.VISIBILITY_PUBLIC) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryGreen.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Conditional View: A public fallback version will be shown on secure lock screens.",
                        color = PrimaryDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Actions Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceLowest)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Actions", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextMain)
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                LabelWithInfo("HAS ACTION BUTTON?", { openFeatureDesc("notification_action_button") })
                Switch(
                    checked = hasActionButton,
                    onCheckedChange = { hasActionButton = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryGreen)
                )
            }

            if (hasActionButton) {
                MinimalistTextField(
                    label = "BUTTON LABEL",
                    value = actionButtonLabel,
                    onValueChange = { actionButtonLabel = it }
                )
                
                DropdownSetting(
                    label = "DEEP LINK TARGET",
                    options = listOf("Home" to "home", "Feature List" to "feature_list", "Demos" to "demo_list"),
                    selected = selectedTarget,
                    onSelect = { selectedTarget = it },
                    onInfoClick = { openFeatureDesc("notification_deep_link") }
                )
            }
        }

        // Action Buttons at bottom
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val gradientBackground = Brush.linearGradient(colors = listOf(PrimaryDark, PrimaryGreen))
            Button(
                onClick = {
                    notificationHelper.sendNotification(
                        notificationId = 1001,
                        channelId = selectedChannel,
                        title = title,
                        text = text,
                        appName = appName,
                        visibility = selectedVisibility,
                        actionLabel = if (hasActionButton) actionButtonLabel else null,
                        actionTarget = if (hasActionButton) selectedTarget else null
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(gradientBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Notification", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    notificationHelper.sendNotification(
                        notificationId = 1001,
                        channelId = selectedChannel,
                        title = title,
                        text = text,
                        appName = appName,
                        visibility = selectedVisibility,
                        actionLabel = if (hasActionButton) actionButtonLabel else null,
                        actionTarget = if (hasActionButton) selectedTarget else null
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = PrimaryDark, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Update Notification", color = PrimaryDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            TextButton(
                onClick = { notificationHelper.cancelNotification(1001) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel Notification", color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun LabelWithInfo(label: String, onInfoClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = TextMuted
        )
        IconButton(onClick = onInfoClick, modifier = Modifier.size(24.dp).padding(start = 4.dp)) {
            Icon(Icons.Default.Info, contentDescription = "Info", tint = PrimaryGreen, modifier = Modifier.size(14.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MinimalistTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onInfoClick: (() -> Unit)? = null
) {
    Column {
        if (onInfoClick != null) {
            LabelWithInfo(label, onInfoClick)
        } else {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextMuted
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = GhostBorder,
                focusedBorderColor = PrimaryGreen,
                unfocusedLabelColor = Color.Transparent, // Removed inner label since we have top label
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownSetting(
    label: String,
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit,
    onInfoClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = options.find { it.second == selected }?.first ?: ""

    Column {
        LabelWithInfo(label, onInfoClick)
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp).menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = GhostBorder,
                    focusedBorderColor = PrimaryGreen
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.first) },
                        onClick = {
                            onSelect(option.second)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
