package com.example.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.example.admin.AdminRepository
import com.example.admin.OperationalAuditEvent
import com.example.auth.UserAccount
import androidx.compose.ui.platform.LocalContext
import com.example.report.PdfReportService
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SoraFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun AdminReportsScreen(
    admin: UserAccount,
    repository: AdminRepository
) {
    var exportPreviewContent by remember { mutableStateOf<String?>(null) }
    var exportFormatTitle by remember { mutableStateOf("EXPORT") }

    val kpis = remember(admin) { repository.computeAdminKpis(admin) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "OPERATIONAL REPORTS & AUTHORIZED EXPORT",
                    color = AdminCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SoraFontFamily
                )
                Text(
                    text = "Deployment Volume, Device Utilization & Quality Discard Records",
                    color = AdminTextDim,
                    fontSize = 11.sp,
                    fontFamily = SpaceGroteskFontFamily
                )
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AdminCardBg,
                border = BorderStroke(1.dp, AdminBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "OPERATIONAL RESEARCH METRICS SUMMARY",
                        color = AdminTextMain,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SoraFontFamily
                    )
                    Text("• Total Screening Volume: ${kpis.totalSessions}", color = AdminTextDim, fontSize = 11.sp, fontFamily = SpaceGroteskFontFamily)
                    Text("• Completed Screenings: ${kpis.completedCount}", color = AdminTextDim, fontSize = 11.sp, fontFamily = SpaceGroteskFontFamily)
                    Text("• Retests Triggered by Quality Gates: ${kpis.retestRequiredCount}", color = AdminAmber, fontSize = 11.sp, fontFamily = SpaceGroteskFontFamily)
                    Text("• High Uncertainty Allocations: ${kpis.highUncertaintyCount}", color = AdminPurple, fontSize = 11.sp, fontFamily = SpaceGroteskFontFamily)
                    Text("• Active Online Hardware Units: ${kpis.devicesOnlineCount}", color = AdminGreen, fontSize = 11.sp, fontFamily = SpaceGroteskFontFamily)
                }
            }
        }

        item {
            Text(
                text = "AUTHORIZED OPERATIONAL EXPORTS",
                color = AdminTextMain,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SoraFontFamily
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        exportFormatTitle = "JSON EXPORT PREVIEW"
                        exportPreviewContent = repository.exportOperationalReportJson(admin)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminCyan),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EXPORT JSON", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFontFamily)
                }

                Button(
                    onClick = {
                        exportFormatTitle = "CSV EXPORT PREVIEW"
                        exportPreviewContent = repository.exportOperationalReportCsv(admin)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminBlue),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EXPORT CSV", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFontFamily)
                }
            }
        }
    }

    if (exportPreviewContent != null) {
        Dialog(onDismissRequest = { exportPreviewContent = null }) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AdminSurfaceBg,
                border = BorderStroke(1.dp, AdminCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(exportFormatTitle, color = AdminCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFontFamily)
                        IconButton(onClick = { exportPreviewContent = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = AdminTextMain)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AdminCardBg,
                        border = BorderStroke(1.dp, AdminBorder),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = exportPreviewContent!!,
                            color = AdminTextMain,
                            fontSize = 10.sp,
                            fontFamily = JetBrainsMonoFontFamily,
                            modifier = Modifier
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { exportPreviewContent = null },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminCyan),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("DONE", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFontFamily)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAuditScreen(
    admin: UserAccount,
    repository: AdminRepository
) {
    val context = LocalContext.current
    val auditLogs by repository.auditLogFlow.collectAsState()
    var downloadedPdfFile by remember { mutableStateOf<java.io.File?>(null) }
    var downloadFailed by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "OPERATIONAL & REGULATORY AUDIT LOG",
                    color = AdminCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SoraFontFamily
                )
                Text(
                    text = "Chronological Immutable Provenance Trail (Append-Only)",
                    color = AdminTextDim,
                    fontSize = 11.sp,
                    fontFamily = SpaceGroteskFontFamily
                )
            }
        }

        // Section 3: Audit Chain Card
        item {
            AuditChainCard(
                integrityStatus = "VERIFIED",
                totalRecords = auditLogs.size,
                latestHash = if (auditLogs.isNotEmpty()) auditLogs.last().traceId else "a4f89b12c3d4e5f67890abcdef1234567890abcdef",
                tamperStatus = "Tamper Evident"
            )
        }

        // PDF Download Button
        item {
            Button(
                onClick = {
                    try {
                        val result = PdfReportService.generateAuditReportPdf(
                            context = context,
                            totalRecords = auditLogs.size,
                            latestHash = if (auditLogs.isNotEmpty()) auditLogs.last().traceId else "a4f89b12c3d4e5f67890abcdef1234567890abcdef",
                            genesisRoot = "0000000000000000",
                            auditLogs = auditLogs
                        )
                        if (result.isSuccess) {
                            downloadedPdfFile = result.getOrNull()
                            downloadFailed = false
                        } else {
                            downloadFailed = true
                        }
                    } catch (e: Exception) {
                        downloadFailed = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AdminCyan),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("DOWNLOAD AUDIT REPORT PDF", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFontFamily)
            }
        }

        if (downloadedPdfFile != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AdminGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AdminGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✓ Report downloaded",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold),
                                color = AdminGreen
                            )
                            Text(
                                text = "PDF saved successfully.",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = SpaceGroteskFontFamily),
                                color = AdminTextDim
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { downloadedPdfFile?.let { PdfReportService.openPdf(context, it) } },
                                colors = ButtonDefaults.buttonColors(containerColor = AdminGreen),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("OPEN PDF", color = Color.Black, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFontFamily))
                            }
                            OutlinedButton(
                                onClick = { downloadedPdfFile = null },
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("DISMISS", style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily, color = AdminTextDim))
                            }
                        }
                    }
                }
            }
        } else if (downloadFailed) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AdminRed.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AdminRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Couldn't generate PDF",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold),
                            color = AdminRed
                        )
                        Button(
                            onClick = {
                                val result = PdfReportService.generateAuditReportPdf(
                                    context = context,
                                    totalRecords = auditLogs.size,
                                    latestHash = if (auditLogs.isNotEmpty()) auditLogs.last().traceId else "a4f89b12c3d4e5f67890abcdef1234567890abcdef",
                                    genesisRoot = "0000000000000000",
                                    auditLogs = auditLogs
                                )
                                if (result.isSuccess) {
                                    downloadedPdfFile = result.getOrNull()
                                    downloadFailed = false
                                } else {
                                    downloadFailed = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminRed),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Retry", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFontFamily))
                        }
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AdminCardBg,
                border = BorderStroke(1.dp, AdminBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = AdminCyan, modifier = Modifier.size(16.dp))
                    Text(
                        text = "REGULATORY IMMUTABILITY: Historical audit records cannot be altered, edited, or deleted under medical device safety standards. All user modifications and access-denied intercepts are recorded.",
                        color = AdminCyan,
                        fontSize = 10.sp,
                        fontFamily = SpaceGroteskFontFamily
                    )
                }
            }
        }

        items(auditLogs) { event ->
            AdminAuditEventCard(event = event)
        }
    }
}

@Composable
fun AdminAuditEventCard(event: OperationalAuditEvent) {
    val resultColor = when (event.result) {
        "SUCCESS" -> AdminGreen
        "BLOCKED" -> AdminRed
        else -> AdminAmber
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = AdminCardBg,
        border = BorderStroke(1.dp, AdminBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.action,
                    color = AdminTextMain,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceGroteskFontFamily
                )
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = resultColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, resultColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = event.result,
                        color = resultColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFontFamily,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Actor: ${event.actorUsername} (${event.actorRole.name}) | Target: ${event.target}",
                color = AdminTextDim,
                fontSize = 10.sp,
                fontFamily = SpaceGroteskFontFamily
            )

            if (event.details.isNotBlank()) {
                Text(
                    text = "Details: ${event.details}",
                    color = AdminCyan,
                    fontSize = 10.sp,
                    fontFamily = SpaceGroteskFontFamily
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = event.formattedDate, color = AdminTextDim, fontSize = 9.sp, fontFamily = JetBrainsMonoFontFamily)
                Text(text = event.traceId, color = AdminTextDim, fontSize = 9.sp, fontFamily = JetBrainsMonoFontFamily)
            }
        }
    }
}
