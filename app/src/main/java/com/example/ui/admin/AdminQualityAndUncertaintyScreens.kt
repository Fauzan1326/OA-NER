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
import com.example.admin.ModalityQualityMetric
import com.example.ai.UncertaintyTier
import com.example.auth.UserAccount
import com.example.core.contract.RiskTier

@Composable
fun AdminQualityScreen(
    admin: UserAccount,
    repository: AdminRepository
) {
    val qualityMetrics = remember(admin) { repository.getSignalQualityMetrics(admin) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "SIGNAL QUALITY & ARTIFACT OPERATIONS",
                color = AdminCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Module 3 Hardware Signal Verification & Sensor Coupling Health",
                color = AdminTextDim,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AdminSurfaceBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, AdminCyan),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "OPERATIONAL RESEARCH METRICS",
                        color = AdminCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "These indicators represent physical signal transmission quality, contact impedance, and noise artifact rejection rates. They do NOT reflect diagnostic sensitivity, clinical specificity, or prevalence.",
                        color = AdminTextDim,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        items(qualityMetrics) { metric ->
            AdminQualityMetricCard(metric = metric)
        }
    }
}

@Composable
fun AdminQualityMetricCard(metric: ModalityQualityMetric) {
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
                Text(
                    text = "MODALITY: ${metric.modality.name}",
                    color = AdminTextMain,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = AdminSurfaceBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AdminGreen)
                ) {
                    Text(
                        text = "PASS: ${metric.passRatePercent}%",
                        color = AdminGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Mean SQI: ${"%.2f".format(metric.operationalSqi)}", color = AdminCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Quality Threshold: >= 0.70", color = AdminTextDim, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("Artifacts: ${metric.artifactsDetectedCount}", color = AdminAmber, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Rejected: ${metric.rejectedSignalsCount}", color = AdminRed, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("Retests: ${metric.retestsTriggeredCount}", color = AdminAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun AdminUncertaintyScreen(
    admin: UserAccount,
    repository: AdminRepository
) {
    val uncertaintyStats = remember(admin) { repository.getUncertaintyBreakdown(admin) }
    val riskStats = remember(admin) { repository.getScreeningTierBreakdown(admin) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "UNCERTAINTY & RISK TIER OPERATIONS",
                color = AdminCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Calibrated Epistemic-Aleatoric Split vs. Screening Risk Categories",
                color = AdminTextDim,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AdminSurfaceBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCE93D8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFFCE93D8), modifier = Modifier.size(16.dp))
                    Text(
                        text = "CRITICAL INDEPENDENCE PRINCIPLE: Calibrated uncertainty measures model epistemic confidence and noise — NOT clinical risk severity. For example, a session can exhibit 'MODERATE SCREENING RISK' alongside 'HIGH UNCERTAINTY'. Admin cannot modify uncertainty calibration.",
                        color = AdminTextDim,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        item {
            Text(
                text = "UNCERTAINTY TIER DISTRIBUTION",
                color = AdminTextMain,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uncertaintyStats.forEach { (tier, count) ->
                    val color = when (tier) {
                        UncertaintyTier.LOWER_UNCERTAINTY -> AdminGreen
                        UncertaintyTier.MODERATE_UNCERTAINTY -> AdminBlue
                        UncertaintyTier.ELEVATED_UNCERTAINTY -> AdminAmber
                        UncertaintyTier.HIGH_UNCERTAINTY -> AdminRed
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AdminCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AdminBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(tier.name, color = AdminTextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text(tier.description, color = AdminTextDim, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                            Text(
                                text = count.toString(),
                                color = color,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "SCREENING RISK CATEGORIES (NON-DIAGNOSTIC)",
                color = AdminTextMain,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                riskStats.forEach { (tier, count) ->
                    val color = when (tier) {
                        RiskTier.LOWER_SCREENING_RISK -> AdminGreen
                        RiskTier.MODERATE_SCREENING_RISK -> AdminAmber
                        RiskTier.HIGHER_SCREENING_RISK -> AdminRed
                        RiskTier.HIGH_UNCERTAINTY_RETEST -> Color(0xFFCE93D8)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AdminCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AdminBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tier.label, color = AdminTextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(
                                text = count.toString(),
                                color = color,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
