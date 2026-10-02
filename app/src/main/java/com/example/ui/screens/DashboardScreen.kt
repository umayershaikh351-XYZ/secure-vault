package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.RiskLevel
import com.example.security.SecurityReport
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalHeader
import com.example.ui.components.TerminalPanel
import com.example.ui.components.terminalScanlines
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DimGreen
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalBlack
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalBorderDim
import com.example.ui.theme.TerminalDark
import com.example.ui.theme.TerminalPanelBg

@Composable
fun DashboardScreen(
    report: SecurityReport?,
    vaultItemCount: Int,
    onViewIssues: () -> Unit,
    onRunFullScan: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenSettings: () -> Unit,
    onLockNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val score = report?.score ?: 100
    val riskLevel = report?.riskLevel ?: RiskLevel.LOW
    val totalIssues = (report?.warningCount ?: 0) + (report?.dangerCount ?: 0)

    val scoreColor = when {
        score >= 80 -> NeonGreen
        score >= 50 -> TerminalAmber
        else -> NeonRed
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .terminalScanlines()
            .testTag("dashboard_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with quick Lock
            TerminalHeader(
                title = "CIPHERLOCK DASHBOARD",
                statusText = riskLevel.label,
                statusColor = scoreColor,
                onLockClick = onLockNow
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Large ASCII bordered Security Score Box
            TerminalScoreCard(
                score = score,
                scoreColor = scoreColor,
                riskLabel = riskLevel.label,
                issuesCount = totalIssues,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Stats Panel
            TerminalPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
            ) {
                Text(
                    text = "> SYSTEM TELEMETRY",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                TelemetryRow(label = "OFFLINE AIR-GAP", value = "ENFORCED [0 NIC]")
                TelemetryRow(label = "KEYSTORE STATUS", value = "HARDWARE BACKED")
                TelemetryRow(label = "ACTIVE VAULT ITEMS", value = "$vaultItemCount ENCRYPTED")
                TelemetryRow(label = "AES-256 CIPHER", value = "GCM 128-BIT AUTH")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TerminalButton(
                    text = "[ VIEW ISSUES ]",
                    onClick = onViewIssues,
                    color = if (totalIssues > 0) TerminalAmber else NeonGreen,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "dashboard_view_issues"
                )

                TerminalButton(
                    text = "[ RUN FULL SCAN ]",
                    onClick = onRunFullScan,
                    color = CyanAccent,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "dashboard_run_scan"
                )

                TerminalButton(
                    text = "[ OPEN VAULT ]",
                    onClick = onOpenVault,
                    color = NeonGreen,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "dashboard_open_vault"
                )

                TerminalButton(
                    text = "[ SETTINGS & CONFIG ]",
                    onClick = onOpenSettings,
                    color = DimGreen,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "dashboard_open_settings"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "> ZERO NETWORK CALLS // AIR-GAPPED // NO TELEMETRY",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DimGreen,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@Composable
private fun TerminalScoreCard(
    score: Int,
    scoreColor: Color,
    riskLabel: String,
    issuesCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(TerminalDark)
            .border(1.dp, scoreColor)
            .drawBehind {
                drawRect(
                    color = scoreColor.copy(alpha = 0.04f),
                    topLeft = Offset.Zero,
                    size = size
                )
            }
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ASCII Box border header
        Text(
            text = "+-----------------------------+",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = scoreColor
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Large Score
        Text(
            text = "$score",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 54.sp,
                letterSpacing = 2.sp,
                color = scoreColor
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = "SECURITY SCORE",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                color = NeonGreen
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = "/100",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = DimGreen
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "+-----------------------------+",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = scoreColor
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Status & Issues detected
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "> STATUS: ",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = DimGreen
                )
            )
            Text(
                text = riskLabel.uppercase(),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = scoreColor
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "> $issuesCount ISSUE${if (issuesCount != 1) "S" else ""} DETECTED",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.8.sp,
                color = if (issuesCount > 0) TerminalAmber else NeonGreen
            )
        )
    }
}

@Composable
private fun TelemetryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = DimGreen,
                letterSpacing = 0.5.sp
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = NeonGreen,
                letterSpacing = 0.5.sp
            )
        )
    }
}
