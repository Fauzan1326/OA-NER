package com.example.ui.portal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.auth.UserAccount
import com.example.core.contract.RiskTier
import com.example.portal.AshaScreeningSession
import com.example.portal.AshaSessionManager
import com.example.ui.theme.*

enum class HistoryFilter(val label: String) {
    ALL("ALL"),
    COMPLETED("COMPLETED"),
    IN_PROGRESS("IN PROGRESS"),
    CANCELLED("CANCELLED"),
    RETEST_REQUIRED("RETEST REQUIRED"),
    HIGHER_RISK("HIGHER RISK"),
    MODERATE("MODERATE"),
    LOWER("LOWER")
}

/**
 * PRODUCTION WORKER SCREENING HISTORY SCREEN
 * ARTHROSCAN-NER | Authoritative Field Audit Trail & Screening Registry
 *
 * Requirements (Section 16):
 * - Search by Session ID and Participant ID
 * - Filters: ALL, COMPLETED, IN PROGRESS, CANCELLED, RETEST REQUIRED, HIGHER RISK, MODERATE, LOWER
 * - Each card: Session ID, Participant ID, Date / Time, Mode, Screening Tier, Status, [ VIEW SUMMARY ]
 * - [ VIEW SUMMARY ] opens corresponding existing Step 11 summary
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AshaScreeningHistoryScreen(
    worker: UserAccount,
    sessionManager: AshaSessionManager,
    onBack: () -> Unit,
    onViewSessionSummary: (AshaScreeningSession) -> Unit
) {
    // Worker isolation security: only return authorized sessions
    val workerSessions = remember(worker.id, sessionManager) {
        sessionManager.getHistoryForWorker(worker.id)
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }

    // Clinical design palette tokens
    val bgCanvas = Color(0xFFF8FAFC)
    val surfaceCard = Color.White
    val surfaceCardLow = Color(0xFFF1F5F9)
    val borderStrokeColor = Color(0xFFE2E8F0)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)
    val primaryBlue = Color(0xFF2563EB)
    val secondaryTeal = Color(0xFF006A61)
    val readyGreen = Color(0xFF059669)
    val readyGreenBg = Color(0xFFECFDF5)
    val warningAmber = Color(0xFFD97706)
    val warningAmberBg = Color(0xFFFFFBEB)
    val errorRed = Color(0xFFDC2626)
    val errorRedBg = Color(0xFFFEF2F2)

    val filteredSessions = workerSessions.filter { session ->
        val query = searchQuery.trim().lowercase()
        val matchesQuery = query.isBlank() ||
                session.sessionId.lowercase().contains(query) ||
                session.participantId.lowercase().contains(query)

        val matchesFilter = when (selectedFilter) {
            HistoryFilter.ALL -> true
            HistoryFilter.COMPLETED -> session.isCompleted && !session.isCancelled
            HistoryFilter.IN_PROGRESS -> !session.isCompleted && !session.isCancelled
            HistoryFilter.CANCELLED -> session.isCancelled
            HistoryFilter.RETEST_REQUIRED -> session.retestRequired
            HistoryFilter.HIGHER_RISK -> session.decision?.screeningRiskTier == RiskTier.HIGHER_SCREENING_RISK ||
                    session.decision?.screeningRiskTier == RiskTier.HIGH_UNCERTAINTY_RETEST
            HistoryFilter.MODERATE -> session.decision?.screeningRiskTier == RiskTier.MODERATE_SCREENING_RISK
            HistoryFilter.LOWER -> session.decision?.screeningRiskTier == RiskTier.LOWER_SCREENING_RISK
        }

        matchesQuery && matchesFilter
    }

    Scaffold(
        topBar = {
            Surface(
                color = surfaceCard,
                border = BorderStroke(1.dp, borderStrokeColor),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("history_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "WORKER SCREENING HISTORY",
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = textPrimary
                                )
                            )
                            Text(
                                text = "Operator: ${worker.fullName} • ${worker.assignedCenter}",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.5.sp,
                                    color = textSecondary
                                )
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFDBEAFE)
                    ) {
                        Text(
                            text = "${filteredSessions.size} Logs",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = primaryBlue
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        },
        containerColor = bgCanvas
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search by Session ID or Participant ID...",
                            style = TextStyle(fontFamily = SpaceGroteskFontFamily, fontSize = 12.5.sp, color = textSecondary)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = textSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = surfaceCard,
                        unfocusedContainerColor = surfaceCard,
                        focusedBorderColor = primaryBlue,
                        unfocusedBorderColor = borderStrokeColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input")
                )
            }

            // Horizontally scrollable Filter Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HistoryFilter.values().forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) primaryBlue else surfaceCard,
                            border = BorderStroke(1.dp, if (isSelected) primaryBlue else borderStrokeColor),
                            modifier = Modifier.clickable { selectedFilter = filter }
                        ) {
                            Text(
                                text = filter.label,
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White else textSecondary
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            if (filteredSessions.isEmpty()) {
                item {
                    Surface(
                        color = surfaceCard,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, borderStrokeColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "NO SCREENING RECORDS FOUND",
                                style = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = textPrimary
                                )
                            )
                            Text(
                                text = "No screening sessions match '${selectedFilter.label}' query for this worker.",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            )
                        }
                    }
                }
            } else {
                items(filteredSessions) { session ->
                    SessionHistoryProductionCard(
                        session = session,
                        onViewSummary = { onViewSessionSummary(session) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }
        }
    }
}

@Composable
private fun SessionHistoryProductionCard(
    session: AshaScreeningSession,
    onViewSummary: () -> Unit
) {
    val tier = session.decision?.screeningRiskTier ?: RiskTier.LOWER_SCREENING_RISK
    val (tierBg, tierBorder, tierColor) = when (tier) {
        RiskTier.LOWER_SCREENING_RISK -> Triple(Color(0xFFECFDF5), Color(0xFFA7F3D0), Color(0xFF059669))
        RiskTier.MODERATE_SCREENING_RISK -> Triple(Color(0xFFFFFBEB), Color(0xFFFDE68A), Color(0xFFD97706))
        RiskTier.HIGHER_SCREENING_RISK, RiskTier.HIGH_UNCERTAINTY_RETEST -> Triple(Color(0xFFFEF2F2), Color(0xFFFECACA), Color(0xFFDC2626))
    }

    val statusText = when {
        session.isCancelled -> "CANCELLED"
        session.retestRequired -> "RETEST REQUIRED"
        session.isCompleted -> "COMPLETED"
        else -> "IN PROGRESS"
    }

    val (statusBg, statusColor) = when {
        session.isCancelled -> Pair(Color(0xFFF1F5F9), Color(0xFF64748B))
        session.retestRequired -> Pair(Color(0xFFFEF2F2), Color(0xFFDC2626))
        session.isCompleted -> Pair(Color(0xFFECFDF5), Color(0xFF059669))
        else -> Pair(Color(0xFFEFF6FF), Color(0xFF2563EB))
    }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Session ID & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = session.sessionId,
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF2563EB)
                        )
                    )
                    Text(text = "•", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text(
                        text = session.participantId,
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF0F172A)
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusText,
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            color = statusColor
                        ),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            // Sub-details: Date & Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.formattedDate,
                    style = TextStyle(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B)
                    )
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = tierBg,
                    border = BorderStroke(0.8.dp, tierBorder)
                ) {
                    Text(
                        text = tier.label.uppercase(),
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = tierColor
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.8.dp)

            // Action row: View Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewSummary,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("view_history_summary_${session.sessionId}"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF8FAFC)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "VIEW SUMMARY",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = Color(0xFF0F172A)
                        )
                    )
                }
            }
        }
    }
}
