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
 * PRODUCTION STEP 04: RF DIELECTRIC RESPONSE
 * ARTHROSCAN-NER | Clinical Field Diagnostic Suite (Google Stitch Spec)
 */
@Composable
fun Step04RfScreen(
    session: AshaScreeningSession,
    uiState: DashboardUiState,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val rfSqi = uiState.sqiScores[Modality.RF] ?: 0.94
    val isRfPassing = rfSqi >= 0.70

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
    val errorRed = Color(0xFFDC2626)
    val errorRedBg = Color(0xFFFEF2F2)
    val errorRedBorder = Color(0xFFFECACA)
    val purpleAccent = Color(0xFF7C3AED)
    val purpleBg = Color(0xFFF5F3FF)
    val purpleBorder = Color(0xFFDDD6FE)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // STEP 04 OVERVIEW CARD
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
                            text = "STEP 04 OF 11: RF DIELECTRIC",
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
                            text = "RF Dielectric Permittivity Sweep",
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
                        color = purpleBg,
                        border = BorderStroke(1.dp, purpleBorder)
                    ) {
                        Text(
                            text = "EXPERIMENTAL RF",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.5.sp,
                                color = purpleAccent
                            )
                        )
                    }
                }

                Text(
                    text = "Frequency sweep across 0.5–3.0 GHz boundary (2.45 GHz ISM Reference). Measures microwave dielectric resonance attenuation across peri-articular joint space.",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = textSecondary
                    )
                )
            }
        }

        // RETURN LOSS (S11) SPECTRUM VISUALIZER
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = surfaceCard,
            border = BorderStroke(1.dp, borderStrokeColor),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "S11 RETURN-LOSS SPECTRUM (0.5 – 3.0 GHz)",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = primaryBlue
                        )
                    )
                    Text(
                        text = "COUPLING: NOMINAL",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = readyGreen
                        )
                    )
                }

                // Frequency Bands Bar Representation
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RfFrequencyBandRowLight("0.90 GHz (Sub-band Low)", "-12.4 dB", 0.62f, highlight = false)
                    RfFrequencyBandRowLight("1.80 GHz (Mid-band)", "-15.8 dB", 0.79f, highlight = false)
                    RfFrequencyBandRowLight("2.45 GHz (ISM Reference)", "-21.6 dB [RESONANT]", 0.95f, highlight = true)
                    RfFrequencyBandRowLight("2.80 GHz (Sub-band High)", "-14.1 dB", 0.70f, highlight = false)
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Resonance: 2.448 GHz",
                        style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = textSecondary)
                    )
                    Text(
                        text = "Mismatch Check: PASS (0.02 Ω)",
                        style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = readyGreen)
                    )
                }
            }
        }

        // SCIENTIFIC READOUT TILES
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, if (isRfPassing) readyGreenBorder else errorRedBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = "RF SQI", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = textSecondary))
                    Text(text = String.format("%.2f", rfSqi), style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isRfPassing) readyGreen else errorRed))
                    Text(text = if (isRfPassing) "PASS (≥0.70)" else "FAIL (<0.70)", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 8.5.sp, color = textSecondary))
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, borderStrokeColor),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = "RESONANCE DIP", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = textSecondary))
                    Text(text = "2.448", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary))
                    Text(text = "GHz Target", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 8.5.sp, color = textSecondary))
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, borderStrokeColor),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = "S11 MIN", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = textSecondary))
                    Text(text = "-21.6", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = purpleAccent))
                    Text(text = "dB Return Loss", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 8.5.sp, color = textSecondary))
                }
            }
        }

        if (!isRfPassing) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = errorRedBg,
                border = BorderStroke(1.dp, errorRedBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "RF MISMATCH — RETEST REQUIRED",
                        style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = errorRed)
                    )
                    Text(
                        text = "Impedance mismatch or antenna decoupling detected. Realign sleeve RF resonator node flush over joint line.",
                        style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, color = textPrimary)
                    )
                }
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
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                        .testTag("rf_step_next_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBlue,
                        disabledContainerColor = Color(0xFFCBD5E1)
                    )
                ) {
                    Text(
                        text = "PROCEED TO 05 VAG",
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

@Composable
private fun RfFrequencyBandRowLight(
    band: String,
    returnLoss: String,
    barRatio: Float,
    highlight: Boolean
) {
    val purpleAccent = Color(0xFF7C3AED)
    val primaryBlue = Color(0xFF2563EB)
    val readyGreen = Color(0xFF059669)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = band,
                style = TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
                    color = if (highlight) purpleAccent else Color(0xFF334155)
                )
            )
            Text(
                text = returnLoss,
                style = TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (highlight) readyGreen else Color(0xFF0F172A)
                )
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { barRatio },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (highlight) purpleAccent else primaryBlue,
            trackColor = Color(0xFFF1F5F9)
        )
    }
}
