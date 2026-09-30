package com.example.ui.portal.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.portal.SleevePlacementCheck
import com.example.ui.theme.*

/**
 * PRODUCTION STEP 02: KNEE SLEEVE PLACEMENT & SENSOR SEATING
 * ARTHROSCAN-NER | Clinical Field Diagnostic Suite (Google Stitch Spec)
 */
@Composable
fun Step02SleeveScreen(
    currentCheck: SleevePlacementCheck,
    onUpdateCheck: (SleevePlacementCheck) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var alignment by remember { mutableStateOf(currentCheck.sensorAlignment) }
    var tension by remember { mutableStateOf(currentCheck.sleeveTension) }
    var contact by remember { mutableStateOf(currentCheck.contactQuality) }
    var orientation by remember { mutableStateOf(currentCheck.orientation) }
    var isConfirmed by remember { mutableStateOf(currentCheck.isConfirmed) }

    val isAllPass = alignment == "PASS" && tension == "PASS" && contact == "PASS" && orientation == "PASS"

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
    val errorRed = Color(0xFFDC2626)
    val errorRedBg = Color(0xFFFEF2F2)
    val purpleAccent = Color(0xFF7C3AED)
    val purpleBg = Color(0xFFF5F3FF)
    val tealAccent = Color(0xFF0D9488)
    val tealBg = Color(0xFFF0FDFA)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // STEP 02 PROTOCOL OVERVIEW CARD
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
                            text = "STEP 02 OF 11: KNEE SLEEVE & FIT",
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
                            text = "Placement & Sensor Seating",
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
                            text = "FIELD AUDIT PROTOCOL",
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
                    text = "Ensure the multi-sensor elastic compression sleeve is seated securely over the patellar and tibial landmarks before zero-offset calibration.",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = textSecondary
                    )
                )
            }
        }

        // SLEEVE SENSOR TOPOLOGY SCHEMATIC
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
                        text = "SLEEVE SENSOR TOPOLOGY (PROTOTYPE DESIGN)",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = primaryBlue
                        )
                    )
                    Text(
                        text = "REF: MK-IV SLEEVE",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.5.sp,
                            color = textSecondary
                        )
                    )
                }

                // Schematic Frame
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Row 1: Upper Thigh Sensor Nodes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SchematicSensorBadge(
                                label = "sEMG CH1",
                                loc = "Vastus Medialis",
                                textColor = Color(0xFF0F766E),
                                bgColor = tealBg,
                                borderColor = Color(0xFF99F6E4),
                                modifier = Modifier.weight(1f)
                            )
                            SchematicSensorBadge(
                                label = "IMU-F",
                                loc = "Distal Femur",
                                textColor = Color(0xFF1E40AF),
                                bgColor = Color(0xFFEFF6FF),
                                borderColor = Color(0xFFBFDBFE),
                                modifier = Modifier.weight(1f)
                            )
                            SchematicSensorBadge(
                                label = "sEMG CH2",
                                loc = "Rectus Femoris",
                                textColor = Color(0xFF0F766E),
                                bgColor = tealBg,
                                borderColor = Color(0xFF99F6E4),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 2: Mid Knee Patella Central Junction
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SchematicSensorBadge(
                                label = "RF RESONATOR",
                                loc = "0.5–3.0 GHz Band",
                                textColor = Color(0xFF581C87),
                                bgColor = purpleBg,
                                borderColor = Color(0xFFDDD6FE),
                                modifier = Modifier.weight(1f)
                            )

                            // Center Patella Landmark
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .border(2.dp, primaryBlue, CircleShape)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "PATELLA",
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = primaryBlue
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(primaryBlue)
                                    )
                                }
                            }

                            SchematicSensorBadge(
                                label = "VAG ACOUSTIC",
                                loc = "Medial Plateau",
                                textColor = Color(0xFF065F46),
                                bgColor = readyGreenBg,
                                borderColor = readyGreenBorder,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 3: Proximal Tibia IMU Sensor
                        Box(
                            modifier = Modifier.width(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SchematicSensorBadge(
                                label = "IMU-T",
                                loc = "Proximal Tibia",
                                textColor = Color(0xFF1E40AF),
                                bgColor = Color(0xFFEFF6FF),
                                borderColor = Color(0xFFBFDBFE),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Text(
                    text = "Schematic sensor orientation for research prototype. Not an anatomical 3D scan.",
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        color = textSecondary
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        // FIT STATUS ALERT BANNER
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isAllPass) readyGreenBg else amberWarnBg,
            border = BorderStroke(1.dp, if (isAllPass) readyGreenBorder else amberWarnBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isAllPass) readyGreen.copy(alpha = 0.2f) else amberWarn.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAllPass) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isAllPass) readyGreen else amberWarn,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAllPass) "FIT STATUS: READY" else "FIT STATUS: ADJUST REQUIRED",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = if (isAllPass) Color(0xFF065F46) else Color(0xFF78350F)
                            )
                        )
                        Text(
                            text = if (isAllPass) "5/5 NODES SEATED" else "4/5 NODES SEATED",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (isAllPass) readyGreen else amberWarn
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isAllPass)
                            "All RF, acoustic, IMU, and dual-sEMG impedance lines calibrated within acceptable skin impedance limits."
                        else
                            "Check Vastus Medialis skin electrode seating. Tap [PASS] to override or verify contact quality.",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = if (isAllPass) Color(0xFF065F46) else Color(0xFF78350F)
                        )
                    )
                }
            }
        }

        // PLACEMENT CHECKLIST & TELEMETRY
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
                        text = "PLACEMENT CHECKLIST & TELEMETRY:",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = primaryBlue
                        )
                    )
                    Text(
                        text = "TAP TO TOGGLE",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            color = textSecondary
                        )
                    )
                }

                PlacementCheckRowItem(
                    label = "Sensor Alignment",
                    subnote = "Centered on patella ring",
                    current = alignment,
                    onSelect = { alignment = it }
                )
                PlacementCheckRowItem(
                    label = "Sleeve Tension",
                    subnote = "18.2 N compression",
                    current = tension,
                    onSelect = { tension = it }
                )
                PlacementCheckRowItem(
                    label = "Contact Quality (sEMG)",
                    subnote = if (contact == "PASS") "Skin Contact Optimal (0.8 kΩ)" else "Check Vastus Medialis Lead",
                    current = contact,
                    onSelect = { contact = it }
                )
                PlacementCheckRowItem(
                    label = "Sleeve Orientation",
                    subnote = "Vertical axis 0° deflection",
                    current = orientation,
                    onSelect = { orientation = it }
                )
            }
        }

        // FIELD OPERATIONAL INSTRUCTIONS
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = surfaceCard,
            border = BorderStroke(1.dp, borderStrokeColor),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = primaryBlue,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "FIELD OPERATIONAL INSTRUCTIONS:",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = textPrimary
                        )
                    )
                }

                val instructions = listOf(
                    "1. Align sleeve center aperture directly over the patella ring.",
                    "2. Ensure sensing electrode elements are seated flush on skin.",
                    "3. Avoid excessive sleeve rotation, bunching, or twisting.",
                    "4. Confirm stable acoustic and RF sensor skin coupling.",
                    "5. Proceed to hardware zero-offset baseline calibration next."
                )

                instructions.forEach { inst ->
                    Text(
                        text = inst,
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            color = textSecondary
                        )
                    )
                }
            }
        }

        // CONFIRMATION CHECKBOX
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = surfaceCard,
            border = BorderStroke(1.dp, if (isConfirmed) Color(0xFFBFDBFE) else borderStrokeColor),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isConfirmed = !isConfirmed }
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = isConfirmed,
                    onCheckedChange = { isConfirmed = it },
                    modifier = Modifier.testTag("sleeve_confirm_checkbox"),
                    colors = CheckboxDefaults.colors(
                        checkedColor = primaryBlue,
                        uncheckedColor = textSecondary
                    )
                )
                Text(
                    text = "I confirm sleeve is aligned and seated as per SIH26004 field protocol.",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        color = textPrimary,
                        fontWeight = if (isConfirmed) FontWeight.SemiBold else FontWeight.Normal
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
                    onClick = {
                        onUpdateCheck(
                            SleevePlacementCheck(
                                sensorAlignment = alignment,
                                sleeveTension = tension,
                                contactQuality = contact,
                                orientation = orientation,
                                isConfirmed = isConfirmed
                            )
                        )
                        onNext()
                    },
                    enabled = isConfirmed && isAllPass,
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                        .testTag("sleeve_step_next_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBlue,
                        disabledContainerColor = Color(0xFFCBD5E1)
                    )
                ) {
                    Text(
                        text = "CONFIRM & CALIBRATE",
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
private fun SchematicSensorBadge(
    label: String,
    loc: String,
    textColor: Color,
    bgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp,
                    color = textColor
                )
            )
            Text(
                text = loc,
                style = TextStyle(
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 8.5.sp,
                    color = textColor.copy(alpha = 0.85f)
                )
            )
        }
    }
}

