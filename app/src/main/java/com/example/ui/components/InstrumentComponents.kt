package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Scientific Instrument Module Card adhering to the ARTHROSCAN-NER Visual Hierarchy:
 * - Rounded corners (10.dp)
 * - Layered dark surface with subtle border (SurfaceSubtleBorder)
 * - Controlled elevation
 * - Compact scientific spacing
 * - Instrument header (icon, section heading, status badge, action)
 */
@Composable
fun InstrumentModuleCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = ArthroscanBlueBright,
    headerBadge: String? = null,
    headerBadgeColor: Color = ArthroscanBlueBright,
    headerAction: @Composable (() -> Unit)? = null,
    containerColor: Color = SurfaceLevel2_Card,
    borderColor: Color = SurfaceSubtleBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Instrument Module Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = accentColor
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (headerBadge != null) {
                        Surface(
                            color = headerBadgeColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, headerBadgeColor.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = headerBadge.uppercase(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = headerBadgeColor
                            )
                        }
                    }
                    headerAction?.invoke()
                }
            }

            // Internal content
            content()
        }
    }
}

/**
 * High-precision status badge that never relies on color alone.
 * Always renders an icon or indicator dot, text label, and subtle high-contrast border.
 */
@Composable
fun TechnicalStatusBadge(
    status: String,
    statusColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPulsing: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        color = statusColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            } else if (isPulsing) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .alpha(pulseAlpha)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }

            Text(
                text = status.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 0.6.sp
                ),
                color = statusColor
            )
        }
    }
}

/**
 * Metric Readout Item for dense scientific consoles with status interpretation.
 */
@Composable
fun ScientificMetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    highlightColor: Color = TextNearWhite,
    statusInterpretation: String? = null,
    statusColor: Color? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                letterSpacing = 0.6.sp
            ),
            color = TextCoolGray
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = highlightColor
            )
            if (unit != null) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        color = TextGrayBlue
                    )
                )
            }
        }
        if (statusInterpretation != null && statusColor != null) {
            Spacer(modifier = Modifier.height(3.dp))
            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(3.dp),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
            ) {
                Text(
                    text = statusInterpretation.uppercase(),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = statusColor
                )
            }
        }
    }
}
