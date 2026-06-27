@file:Suppress("DEPRECATION")
package com.unbreakable.alarm.ui.missions
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unbreakable.alarm.data.Difficulty
import com.unbreakable.alarm.data.MissionConfig
import com.unbreakable.alarm.data.MissionType
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionsSection(
    missions: List<MissionConfig>,
    onMissionsChange: (List<MissionConfig>) -> Unit
) {
    var showMissionBottomSheet by remember { mutableStateOf(false) }
    var selectedMissionIndex by remember { mutableStateOf<Int?>(null) }
    var missionToEdit by remember { mutableStateOf<MissionConfig?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Wake-up mission", fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
            Text("${missions.size}/5", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(missions) { index, mission ->
                MissionItem(mission = mission, onClick = {
                    selectedMissionIndex = index
                    missionToEdit = mission
                    showMissionBottomSheet = true
                })
            }
            if (missions.size < 5) {
                item {
                    AddMissionButton(onClick = {
                        selectedMissionIndex = null
                        missionToEdit = null
                        showMissionBottomSheet = true
                    })
                }
                items(4 - missions.size) {
                    EmptyMissionSlot()
                }
            }
        }
    }
    if (showMissionBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMissionBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            MissionSetupFlow(
                initialMission = missionToEdit,
                onSave = { newMission ->
                    val newList = missions.toMutableList()
                    if (selectedMissionIndex != null) {
                        newList[selectedMissionIndex!!] = newMission
                    } else {
                        newList.add(newMission)
                    }
                    onMissionsChange(newList)
                    showMissionBottomSheet = false
                },
                onCancel = { showMissionBottomSheet = false },
                onDelete = {
                    if (selectedMissionIndex != null) {
                        val newList = missions.toMutableList()
                        newList.removeAt(selectedMissionIndex!!)
                        onMissionsChange(newList)
                    }
                    showMissionBottomSheet = false
                }
            )
        }
    }
}
@Composable
fun MissionItem(mission: MissionConfig, onClick: () -> Unit) {
    val icon = when(mission.type) {
        MissionType.COLOR_TILES -> Icons.Default.GridView
        MissionType.TYPING -> Icons.Default.Keyboard
        MissionType.MATH -> Icons.Default.Calculate
        MissionType.STEP -> Icons.Default.DirectionsWalk
        MissionType.SHAKE -> Icons.Default.Vibration
    }
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
    }
}
@Composable
fun AddMissionButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add Mission", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable
fun EmptyMissionSlot() {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.surfaceVariant)
    }
}