@Composable
private fun PlacementCheckRowItem(
    label: String,
    subnote: String,
    current: String,
    onSelect: (String) -> Unit
) {
    val isItemPass = current == "PASS"
    val isItemWarn = current == "WARN"

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isItemPass) Color.White else if (isItemWarn) Color(0xFFFFFBEB) else Color(0xFFFEF2F2),
        border = BorderStroke(
            1.dp,
            if (isItemPass) Color(0xFFE2E8F0) else if (isItemWarn) Color(0xFFFDE68A) else Color(0xFFFECACA)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isItemPass) Color(0xFF059669) else if (isItemWarn) Color(0xFFD97706) else Color(0xFFDC2626)
                            )
                    )
                    Text(
                        text = label,
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF0F172A)
                        )
                    )
                }
                Text(
                    text = subnote,
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        color = Color(0xFF64748B)
                    ),
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("PASS", "WARN", "REPOSITION").forEach { option ->
                    val selected = current == option
                    val optColor = when (option) {
                        "PASS" -> Color(0xFF059669)
                        "WARN" -> Color(0xFFD97706)
                        else -> Color(0xFFDC2626)
                    }
                    val optBg = when (option) {
                        "PASS" -> Color(0xFFECFDF5)
                        "WARN" -> Color(0xFFFFFBEB)
                        else -> Color(0xFFFEF2F2)
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (selected) optBg else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (selected) optColor else Color.Transparent),
                        modifier = Modifier.clickable { onSelect(option) }
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 9.sp,
                                color = if (selected) optColor else Color(0xFF94A3B8)
                            )
                        )
                    }
                }
            }
        }
    }
}
