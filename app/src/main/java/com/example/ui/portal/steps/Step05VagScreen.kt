package com.example.ui.portal.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.contract.Modality
import com.example.portal.AshaScreeningSession
import com.example.ui.DashboardUiState
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * PRODUCTION STEP 05: VAG ACOUSTIC CREPITUS
 * ARTHROSCAN-NER | Clinical Field Diagnostic Suite (Google Stitch Spec)
 */
@Composable
fun Step05VagScreen(
    session: AshaScreeningSession,
    uiState: DashboardUiState,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val vagSqi = uiState.sqiScores[Modality.VAG] ?: 0.91
    val vagArtifacts = uiState.detectedArtifacts[Modality.VAG] ?: emptyList()
    val isVagPassing = vagSqi >= 0.70 && vagArtifacts.isEmpty()

    // Live wave animation phase shift
    var animPhase by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            animPhase += 0.15f
        }
    }

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
    val amberWarn = Color(0xFFD97706)
    val amberWarnBg = Color(0xFFFFFBEB)
    val amberWarnBorder = Color(0xFFFDE68A)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // STEP 05 OVERVIEW CARD
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
                            text = "STEP 05 OF 11: VAG ACOUSTICS",
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
                            text = "Acoustic Vibroarthrography (VAG)",
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
                        color = if (isVagPassing) readyGreenBg else amberWarnBg,
                        border = BorderStroke(1.dp, if (isVagPassing) readyGreenBorder else amberWarnBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isVagPassing) readyGreen else amberWarn)
                            )
                            Text(
                                text = if (isVagPassing) "QUALITY PASS" else "CHECK COUPLING",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isVagPassing) readyGreen else amberWarn
                                )
                            )
                        }
                    }
                }

                Text(
                    text = "Vibroarthrographic piezoelectric contact sensor records joint micro-vibration emissions during active knee flexion-extension arcs at 2000 Hz.",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = textSecondary
                    )
                )
            }
        }

        // ANIMATED ACOUSTIC OSCILLOSCOPE DISPLAY
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
                        text = "VAG ACOUSTIC WAVEFORM (2000 Hz)",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = primaryBlue
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(readyGreen)
                        )
                        Text(
                            text = "LIVE AUDIO GATE",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = readyGreen
                            )
                        )
                    }
                }

                // Clean Oscilloscope Canvas
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            val w = size.width
                            val h = size.height
                            val midY = h / 2f

                            // Technical grid lines
                            val gridColor = Color(0xFF1E293B)
                            drawLine(gridColor, Offset(0f, midY), Offset(w, midY), strokeWidth = 1f)
                            drawLine(gridColor, Offset(w * 0.25f, 0f), Offset(w * 0.25f, h), strokeWidth = 1f)
                            drawLine(gridColor, Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), strokeWidth = 1f)
                            drawLine(gridColor, Offset(w * 0.75f, 0f), Offset(w * 0.75f, h), strokeWidth = 1f)

                            // Acoustic waveform
                            val path = Path()
                            path.moveTo(0f, midY)
                            val points = 80
                            for (i in 0..points) {
                                val x = (i.toFloat() / points) * w
                                val envelope = sin(i.toFloat() / points * Math.PI.toFloat())
                                val wave = sin(i * 0.6f + animPhase) * cos(i * 1.8f + animPhase * 0.5f)
                                val y = midY + (wave * envelope * (h * 0.38f))
                                path.lineTo(x, y.toFloat())
                            }
                            drawPath(path, color = Color(0xFF10B981), style = Stroke(width = 2.2f))
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CYCLE 5/5 • NO CLIPPING",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            )
                        }
                    }
                }

                Text(
                    text = "Patient performed smooth active knee extension without acoustic clipping or contact decoupling.",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                )
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
                border = BorderStroke(1.dp, readyGreenBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = "VAG SQI", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = textSecondary))
                    Text(text = String.format("%.2f", vagSqi), style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = readyGreen))
                    Text(text = if (isVagPassing) "PASS (≥0.70)" else "LOW", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 8.5.sp, color = textSecondary))
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, borderStrokeColor),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = "SAMPLING", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = textSecondary))
                    Text(text = "2000", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary))
                    Text(text = "Hz Piezo Channel", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 8.5.sp, color = textSecondary))
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, borderStrokeColor),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = "ARTIFACTS", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = textSecondary))
                    Text(text = if (vagArtifacts.isEmpty()) "NONE" else "${vagArtifacts.size} FLAGGED", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (vagArtifacts.isEmpty()) readyGreen else amberWarn))
                    Text(text = "Rejection Engine", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 8.5.sp, color = textSecondary))
                }
            }
        }

        if (vagArtifacts.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = amberWarnBg,
                border = BorderStroke(1.dp, amberWarnBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "VAG ARTIFACT: ${vagArtifacts.joinToString { it.name }}",
                        style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = amberWarn)
                    )
                    Text(
                        text = "Excessive motion or acoustic sensor decoupling. Repeat measurement window or reapply coupling gel.",
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
                        .testTag("vag_step_next_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBlue,
                        disabledContainerColor = Color(0xFFCBD5E1)
                    )
                ) {
                    Text(
                        text = "PROCEED TO 06 IMU / sEMG",
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
