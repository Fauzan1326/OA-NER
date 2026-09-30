package com.example.ui.superadmin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.UserAccount
import com.example.auth.UserRole
import com.example.auth.UserStatus
import com.example.superadmin.*
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

// ============================================================
// USER & ROLE ADMINISTRATION SCREEN
// ============================================================
@Composable
fun SuperAdminUsersScreen(
    superAdmin: UserAccount,
    repository: SuperAdminRepository
) {
    var users by remember { mutableStateOf(repository.getAllUsers(superAdmin)) }
    var selectedRoleFilter by remember { mutableStateOf<UserRole?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<UserStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun refreshUsers() {
        users = repository.getAllUsers(superAdmin)
    }

    val filteredUsers = remember(users, selectedRoleFilter, selectedStatusFilter, searchQuery) {
        users.filter { u ->
            (selectedRoleFilter == null || u.role == selectedRoleFilter) &&
            (selectedStatusFilter == null || u.status == selectedStatusFilter) &&
            (searchQuery.isBlank() || u.username.contains(searchQuery, ignoreCase = true) ||
             u.fullName.contains(searchQuery, ignoreCase = true) ||
             u.assignedCenter.contains(searchQuery, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SaDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SuperAdminSectionHeader(
                title = "USER & ROLE ADMINISTRATION",
                subtitle = "Root Credential Lifecycle, Authorization Levels & Provisioning",
                badgeText = "${users.size} Registered",
                badgeColor = SaPurple
            )
        }

        // Top Action Bar & Create Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search users, centers...", fontFamily = SoraFontFamily, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SaTextSecondary) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SaPurple),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add User", fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Role Filter Pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedRoleFilter == null,
                    onClick = { selectedRoleFilter = null },
                    label = { Text("All Roles", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp) }
                )
                UserRole.values().forEach { role ->
                    FilterChip(
                        selected = selectedRoleFilter == role,
                        onClick = { selectedRoleFilter = if (selectedRoleFilter == role) null else role },
                        label = { Text(role.name, fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp) }
                    )
                }
            }
        }

        if (statusMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SaCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SaCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage!!,
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = SaCyan,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Users List - Section 18 Compact Specification
        items(filteredUsers) { user ->
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = SaCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, SaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = SoraFontFamily
                        ),
                        color = SaTextPrimary
                    )
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        color = SaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = user.role.displayName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontFamily = SpaceGroteskFontFamily
                        ),
                        color = SaCyan
                    )
                    Text(
                        text = "${user.assignedCenter} • ${user.assignedRegion}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = SpaceGroteskFontFamily
                        ),
                        color = SaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (user.status) {
                                UserStatus.ACTIVE -> SaGreen.copy(alpha = 0.15f)
                                UserStatus.PENDING_APPROVAL -> SaGold.copy(alpha = 0.15f)
                                UserStatus.SUSPENDED, UserStatus.REJECTED -> SaRed.copy(alpha = 0.15f)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (user.status) {
                                    UserStatus.ACTIVE -> SaGreen
                                    UserStatus.PENDING_APPROVAL -> SaGold
                                    UserStatus.SUSPENDED, UserStatus.REJECTED -> SaRed
                                }
                            )
                        ) {
                            Text(
                                text = "[ ${user.status.name} ]",
                                fontSize = 10.sp,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = when (user.status) {
                                    UserStatus.ACTIVE -> SaGreen
                                    UserStatus.PENDING_APPROVAL -> SaGold
                                    UserStatus.SUSPENDED, UserStatus.REJECTED -> SaRed
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // Operational Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (user.status == UserStatus.PENDING_APPROVAL) {
                                Button(
                                    onClick = {
                                        repository.updateUserStatus(superAdmin, user.username, UserStatus.ACTIVE)
                                        refreshUsers()
                                        statusMessage = "Approved user ${user.username}"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SaGreen),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Approve", fontSize = 11.sp, fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (user.status == UserStatus.ACTIVE && user.role != UserRole.SUPER_ADMIN) {
                                OutlinedButton(
                                    onClick = {
                                        repository.updateUserStatus(superAdmin, user.username, UserStatus.SUSPENDED)
                                        refreshUsers()
                                        statusMessage = "Suspended user ${user.username}"
                                    },
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Suspend", fontSize = 11.sp, fontFamily = SpaceGroteskFontFamily, color = SaRed)
                                }
                            }

                            if (user.status == UserStatus.SUSPENDED) {
                                Button(
                                    onClick = {
                                        repository.updateUserStatus(superAdmin, user.username, UserStatus.ACTIVE)
                                        refreshUsers()
                                        statusMessage = "Reactivated user ${user.username}"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SaCyan),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Reactivate", fontSize = 11.sp, fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateUserDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { uname, fname, mail, role, pass, center, region ->
                val result = repository.createAdminOrStaffUser(
                    superAdmin = superAdmin,
                    username = uname,
                    fullName = fname,
                    email = mail,
                    role = role,
                    initialPassword = pass,
                    assignedCenter = center,
                    assignedRegion = region
                )
                if (result.isSuccess) {
                    refreshUsers()
                    statusMessage = "User '$uname' successfully provisioned."
                } else {
                    statusMessage = "Creation failed: ${result.exceptionOrNull()?.message}"
                }
                showCreateDialog = false
            }
        )
    }
}

// ============================================================
// DEPLOYMENT / REGIONAL HIERARCHY SCREEN
// ============================================================
@Composable
fun SuperAdminRegionsScreen(
    superAdmin: UserAccount,
    repository: SuperAdminRepository
) {
    val zones = remember { repository.getDeploymentZones(superAdmin) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SaDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SuperAdminSectionHeader(
                title = "DEPLOYMENT & REGIONAL GOVERNANCE",
                subtitle = "Multi-District Rural Deployment Network (NER Tier-3 Centers)",
                badgeText = "${zones.size} Active Zones",
                badgeColor = SaCyan
            )
        }

        items(zones) { zone ->
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = SaCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, SaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${zone.district}, ${zone.state}",
                                fontSize = 16.sp,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = SaTextPrimary
                            )
                            Text(
                                text = "Zone ID: ${zone.zoneId} | Assigned Admin: @${zone.leadAdminUsername}",
                                fontSize = 11.sp,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = SaCyan
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SaGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${zone.totalScreeningsCount} Screenings",
                                fontSize = 10.sp,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = SaGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Active ASHA Workers: ${zone.activeAshaCount}",
                            fontSize = 11.sp,
                            fontFamily = SoraFontFamily,
                            color = SaTextSecondary
                        )
                        Text(
                            text = "•",
                            color = SaTextSecondary
                        )
                        Text(
                            text = "Hardware Fleet: ${zone.assignedDevicesCount}",
                            fontSize = 11.sp,
                            fontFamily = SoraFontFamily,
                            color = SaTextSecondary
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SaBorder)

                    Text(
                        text = "HEALTH CENTERS HIERARCHY (${zone.healthCenters.size})",
                        fontSize = 11.sp,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = SaTextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        zone.healthCenters.forEach { center ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SaDarkBg, RoundedCornerShape(4.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = center.name,
                                        fontSize = 12.sp,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SaTextPrimary
                                    )
                                    Text(
                                        text = "${center.villageOrWard} (PIN: ${center.pinCode})",
                                        fontSize = 10.sp,
                                        fontFamily = SoraFontFamily,
                                        color = SaTextSecondary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = SaBlue.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = center.type.label,
                                        fontSize = 9.sp,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = SaBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateUserDialog(
    onDismiss: () -> Unit,
    onCreate: (uname: String, fname: String, mail: String, role: UserRole, pass: String, center: String, reg: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.ADMIN) }
    var password by remember { mutableStateOf("AdminPass2026!") }
    var center by remember { mutableStateOf("AMCH Dibrugarh Central") }
    var region by remember { mutableStateOf("Dibrugarh, Assam-NER") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("PROVISION NEW SYSTEM USER", fontFamily = SpaceGroteskFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", fontFamily = JetBrainsMonoFontFamily) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Legal Name", fontFamily = SoraFontFamily) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Institutional Email", fontFamily = SoraFontFamily) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Initial Password", fontFamily = JetBrainsMonoFontFamily) },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Assigned Role:", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(UserRole.ADMIN, UserRole.CLINICIAN, UserRole.ASHA_WORKER).forEach { r ->
                        FilterChip(
                            selected = role == r,
                            onClick = { role = r },
                            label = { Text(r.name, fontSize = 10.sp, fontFamily = JetBrainsMonoFontFamily) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(username, fullName, email, role, password, center, region) },
                colors = ButtonDefaults.buttonColors(containerColor = SaPurple)
            ) {
                Text("Create Account", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = SpaceGroteskFontFamily)
            }
        }
    )
}
