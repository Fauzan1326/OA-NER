package com.example.ui.portal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.auth.UserAccount
import com.example.core.config.ProfileType
import com.example.ui.DashboardUiState
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PRODUCTION FIELD SCREENING SESSION PREPARATION MODAL
 * ARTHROSCAN-NER | Clinical Field Diagnostic Suite
 *
 * Implements strict clinical safety:
 * - Single production workflow (no prototype / research / demo mode selectors)
 * - Real hardware link verification with blocking gate
 * - Mandatory participant briefing confirmation checkbox
 * - Privacy-conscious pseudonymous participant identifier
 */
@Composable
fun AshaSessionCreationDialog(
    worker: UserAccount,
    uiState: DashboardUiState,
    onStartSession: (ProfileType, String) -> Unit,
    onDismiss: () -> Unit,
    onCheckDevice: (() -> Unit)? = null
) {
    val tempSessionId = remember { "SES-NER-${(10000 + (System.currentTimeMillis() % 90000))}" }
    val formattedTimestamp = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
    }
    var participantId by remember { mutableStateOf("PART-ANON-420") }
    var isBriefingConfirmed by remember { mutableStateOf(false) }

    // Real hardware state from UI state
    val isDeviceReady = uiState.isConnected || uiState.isStreaming

    // Clinical design palette
    val surfaceCard = Color.White
    val surfaceLow = Color(0xFFF1F5F9)
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A).copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = surfaceCard,
                border = BorderStroke(1.dp, borderStrokeColor),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Grab Handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFCBD5E1))
                    )

                    // Header & Dismiss
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NEW FIELD SCREENING SESSION",
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Prepare the participant and verify the screening system before starting.",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 12.5.sp,
                                    color = textSecondary
                                )
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(surfaceLow)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = borderStrokeColor, thickness = 1.dp)

                    // Session Meta Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = surfaceLow,
                        border = BorderStroke(1.dp, borderStrokeColor),
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = primaryBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "SESSION DETAILS",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.5.sp,
                                            color = textSecondary
                                        )
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFDBEAFE)
                                ) {
                                    Text(
                                        text = "OFFICIAL DISPATCH",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp,
                                            color = primaryBlue
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "SESSION ID",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 10.5.sp,
                                            color = textSecondary
                                        )
                                    )
                                    Text(
                                        text = tempSessionId,
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = textPrimary
                                        )
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "DATE / TIME",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 10.5.sp,
                                            color = textSecondary
                                        )
                                    )
                                    Text(
                                        text = formattedTimestamp,
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.5.sp,
                                            color = textPrimary
                                        )
                                    )
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "WORKER",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 10.5.sp,
                                            color = textSecondary
                                        )
                                    )
                                    Text(
                                        text = worker.fullName,
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.5.sp,
                                            color = textPrimary
                                        )
                                    )
                                    Text(
                                        text = "ASHA WORKER",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            color = textSecondary
                                        )
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "CENTER",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 10.5.sp,
                                            color = textSecondary
                                        )
                                    )
                                    Text(
                                        text = worker.assignedCenter,
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = textPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Device Status Section (Strict Safety Verification)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDeviceReady) readyGreenBg else errorRedBg,
                        border = BorderStroke(1.dp, if (isDeviceReady) readyGreenBorder else errorRedBorder),
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isDeviceReady) readyGreen else errorRed)
                                    )
                                    Text(
                                        text = if (isDeviceReady) "DEVICE READY" else "DEVICE NOT READY",
                                        style = TextStyle(
                                            fontFamily = SoraFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isDeviceReady) readyGreen else errorRed
                                        )
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isDeviceReady) readyGreen.copy(alpha = 0.15f) else errorRed.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isDeviceReady) "VERIFIED" else "ACTION REQUIRED",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = if (isDeviceReady) readyGreen else errorRed
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = if (isDeviceReady) {
                                    "Sensor system is ready to begin screening."
                                } else {
                                    "Connect the ARTHROSCAN sensor system before starting the screening."
                                },
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 12.sp,
                                    color = if (isDeviceReady) Color(0xFF065F46) else Color(0xFF991B1B)
                                )
                            )

                            if (!isDeviceReady && onCheckDevice != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = onCheckDevice,
                                        modifier = Modifier.testTag("check_device_button"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.White
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = primaryBlue,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "CHECK DEVICE",
                                            style = TextStyle(
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp,
                                                color = textPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Participant ID (Privacy-Conscious Input)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Participant ID (Privacy-Conscious Pseudonym)",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    color = textPrimary
                                )
                            )
                            Text(
                                text = "REQUIRED",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = primaryBlue
                                )
                            )
                        }

                        OutlinedTextField(
                            value = participantId,
                            onValueChange = { participantId = it },
                            placeholder = { Text("e.g. PART-ANON-420", color = textSecondary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (participantId.isNotBlank()) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = readyGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            textStyle = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                color = textPrimary
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = surfaceLow,
                                unfocusedContainerColor = surfaceLow,
                                focusedBorderColor = primaryBlue,
                                unfocusedBorderColor = borderStrokeColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("participant_id_input")
                        )

                        Text(
                            text = "Use a pseudonymous participant ID. Avoid entering unnecessary personally identifiable information.",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.sp,
                                color = textSecondary
                            ),
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }

                    // Mandatory Confirmation Checkbox
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = surfaceLow,
                        border = BorderStroke(1.dp, borderStrokeColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isBriefingConfirmed = !isBriefingConfirmed }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = isBriefingConfirmed,
                                onCheckedChange = { isBriefingConfirmed = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = primaryBlue,
                                    uncheckedColor = textSecondary
                                ),
                                modifier = Modifier
                                    .size(20.dp)
                                    .testTag("research_ack_checkbox")
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "I confirm that the participant has been briefed on the non-diagnostic screening process and that the information entered is accurate and appropriate for this screening session.",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = textPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Action Buttons (Primary & Cancel)
                    val canStart = isDeviceReady && isBriefingConfirmed && participantId.isNotBlank()

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (canStart) {
                                    val mode = if (uiState.isConnected) ProfileType.HARDWARE else uiState.profile
                                    onStartSession(mode, participantId.trim())
                                }
                            },
                            enabled = canStart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("confirm_create_session_button")
                                .testTag("start_session_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryBlue,
                                disabledContainerColor = Color(0xFFE2E8F0),
                                disabledContentColor = textSecondary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "START SCREENING SESSION",
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }

                        // Disabled hint warning
                        if (!canStart) {
                            val hintText = when {
                                !isDeviceReady -> "Connect the ARTHROSCAN sensor system before starting the screening."
                                !isBriefingConfirmed -> "Confirmation checkbox must be acknowledged before starting."
                                else -> "Enter a valid participant ID."
                            }
                            Text(
                                text = hintText,
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.5.sp,
                                    color = errorRed
                                ),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("cancel_session_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, borderStrokeColor),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = surfaceLow
                            )
                        ) {
                            Text(
                                text = "CANCEL",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp,
                                    color = textPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
