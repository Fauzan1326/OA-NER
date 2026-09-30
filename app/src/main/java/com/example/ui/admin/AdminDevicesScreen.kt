package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.admin.AdminRepository
import com.example.admin.DeviceConnectionStatus
import com.example.admin.OperationalDevice
import com.example.auth.UserAccount

@Composable
fun AdminDevicesScreen(
    admin: UserAccount,
    repository: AdminRepository
) {
    val devices by repository.devices.collectAsState()
    val workers = remember(admin) { repository.getWorkers(admin) }

    var deviceToAssign by remember { mutableStateOf<OperationalDevice?>(null) }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "DEVICE OPERATIONS & HARDWARE FLEET",
                color = AdminCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Universal Hardware Bus Telemetry, Calibration & Worker Pairing",
                color = AdminTextDim,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        if (actionMessage != null) {
            item {
                Surface(
                    color = Color(0xFF0F2618),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AdminGreen),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = actionMessage!!, color = AdminGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        IconButton(onClick = { actionMessage = null }, modifier = Modifier.size(20.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = AdminGreen)
                        }
                    }
                }
            }
        }

        if (errorMessage != null) {
            item {
                Surface(
                    color = Color(0xFF2E0D0D),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AdminRed),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = errorMessage!!, color = AdminRed, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        IconButton(onClick = { errorMessage = null }, modifier = Modifier.size(20.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = AdminRed)
                        }
                    }
                }
            }
        }

        items(devices) { device ->
            AdminDeviceCard(
                device = device,
                onAssign = { deviceToAssign = device },
                onRelease = {
                    val res = repository.releaseDevice(admin, device.deviceId)
                    if (res.isSuccess) {
                        actionMessage = "Device ${device.deviceId} released."
                        errorMessage = null
                    } else {
                        errorMessage = res.exceptionOrNull()?.message
                    }
                },
                onRequestCalibration = {
                    val res = repository.requestDeviceCalibration(admin, device.deviceId)
                    if (res.isSuccess) {
                        actionMessage = "Device ${device.deviceId} flagged for calibration."
                        errorMessage = null
                    }
                }
            )
        }
    }

    if (deviceToAssign != null) {
        AdminAssignDeviceDialog(
            device = deviceToAssign!!,
            workers = workers,
            onDismiss = { deviceToAssign = null },
            onConfirm = { worker ->
                val res = repository.assignDevice(
                    caller = admin,
                    deviceId = deviceToAssign!!.deviceId,
                    workerId = worker.id,
                    workerName = worker.fullName,
                    center = worker.assignedCenter
                )
                if (res.isSuccess) {
                    actionMessage = "Device ${deviceToAssign!!.deviceId} successfully paired with ${worker.fullName}."
                    errorMessage = null
                } else {
                    errorMessage = res.exceptionOrNull()?.message
                }
                deviceToAssign = null
            }
        )
    }
}

@Composable
fun AdminDeviceCard(
    device: OperationalDevice,
    onAssign: () -> Unit,
    onRelease: () -> Unit,
    onRequestCalibration: () -> Unit
) {
    val statusColor = when (device.connectionStatus) {
        DeviceConnectionStatus.CONNECTED, DeviceConnectionStatus.STREAMING -> AdminGreen
        DeviceConnectionStatus.CALIBRATION_REQUIRED -> AdminAmber
        DeviceConnectionStatus.FAULT -> AdminRed
        DeviceConnectionStatus.DISCONNECTED, DeviceConnectionStatus.UNKNOWN -> Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = AdminCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, AdminBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = device.deviceId,
                        color = AdminCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = device.deviceType,
                        color = AdminTextDim,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF0F1820),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                ) {
                    Text(
                        text = device.connectionStatus.label,
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Real telemetry metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Packets: ${device.packetsReceived}", color = AdminTextMain, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("Packet Loss: ${"%.2f".format(device.packetLossPercent)}%", color = if (device.packetLossPercent > 0.05) AdminAmber else AdminGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("Jitter: ${device.jitterMs} ms", color = AdminTextMain, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("Gaps: ${device.sequenceGaps}", color = if (device.sequenceGaps > 0) AdminAmber else AdminGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("Schema: ${device.schemaVersion}", color = AdminBlue, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("Quality: ${"%.2f".format(device.qualityScore)}", color = AdminCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Assigned field worker information
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF0F1820),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (device.assignedWorkerId != null) {
                        Column {
                            Text(
                                text = "PAIRED: ${device.assignedWorkerName} (${device.assignedWorkerId})",
                                color = Color(0xFF81D4FA),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Center: ${device.assignedCenter ?: "Unassigned"}",
                                color = AdminTextDim,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        OutlinedButton(
                            onClick = onRelease,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AdminRed)
                        ) {
                            Text("RELEASE", fontSize = 8.sp, color = AdminRed, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        Text(
                            text = "AVAILABLE FOR DEPLOYMENT (NO WORKER ASSIGNED)",
                            color = AdminTextDim,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Button(
                            onClick = onAssign,
                            colors = ButtonDefaults.buttonColors(containerColor = AdminCyan),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("ASSIGN", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Additional diagnostic / calibration actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRequestCalibration,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AdminAmber)
                ) {
                    Text("CALIBRATION REQUEST", fontSize = 8.sp, color = AdminAmber, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun AdminAssignDeviceDialog(
    device: OperationalDevice,
    workers: List<UserAccount>,
    onDismiss: () -> Unit,
    onConfirm: (UserAccount) -> Unit
) {
    var selectedWorker by remember { mutableStateOf<UserAccount?>(workers.firstOrNull()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = AdminSurfaceBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AdminCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PAIR DEVICE WITH FIELD WORKER",
                    color = AdminCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Target Hardware: ${device.deviceId} (${device.deviceType})",
                    color = AdminTextDim,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SELECT AUTHORIZED ASHA WORKER:",
                    color = AdminTextMain,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                workers.forEach { worker ->
                    val isSelected = selectedWorker?.id == worker.id
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) AdminActiveBg else AdminCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AdminCyan else AdminBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(worker.fullName, color = AdminTextMain, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text("${worker.id} | ${worker.assignedCenter}", color = AdminTextDim, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedWorker = worker },
                                colors = RadioButtonDefaults.colors(selectedColor = AdminCyan)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("CANCEL", color = AdminTextDim, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Button(
                        onClick = {
                            if (selectedWorker != null) onConfirm(selectedWorker!!)
                        },
                        enabled = selectedWorker != null,
                        colors = ButtonDefaults.buttonColors(containerColor = AdminCyan),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CONFIRM PAIRING", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
