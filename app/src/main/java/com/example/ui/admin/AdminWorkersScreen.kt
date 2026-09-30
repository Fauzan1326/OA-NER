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
import com.example.auth.UserAccount
import com.example.auth.UserRole
import com.example.auth.UserStatus

@Composable
fun AdminWorkersScreen(
    admin: UserAccount,
    repository: AdminRepository
) {
    var workers by remember { mutableStateOf(repository.getWorkers(admin)) }
    var pendingWorkers by remember { mutableStateOf(repository.getPendingAshaApprovals(admin)) }
    var workerToAssignRegion by remember { mutableStateOf<UserAccount?>(null) }
    var actionMessage by remember { mutableStateOf<String?>(null) }

    fun refreshLists() {
        workers = repository.getWorkers(admin)
        pendingWorkers = repository.getPendingAshaApprovals(admin)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "ASHA FIELD WORKER MANAGEMENT",
                color = AdminCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Operational Field Force Roster, Approvals & District Allocations",
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
                        Text(
                            text = actionMessage!!,
                            color = AdminGreen,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(onClick = { actionMessage = null }, modifier = Modifier.size(20.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = AdminGreen)
                        }
                    }
                }
            }
        }

        // PENDING USER APPROVALS SECTION
        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AdminCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, AdminAmber),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.PendingActions, contentDescription = null, tint = AdminAmber, modifier = Modifier.size(18.dp))
                            Text(
                                text = "PENDING USER APPROVALS (${pendingWorkers.size})",
                                color = AdminAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (pendingWorkers.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No pending field registrations awaiting supervisor approval.",
                            color = AdminTextDim,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        pendingWorkers.forEach { pending ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1F1805),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6B4E00)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pending.fullName,
                                            color = AdminTextMain,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Username: ${pending.username} | ID: ${pending.id}",
                                            color = AdminTextDim,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "REQUESTED ROLE: ${pending.role.name}",
                                            color = AdminAmber,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Proposed Center: ${pending.assignedCenter}",
                                            color = AdminTextDim,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val res = repository.approveAshaWorker(admin, pending.username)
                                            if (res.isSuccess) {
                                                actionMessage = "ASHA Worker ${pending.username} APPROVED successfully."
                                                refreshLists()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AdminGreen),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("APPROVE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ACTIVE & REGISTERED ASHA WORKERS
        item {
            Text(
                text = "ACTIVE & REGISTERED FIELD WORKERS (${workers.size})",
                color = AdminTextMain,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        items(workers) { worker ->
            AdminWorkerCard(
                worker = worker,
                onSuspend = {
                    repository.suspendAshaWorker(admin, worker.username)
                    actionMessage = "Worker ${worker.username} suspended."
                    refreshLists()
                },
                onReactivate = {
                    repository.reactivateAshaWorker(admin, worker.username)
                    actionMessage = "Worker ${worker.username} reactivated."
                    refreshLists()
                },
                onAssignRegion = {
                    workerToAssignRegion = worker
                }
            )
        }
    }

    if (workerToAssignRegion != null) {
        AdminAssignRegionDialog(
            worker = workerToAssignRegion!!,
            onDismiss = { workerToAssignRegion = null },
            onConfirm = { region, center ->
                repository.assignAshaWorkerRegion(admin, workerToAssignRegion!!.username, region, center)
                actionMessage = "Assigned ${workerToAssignRegion!!.username} to $region / $center"
                workerToAssignRegion = null
                refreshLists()
            }
        )
    }
}

@Composable
fun AdminWorkerCard(
    worker: UserAccount,
    onSuspend: () -> Unit,
    onReactivate: () -> Unit,
    onAssignRegion: () -> Unit
) {
    val statusColor = when (worker.status) {
        UserStatus.ACTIVE -> AdminGreen
        UserStatus.PENDING_APPROVAL -> AdminAmber
        UserStatus.SUSPENDED -> AdminRed
        UserStatus.REJECTED -> Color.Gray
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
                Column {
                    Text(
                        text = worker.fullName,
                        color = AdminTextMain,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "ID: ${worker.id} | @${worker.username}",
                        color = AdminTextDim,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = AdminSurfaceBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                ) {
                    Text(
                        text = worker.status.label,
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Region: ${worker.assignedRegion}",
                    color = AdminTextDim,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Center: ${worker.assignedCenter}",
                    color = Color(0xFF81D4FA),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Permitted operational actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAssignRegion,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AdminBlue)
                ) {
                    Text("ASSIGN REGION", fontSize = 9.sp, color = AdminBlue, fontFamily = FontFamily.Monospace)
                }

                if (worker.status == UserStatus.ACTIVE) {
                    OutlinedButton(
                        onClick = onSuspend,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AdminRed)
                    ) {
                        Text("SUSPEND", fontSize = 9.sp, color = AdminRed, fontFamily = FontFamily.Monospace)
                    }
                } else if (worker.status == UserStatus.SUSPENDED) {
                    OutlinedButton(
                        onClick = onReactivate,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AdminGreen)
                    ) {
                        Text("REACTIVATE", fontSize = 9.sp, color = AdminGreen, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAssignRegionDialog(
    worker: UserAccount,
    onDismiss: () -> Unit,
    onConfirm: (region: String, center: String) -> Unit
) {
    var selectedRegion by remember { mutableStateOf(worker.assignedRegion) }
    var selectedCenter by remember { mutableStateOf(worker.assignedCenter) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = AdminSurfaceBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AdminCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ASSIGN OPERATIONAL REGION",
                    color = AdminCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Worker: ${worker.fullName} (${worker.id})",
                    color = AdminTextDim,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = selectedRegion,
                    onValueChange = { selectedRegion = it },
                    label = { Text("Region / District", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AdminTextMain,
                        unfocusedTextColor = AdminTextMain,
                        focusedBorderColor = AdminCyan,
                        unfocusedBorderColor = AdminBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = selectedCenter,
                    onValueChange = { selectedCenter = it },
                    label = { Text("Primary Health Center / Sub-Center", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AdminTextMain,
                        unfocusedTextColor = AdminTextMain,
                        focusedBorderColor = AdminCyan,
                        unfocusedBorderColor = AdminBorder
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("CANCEL", color = AdminTextDim, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Button(
                        onClick = { onConfirm(selectedRegion, selectedCenter) },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminCyan),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SAVE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
