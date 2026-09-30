package com.example.ui.portal.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portal.ResearchQuestionnaireResponse
import com.example.ui.theme.*

/**
 * PRODUCTION STEP 07: RESEARCH CONTEXT INPUT
 * ARTHROSCAN-NER | Clinical Field Diagnostic Suite (Google Stitch Spec)
 */
@Composable
fun Step07QuestionnaireScreen(
    currentResponse: ResearchQuestionnaireResponse,
    onSaveResponse: (ResearchQuestionnaireResponse) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var pain by remember { mutableStateOf(currentResponse.painContext) }
    var funcDiff by remember { mutableStateOf(currentResponse.functionalDifficulty) }
    var occExp by remember { mutableStateOf(currentResponse.occupationalExposure) }
    var bioImpact by remember { mutableStateOf(currentResponse.biomechanicalImpact) }

    // Clinical light mode design palette
    val surfaceCard = Color.White
    val surfaceCardLow = Color(0xFFF1F5F9)
    val borderStrokeColor = Color(0xFFE2E8F0)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)
    val primaryBlue = Color(0xFF2563EB)
    val amberWarn = Color(0xFFD97706)
    val purpleAccent = Color(0xFF7C3AED)
    val purpleBg = Color(0xFFF5F3FF)
    val purpleBorder = Color(0xFFDDD6FE)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // STEP 07 OVERVIEW CARD
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
                            text = "STEP 07 OF 11: RESEARCH CONTEXT",
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
                            text = "Epidemiological Context Priors",
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
                            text = "NON-DIAGNOSTIC",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = purpleAccent
                            )
                        )
                    }
                }

                Text(
                    text = "Generic occupational, ergonomic, and symptom context factors used solely for prior likelihood estimation in multimodal AI.",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = textSecondary
                    )
                )
            }
        }

        // QUESTIONNAIRE 1: PAIN CONTEXT
        SurveyQuestionCardLight(
            question = "PAIN / SYMPTOM CONTEXT (LAST 30 DAYS)",
            currentValue = pain,
            options = listOf(
                "NONE" to "None / Asymptomatic",
                "MILD_INTERMITTENT" to "Mild / Occasional after walking",
                "MODERATE_ACTIVITY" to "Moderate during squats/climbing",
                "PERSISTENT" to "Constant ache even at rest"
            ),
            onSelect = { pain = it }
        )

        // QUESTIONNAIRE 2: FUNCTIONAL DIFFICULTY
        SurveyQuestionCardLight(
            question = "FUNCTIONAL JOINT MOBILITY",
            currentValue = funcDiff,
            options = listOf(
                "NONE" to "Full unimpeded mobility",
                "STAIRS_AND_SQUATTING" to "Stiffness with stairs/squatting",
                "WALKING_LIMIT" to "Walking limited under 1 km",
                "PROLONGED_STANDING" to "Discomfort with standing >30m"
            ),
            onSelect = { funcDiff = it }
        )

        // QUESTIONNAIRE 3: OCCUPATIONAL EXPOSURE
        SurveyQuestionCardLight(
            question = "OCCUPATIONAL / PHYSICAL EXPOSURE",
            currentValue = occExp,
            options = listOf(
                "SEDENTARY" to "Predominantly sedentary / desk",
                "MODERATE_WALKING" to "Moderate daily walking",
                "MANUAL_FIELD_LABOR" to "Manual field / agricultural labor",
                "HEAVY_LIFTING" to "Repetitive heavy carrying / load"
            ),
            onSelect = { occExp = it }
        )

        // QUESTIONNAIRE 4: BIOMECHANICAL IMPACT
        SurveyQuestionCardLight(
            question = "BIOMECHANICAL INJURY HISTORY",
            currentValue = bioImpact,
            options = listOf(
                "NONE" to "No prior known injury",
                "PREVIOUS_MINOR_TWIST" to "Past minor knee sprain / twist",
                "REPETITIVE_IMPACT" to "Repetitive knee impact exposure",
                "JOINT_STIFFNESS_MORNING" to "Morning stiffness >15 minutes"
            ),
            onSelect = { bioImpact = it }
        )

        // REGULATORY NOTICE
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFFFBEB),
            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "RESEARCH CONTEXT INPUT — NOT A MEDICAL DIAGNOSIS.\nQuestionnaire responses are treated strictly as epidemiological context priors, never as clinical diagnostic evidence.",
                style = TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    lineHeight = 13.sp,
                    color = Color(0xFF92400E)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )
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
                        onSaveResponse(
                            ResearchQuestionnaireResponse(
                                painContext = pain,
                                functionalDifficulty = funcDiff,
                                occupationalExposure = occExp,
                                biomechanicalImpact = bioImpact
                            )
                        )
                        onNext()
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                        .testTag("questionnaire_step_next_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBlue,
                        disabledContainerColor = Color(0xFFCBD5E1)
                    )
                ) {
                    Text(
                        text = "PROCEED TO 08 QUALITY",
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
private fun SurveyQuestionCardLight(
    question: String,
    currentValue: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = question,
                style = TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFF0F172A)
                )
            )

            options.forEach { (key, label) ->
                val selected = currentValue == key
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                    border = BorderStroke(
                        1.dp,
                        if (selected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(key) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = { onSelect(key) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF2563EB),
                                unselectedColor = Color(0xFF94A3B8)
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.5.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) Color(0xFF1E40AF) else Color(0xFF334155)
                            )
                        )
                    }
                }
            }
        }
    }
}
