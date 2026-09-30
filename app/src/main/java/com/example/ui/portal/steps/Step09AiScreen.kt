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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.contract.Modality
import com.example.portal.AshaScreeningSession
import com.example.ui.DashboardUiState
import com.example.ui.theme.*

/**
 * PRODUCTION STEP 09: MODULE 5 MODALITY AI MODELS
 * ARTHROSCAN-NER | Clinical Field Diagnostic Suite (Google Stitch Spec)
 */
@Composable
fun Step09AiScreen(
    session: AshaScreeningSession,
    uiState: DashboardUiState,
    onRunInference: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val modalities = listOf(Modality.IMU, Modality.VAG, Modality.SEMG, Modality.RF)

    // Clinical light mode design palette
    val surfaceCard = Color.White
    val surfaceCardLow = Color(0xFFF1F5F9)
    val borderStrokeColor = Color(0xFFE2E8F0)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)
    val primaryBlue = Color(0xFF2563EB)
    val readyGreen = Color(0xFF059669)
    val readyGreenBg = Color(0xFFECFDF5)
    val readyGreenBorder = Color(0xFFA7F3D0)
    val purpleAccent = Color(0xFF7C3AED)
    val purpleBg = Color(0xFFF5F3FF)
    val purpleBorder = Color(0xFFDDD6FE)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // STEP 09 OVERVIEW CARD
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = surfaceCard,
            border = BorderStroke(1.dp, borderStrokeColor),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "STEP 09 OF 11: MULTIMODAL AI",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                letterSpacing = 0.8.sp,
                                color = primaryBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Late Fusion Neural Engine",
                            style = TextStyle(
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = textPrimary
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = surfaceCardLow
                    ) {
                        Text(
                            text = "LOCKED WEIGHTS",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = textSecondary
                            )
                        )
                    }
                }

                Text(
                    text = "Independent neural networks evaluate modality features with locked normalization parameters and Dirichlet calibrated uncertainty estimation.",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = textSecondary
                    )
                )
            }
        }

        // MODALITY AI MODELS CARDS
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            modalities.forEach { mod ->
                val prediction = uiState.latestPredictions[mod]
                val isRf = mod == Modality.RF
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = surfaceCard,
                    border = BorderStroke(1.dp, if (isRf) purpleBorder else borderStrokeColor),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MODEL: ${mod.name} (${if (isRf) "M5_RF_DIELECTRIC_V1" else "M5_${mod.name}_MODEL_V1"})",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isRf) purpleAccent else textPrimary
                                )
                            )
                            if (isRf) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = purpleBg,
                                    border = BorderStroke(1.dp, purpleBorder)
                                ) {
                                    Text(
                                        text = "EXPERIMENTAL RF",
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.sp,
                                            color = purpleAccent
                                        )
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status: ${if (prediction != null) prediction.status.name else "READY FOR INFERENCE"}",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = if (prediction != null) readyGreen else textSecondary
                                )
                            )
                            Text(
                                text = "Score: ${if (prediction?.score != null) String.format("%.3f", prediction.score) else "--"}",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = primaryBlue
                                )
                            )
                            Text(
                                text = "Uncertainty: ${if (prediction?.calibratedUncertainty != null) String.format("%.3f", prediction.calibratedUncertainty) else "--"}",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = textSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // ACTION BUTTON: RUN INFERENCE
        Button(
            onClick = onRunInference,
            enabled = !uiState.isAnalyzing,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("run_ai_inference_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryBlue,
                disabledContainerColor = Color(0xFFCBD5E1)
            )
        ) {
            if (uiState.isAnalyzing) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = uiState.analysisStepDescription ?: "ANALYZING MULTIMODAL FEATURES...",
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            } else {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "EXECUTE MULTIMODAL FUSION & UNCERTAINTY",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }

        // NAVIGATION ACTIONS
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
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
                    enabled = uiState.screeningDecision != null,
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                        .testTag("ai_step_next_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBlue,
                        disabledContainerColor = Color(0xFFCBD5E1)
                    )
                ) {
                    Text(
                        text = "VIEW SCREENING RESULT",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
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
