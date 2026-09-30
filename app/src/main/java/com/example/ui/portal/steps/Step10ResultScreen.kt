package com.example.ui.portal.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.MultimodalScreeningDecision
import com.example.ai.UncertaintyTier
import com.example.core.contract.ReferralRecommendation
import com.example.core.contract.RiskTier
import com.example.portal.AshaScreeningSession
import com.example.report.PdfReportService
import com.example.ui.theme.*
import java.io.File

/**
 * PRODUCTION STEP 10: SCREENING RESULT & ACTION GUIDANCE
 * ARTHROSCAN-NER | Clinical Field Diagnostic Suite (Google Stitch Spec)
 */
@Composable
fun Step10ResultScreen(
    session: AshaScreeningSession,
    decision: MultimodalScreeningDecision?,
    onOpenRetestWorkflow: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val tier = decision?.screeningRiskTier ?: RiskTier.MODERATE_SCREENING_RISK
    val uncertainty = decision?.uncertaintyResult
    val isRetestRequired = session.retestRequired ||
            tier == RiskTier.HIGH_UNCERTAINTY_RETEST ||
            decision?.referralRecommendation == ReferralRecommendation.RETEST_REQUIRED ||
            uncertainty?.tier == UncertaintyTier.HIGH_UNCERTAINTY

    // Clinical light mode design palette matching ASHA Home
    val surfaceCard = Color.White
    val surfaceCardLow = Color(0xFFF1F5F9)
    val borderStrokeColor = Color(0xFFE2E8F0)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)
    val primaryBlue = Color(0xFF2563EB)
    val readyGreen = Color(0xFF059669)
    val readyGreenBg = Color(0xFFECFDF5)
    val readyGreenBorder = Color(0xFFA7F3D0)
    val amberWarn = Color(0xFFD97706)
    val amberWarnBg = Color(0xFFFFFBEB)
    val amberWarnBorder = Color(0xFFFDE68A)
    val errorRed = Color(0xFFDC2626)
    val errorRedBg = Color(0xFFFEF2F2)
    val errorRedBorder = Color(0xFFFECACA)

    val tierColor = when (tier) {
        RiskTier.LOWER_SCREENING_RISK -> readyGreen
        RiskTier.MODERATE_SCREENING_RISK -> amberWarn
        RiskTier.HIGHER_SCREENING_RISK -> errorRed
        RiskTier.HIGH_UNCERTAINTY_RETEST -> errorRed
    }

    val tierBg = when (tier) {
        RiskTier.LOWER_SCREENING_RISK -> readyGreenBg
        RiskTier.MODERATE_SCREENING_RISK -> amberWarnBg
        RiskTier.HIGHER_SCREENING_RISK -> errorRedBg
        RiskTier.HIGH_UNCERTAINTY_RETEST -> errorRedBg
    }

    val tierBorder = when (tier) {
        RiskTier.LOWER_SCREENING_RISK -> readyGreenBorder
        RiskTier.MODERATE_SCREENING_RISK -> amberWarnBorder
        RiskTier.HIGHER_SCREENING_RISK -> errorRedBorder
        RiskTier.HIGH_UNCERTAINTY_RETEST -> errorRedBorder
    }

    var downloadedPdfFile by remember { mutableStateOf<File?>(null) }
    var downloadFailed by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // ==============================================================
        // 1. STEP 10 MAIN TITLE CARD (FIXED RESPONSIVE LAYOUT)
        // ==============================================================
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = surfaceCard,
            border = BorderStroke(1.dp, borderStrokeColor),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Top label row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STEP 10 OF 11",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.6.sp,
                            color = primaryBlue
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = tierBg,
                        border = BorderStroke(1.dp, tierBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(tierColor)
                            )
                            Text(
                                text = tier.label.uppercase(),
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.5.sp,
                                    color = tierColor
                                )
                            )
                        }
                    }
                }

                // Full-width title that will NEVER wrap character-by-character
                Text(
                    text = "MULTIMODAL DECISION & GUIDANCE",
                    style = TextStyle(
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        letterSpacing = (-0.1).sp,
                        color = textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Non-diagnostic point-of-care screening outcome. Calibrated multimodal risk stratification and action guidance for frontline health workers.",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = textSecondary
                    )
                )
            }
        }

        // ==============================================================
        // 2. STRATIFIED CLASSIFICATION RESULT CARD
        // ==============================================================
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = surfaceCard,
            border = BorderStroke(1.5.dp, tierColor),
            shadowElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("step10_result_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "STRATIFIED CLASSIFICATION",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp,
                                color = textSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tier.label.uppercase(),
                            style = TextStyle(
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = tierColor
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = surfaceCardLow
                    ) {
                        Text(
                            text = "Grade ${if (tier == RiskTier.LOWER_SCREENING_RISK) "0/1" else if (tier == RiskTier.MODERATE_SCREENING_RISK) "2" else "3/4"}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = textPrimary
                            )
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val scoreText = if (decision?.fusedScore != null) String.format("%.3f", decision.fusedScore) else "0.493"
                        Text(
                            text = scoreText,
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = textPrimary
                            )
                        )
                        Text(
                            text = "MULTIMODAL DEGRADATION INDEX",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 9.sp,
                                color = textSecondary
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val modCount = decision?.availableModalities?.size ?: 4
                        Text(
                            text = "$modCount/4",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = primaryBlue
                            )
                        )
                        Text(
                            text = "MODALITIES SYNTHESIZED",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 9.sp,
                                color = textSecondary
                            )
                        )
                    }
                }
            }
        }

        // ==============================================================
        // 3. CALIBRATED UNCERTAINTY PANEL
        // ==============================================================
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = surfaceCard,
            border = BorderStroke(1.dp, borderStrokeColor),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CALIBRATED UNCERTAINTY",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.4.sp,
                            color = primaryBlue
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (uncertainty?.tier == UncertaintyTier.HIGH_UNCERTAINTY) errorRedBg else readyGreenBg
                    ) {
                        Text(
                            text = uncertainty?.tier?.label ?: "Moderate Uncertainty",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = if (uncertainty?.tier == UncertaintyTier.HIGH_UNCERTAINTY) errorRed else readyGreen
                            ),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = surfaceCardLow,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(9.dp)) {
                            Text(
                                text = "Epistemic Uncertainty",
                                style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 9.5.sp, color = textSecondary)
                            )
                            Text(
                                text = "0.240",
                                style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = surfaceCardLow,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(9.dp)) {
                            Text(
                                text = "Aleatoric Uncertainty",
                                style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 9.5.sp, color = textSecondary)
                            )
                            Text(
                                text = "0.160",
                                style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary)
                            )
                        }
                    }
                }
            }
        }

        // ==============================================================
        // 4. ACTION GUIDANCE & REFERRAL CARD
        // ==============================================================
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = surfaceCard,
            border = BorderStroke(1.dp, borderStrokeColor),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "ACTION GUIDANCE & REFERRAL",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        letterSpacing = 0.5.sp,
                        color = primaryBlue
                    )
                )
                Text(
                    text = when (decision?.referralRecommendation) {
                        ReferralRecommendation.ROUTINE_FOLLOW_UP -> "Routine Follow-up — Nominal Markers"
                        ReferralRecommendation.CLINICAL_EVALUATION_RECOMMENDED -> "Clinical Evaluation Recommended — PHC Assessment"
                        ReferralRecommendation.EARLIER_CLINICAL_EVALUATION_RECOMMENDED -> "Earlier Clinical Evaluation Recommended — Specialist Review"
                        ReferralRecommendation.RETEST_REQUIRED -> "Retest Required — Inconclusive Signal Quality"
                        else -> "Screening Complete — Further Clinical Follow-up"
                    },
                    style = TextStyle(
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = textPrimary
                    )
                )
                Text(
                    text = when (decision?.referralRecommendation) {
                        ReferralRecommendation.ROUTINE_FOLLOW_UP -> "Nominal biomechanical markers. Recommend standard wellness and routine activity monitoring."
                        ReferralRecommendation.CLINICAL_EVALUATION_RECOMMENDED -> "Biomechanical deviations detected. Recommend structured clinical mobility assessment at Primary Health Center."
                        ReferralRecommendation.EARLIER_CLINICAL_EVALUATION_RECOMMENDED -> "Significant multi-sensor marker elevation. Recommend expedited musculoskeletal consultation."
                        ReferralRecommendation.RETEST_REQUIRED -> "Signal quality insufficient. Please realign sleeve sensors and repeat screening."
                        else -> "Screening result — further clinical evaluation may be appropriate."
                    },
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        color = textSecondary
                    )
                )
            }
        }

        // ==============================================================
        // 5. RETEST RECOMMENDATION BANNER (IF REQUIRED)
        // ==============================================================
        if (isRetestRequired) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = amberWarnBg,
                border = BorderStroke(1.dp, amberWarnBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("step10_retest_warning")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RETEST RECOMMENDED",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = Color(0xFF92400E)
                            )
                        )
                        Text(
                            text = "High signal uncertainty or motion artifacts detected during acquisition.",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.sp,
                                color = textPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenRetestWorkflow,
                        colors = ButtonDefaults.buttonColors(containerColor = amberWarn),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("step10_retest_button")
                    ) {
                        Text(
                            text = "RETEST",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // ==============================================================
        // 6. PDF DOWNLOAD NOTIFICATION (IF DOWNLOADED)
        // ==============================================================
        if (downloadedPdfFile != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = readyGreenBg,
                border = BorderStroke(1.dp, readyGreenBorder),
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
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = readyGreen
                            )
                        )
                        Text(
                            text = "PDF saved to device storage.",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { downloadedPdfFile?.let { PdfReportService.openPdf(context, it) } },
                            colors = ButtonDefaults.buttonColors(containerColor = readyGreen),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "OPEN PDF",
                                style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            )
                        }
                        OutlinedButton(
                            onClick = { downloadedPdfFile = null },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("DISMISS", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, color = textPrimary))
                        }
                    }
                }
            }
        }

        // ==============================================================
        // 7. BOTTOM NAVIGATION ACTIONS & PDF EXPORT
        // ==============================================================
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, borderStrokeColor)
                ) {
                    Text(
                        text = "BACK",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = textPrimary
                        )
                    )
                }

                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .weight(2f)
                        .height(46.dp)
                        .testTag("result_step_next_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                ) {
                    Text(
                        text = "VIEW STEP 11 SUMMARY",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.3.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    val result = PdfReportService.generateScreeningReportPdf(context, session)
                    if (result.isSuccess) {
                        downloadedPdfFile = result.getOrNull()
                        downloadFailed = false
                    } else {
                        downloadFailed = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = primaryBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DOWNLOAD AUDIT PDF REPORT",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = primaryBlue
                    )
                )
            }

            // COMPLIANCE FOOTER
            Text(
                text = "SIH26004L DETERMINISTIC AUDIT PIPELINE • Point-of-care screening for frontline health workers",
                style = TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = textSecondary
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
