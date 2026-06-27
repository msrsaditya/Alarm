package com.unbreakable.alarm
import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.unbreakable.alarm.config.AdminReceiver
@Composable
fun PermissionsCheckScreen(onAllGranted: @Composable () -> Unit) {
    val context = LocalContext.current
    var hasNotifications by remember { mutableStateOf(checkNotifications(context)) }
    var hasBatteryOpt by remember { mutableStateOf(checkBatteryOpt(context)) }
    var hasDNDAccess by remember { mutableStateOf(checkDNDAccess(context)) }
    var hasDeviceAdmin by remember { mutableStateOf(checkDeviceAdmin(context)) }
    var hasDrawOverlay by remember { mutableStateOf(checkDrawOverlay(context)) }
    var hasActivityRecognition by remember { mutableStateOf(checkActivityRecognition(context)) }
    var skipped by remember { mutableStateOf(false) }
    val allGranted = hasNotifications && hasBatteryOpt && hasDNDAccess && hasDeviceAdmin && hasDrawOverlay && hasActivityRecognition
    LaunchedEffect(Unit) {
        while(!allGranted && !skipped) {
            kotlinx.coroutines.delay(1000)
            hasNotifications = checkNotifications(context)
            hasBatteryOpt = checkBatteryOpt(context)
            hasDNDAccess = checkDNDAccess(context)
            hasDeviceAdmin = checkDeviceAdmin(context)
            hasDrawOverlay = checkDrawOverlay(context)
            hasActivityRecognition = checkActivityRecognition(context)
            if (hasNotifications && hasBatteryOpt && hasDNDAccess && hasDeviceAdmin && hasDrawOverlay && hasActivityRecognition) break
        }
    }
    if (allGranted || skipped) {
        onAllGranted()
    } else {
        Scaffold(
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 4.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        Button(
                            onClick = { skipped = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text("Skip for Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Box(
                    modifier = Modifier.size(72.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("App Permissions", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("To ensure your alarms wake you up reliably, we need access to some system permissions. Please grant them below.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(32.dp))
                val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                    hasNotifications = it
                }
                PermissionItem(
                    title = "Notifications",
                    description = "Required to show alarms while ringing.",
                    isGranted = hasNotifications,
                    isRequired = true,
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
                val activityLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                    hasActivityRecognition = it
                }
                PermissionItem(
                    title = "Walking",
                    description = "Required to track your steps for step missions.",
                    isGranted = hasActivityRecognition,
                    isRequired = false,
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            activityLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                        }
                    }
                )
                PermissionItem(
                    title = "Battery",
                    description = "Prevents the system from putting the app to sleep, missing alarms.",
                    isGranted = hasBatteryOpt,
                    isRequired = true,
                    onClick = {
                        @Suppress("BatteryLife")
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                )
                PermissionItem(
                    title = "Do Not Disturb",
                    description = "Allows alarms to ring even when your phone is silent.",
                    isGranted = hasDNDAccess,
                    isRequired = true,
                    onClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        context.startActivity(intent)
                    }
                )
                PermissionItem(
                    title = "Display Over Apps",
                    description = "Shows the alarm screen on top of everything when it rings.",
                    isGranted = hasDrawOverlay,
                    isRequired = true,
                    onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                )
                PermissionItem(
                    title = "Device Administrator",
                    description = "Secures the alarm so it can't be easily bypassed while ringing.",
                    isGranted = hasDeviceAdmin,
                    isRequired = false,
                    onClick = {
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(context, AdminReceiver::class.java))
                            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required to enforce maximum persistence while the alarm rings.")
                        }
                        context.startActivity(intent)
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
@Composable
fun PermissionItem(title: String, description: String, isGranted: Boolean, isRequired: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        onClick = { if (!isGranted) onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isRequired) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("Required", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer, maxLines = 1, softWrap = false)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isGranted) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = "Granted", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            } else {
                Icon(Icons.Rounded.ChevronRight, contentDescription = "Grant item", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
private fun checkNotifications(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
    return true
}
private fun checkActivityRecognition(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
    }
    return true
}
private fun checkBatteryOpt(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}
private fun checkDNDAccess(context: Context): Boolean {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return nm.isNotificationPolicyAccessGranted
}
private fun checkDeviceAdmin(context: Context): Boolean {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val adminComponent = ComponentName(context, AdminReceiver::class.java)
    return dpm.isAdminActive(adminComponent)
}
private fun checkDrawOverlay(context: Context): Boolean {
    return Settings.canDrawOverlays(context)
}