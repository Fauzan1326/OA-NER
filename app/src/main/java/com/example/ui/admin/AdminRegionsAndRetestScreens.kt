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
import com.example.admin.AdminRepository
import com.example.admin.RetestQueueItem
import com.example.auth.UserAccount

@Composable
fun AdminRegionsScreen(
    admin: UserAccount,
    repository: AdminRepository
) {
    val regions by repository.regions.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "OPERATIONAL REGIONS & SUB-CENTERS",
                color = AdminCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Hierarchical Deployment Topology (Region -> Center -> Worker + Device)",
                color = AdminTextDim,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        if (regions.isEmpty()) {
            item {
                Surface(
                    color = AdminCardBg,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AdminBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "NO REGIONS CONFIGURED",
                            color = AdminTextDim,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        } else {
            items(regions) { region ->
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
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = AdminCyan, modifier = Modifier.size(18.dp))
                                Text(
                                    text = region.name,
                                    color = AdminTextMain,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = region.regionId,
                                color = AdminTextDim,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        region.centers.forEach { center ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AdminSurfaceBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AdminBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Center: ${center.name}",
                                        color = AdminBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Workers: ${if (center.workerIds.isNotEmpty()) center.workerIds.joinToString(", ") else "None"}",
                                            color = AdminTextDim,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Devices: ${if (center.deviceIds.isNotEmpty()) center.deviceIds.joinToString(", ") else "None"}",
                                            color = AdminCyan,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
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
}

@Composable
fun AdminRetestScreen(
    admin: UserAccount,
    repository: AdminRepository
) {
    val retests = remember(admin) { repository.getRetestQueue(admin) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "RETEST QUEUE & REMEDIATION MONITORING",
                color = AdminAmber,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Multimodal Signal Quality Rejections & High Uncertainty Follow-Ups",
                color = AdminTextDim,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E1400),
                border = androidx.compose.foundation.BorderStroke(1.dp, AdminAmber),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = AdminAmber, modifier = Modifier.size(16.dp))
                    Text(
                        text = "SCIENTIFIC NOTICE: Retest determinations are mandated by physical contact, SQI < 0.70, or epistemic uncertainty limits. Admin cannot manually override scientific retest recommendations.",
                        color = AdminAmber,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        if (retests.isEmpty()) {
            item {
                Surface(
                    color = AdminCardBg,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AdminBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "NO SESSIONS CURRENTLY REQUIRE RETEST REMEDIATION",
                            color = AdminGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        } else {
            items(retests) { item ->
                AdminRetestCard(item = item)
            }
        }
    }
}

@Composable
fun AdminRetestCard(item: RetestQueueItem) {
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
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AdminAmber, modifier = Modifier.size(16.dp))
                    Text(
                        text = item.sessionId,
                        color = AdminCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = AdminSurfaceBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AdminAmber)
                ) {
                    Text(
                        text = item.status,
                        color = AdminAmber,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Failed Modality: ${item.failedModality}",
                color = AdminTextMain,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Scientific Reason: ${item.reason}",
                color = AdminAmber,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Worker: ${item.workerName} (${item.workerId}) | Attempt: ${item.formattedDate}",
                color = AdminTextDim,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Operational Note: ${item.operationalNote}",
                color = Color(0xFF81D4FA),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
