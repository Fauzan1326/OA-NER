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
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * PRODUCTION STEP 06: KINEMATICS (IMU) & MUSCLE ACTIVATION (sEMG)
 * ARTHROSCAN-NER | Clinical Field Diagnostic Suite (Google Stitch Spec)
 */
@Composable
fun Step06ImuSemgScreen(
    session: AshaScreeningSession,
    uiState: DashboardUiState,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val imuSqi = uiState.sqiScores[Modality.IMU] ?: 0.95
    val semgSqi = uiState.sqiScores[Modality.SEMG] ?: 0.90
    val imuAvailable = uiState.modalityStates[Modality.IMU] != null || session.mode == com.example.core.config.ProfileType.DEMO
    val semgAvailable = uiState.modalityStates[Modality.SEMG] != null || session.mode == com.example.core.config.ProfileType.DEMO

    // Live fluctuating signal simulation using coroutines
    var animTick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(80)
            animTick = (animTick + 1) % 100
        }
    }

    val liveAccel = String.format("%.2f", 9.81 + sin(animTick * 0.2) * 0.18)
    val liveGyro = String.format("%.2f", 0.85 + cos(animTick * 0.15) * 0.12)
    val liveKneeAngle = String.format("%.1f°", 74.5 + sin(animTick * 0.1) * 3.2)
    val liveSemgCh1 = String.format("%.1f", 42.8 + sin(animTick * 0.25) * 4.5)
    val liveSemgCh2 = String.format("%.1f", 38.1 + cos(animTick * 0.22) * 3.8)

    // Clinical light mode design palette
    val surfaceCard = Color.White
    val surfaceCardLow = Color(0xFFF1F5F9)
    val borderStrokeColor = Color(0xFFE2E8F0)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)
    val primaryBlue = Color(0xFF2563EB)
    val secondaryTeal = Color(0xFF0D9488)
    val readyGreen = Color(0xFF059669)
    val readyGreenBg = Color(0xFFECFDF5)
    val readyGreenBorder = Color(0xFFA7F3D0)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // STEP 06 OVERVIEW CARD
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
                            text = "STEP 06 OF 11: IMU & sEMG",
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
                            text = "Kinematic Goniometry & sEMG",
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
                        color = readyGreenBg,
                        border = BorderStroke(1.dp, readyGreenBorder)
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
                                    .background(readyGreen)
                            )
                            Text(
                                text = "DUAL STREAM LIVE",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = readyGreen
                                )
                            )
                        }
                    }
                }

                Text(
                    text = "Dual 6-DOF IMU arrays quantify knee flexion/extension goniometry while 2-channel sEMG evaluates quadriceps co-activation during active joint excursion.",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = textSecondary
                    )
                )
            }
        }

        // IMU KINEMATICS CARD
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
                        text = "IMU KINEMATIC STREAM (100 Hz / 200 Hz)",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = primaryBlue
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (imuAvailable) readyGreenBg else surfaceCardLow
                    ) {
                        Text(
                            text = if (imuAvailable) "SQI 0.95 (PASS)" else "OFFLINE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = if (imuAvailable) readyGreen else textSecondary
                            )
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
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("ACCEL MAG", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = textSecondary))
                            Text(liveAccel, style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary))
                            Text("m/s²", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 8.5.sp, color = textSecondary))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = surfaceCardLow,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("GYRO RMS", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = textSecondary))
                            Text(liveGyro, style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary))
                            Text("rad/s", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 8.5.sp, color = textSecondary))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = surfaceCardLow,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("KNEE ANGLE", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = textSecondary))
                            Text(liveKneeAngle, style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = primaryBlue))
                            Text("Goniometry", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 8.5.sp, color = textSecondary))
                        }
                    }
                }
            }
        }

        // sEMG MUSCLE ACTIVATION CARD
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
                        text = "sEMG MUSCLE ACTIVATION (1000 Hz)",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = secondaryTeal
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (semgAvailable) readyGreenBg else surfaceCardLow
                    ) {
                        Text(
                            text = if (semgAvailable) "SQI 0.90 (PASS)" else "OFFLINE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = if (semgAvailable) readyGreen else textSecondary
                            )
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
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("CH1 (VM)", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = textSecondary))
                            Text(liveSemgCh1, style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = secondaryTeal))
                            Text("μV RMS", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 8.5.sp, color = textSecondary))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = surfaceCardLow,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("CH2 (RF)", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = textSecondary))
                            Text(liveSemgCh2, style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = secondaryTeal))
                            Text("μV RMS", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 8.5.sp, color = textSecondary))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = surfaceCardLow,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("CO-CONTRACT", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = textSecondary))
                            Text("0.78", style = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary))
                            Text("Ratio", style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 8.5.sp, color = textSecondary))
                        }
                    }
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
                        .testTag("imu_semg_step_next_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBlue,
                        disabledContainerColor = Color(0xFFCBD5E1)
                    )
                ) {
                    Text(
                        text = "PROCEED TO 07 CONTEXT",
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
