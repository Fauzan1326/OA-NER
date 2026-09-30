package com.example.ui.portal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portal.AshaStep
import com.example.portal.StepStatus
import com.example.ui.theme.*

/**
 * PRODUCTION 11-STEP PROGRESS RAIL FOR ASHA FIELD SCREENING
 * ARTHROSCAN-NER | Canonical 11-step protocol derived directly from AshaStep
 */
@Composable
fun AshaStepperBar(
    currentStep: AshaStep,
    stepStatuses: Map<AshaStep, StepStatus>,
    onStepSelected: (AshaStep) -> Unit,
    modifier: Modifier = Modifier
) {
    // Clinical Design Tokens
    val barBg = Color(0xFFF8FAFC)
    val borderStrokeColor = Color(0xFFE2E8F0)
    val currentBg = Color(0xFFEFF6FF)
    val currentBorder = Color(0xFF2563EB)
    val currentText = Color(0xFF1D4ED8)
    val completedBg = Color(0xFFECFDF5)
    val completedBorder = Color(0xFFA7F3D0)
    val completedText = Color(0xFF059669)
    val pendingBg = Color.White
    val pendingBorder = Color(0xFFE2E8F0)
    val pendingText = Color(0xFF64748B)

    Surface(
        color = barBg,
        border = BorderStroke(1.dp, borderStrokeColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AshaStep.values().forEach { step ->
                val status = stepStatuses[step] ?: StepStatus.PENDING
                val isCurrent = step == currentStep
                val stepNum = step.stepNumber.toString().padStart(2, '0')
                val cleanTitle = if (step.code.matches(Regex("""^\d+\s+.*"""))) {
                    step.code.substringAfter(' ')
                } else {
                    step.code
                }

                val (badgeBg, badgeBorder, textColor, displayText) = when {
                    status == StepStatus.PASS -> {
                        Quad(completedBg, completedBorder, completedText, "✓ $stepNum $cleanTitle")
                    }
                    isCurrent -> {
                        Quad(currentBg, currentBorder, currentText, "→ $stepNum $cleanTitle")
                    }
                    else -> {
                        Quad(pendingBg, pendingBorder, pendingText, "○ $stepNum $cleanTitle")
                    }
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(if (isCurrent) 1.5.dp else 1.dp, badgeBorder),
                    shadowElevation = if (isCurrent) 1.dp else 0.dp,
                    modifier = Modifier
                        .clickable { onStepSelected(step) }
                        .testTag("stepper_item_${step.stepNumber}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = displayText,
                            style = TextStyle(
                                fontFamily = if (isCurrent) SpaceGroteskFontFamily else JetBrainsMonoFontFamily,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 11.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = textColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
