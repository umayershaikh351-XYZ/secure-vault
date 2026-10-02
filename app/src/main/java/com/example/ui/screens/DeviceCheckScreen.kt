package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.security.CheckResult
import com.example.security.CheckStatus
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
import kotlin.math.roundToInt

@Composable
fun DeviceCheckScreen(
    report: SecurityReport?,
    isScanning: Boolean,
    scanProgress: Float,
    onRunScan: () -> Unit,
    selectedCheck: CheckResult?,
    onSelectCheck: (CheckResult) -> Unit,
    onDismissDetail: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .terminalScanlines()
            .testTag("device_check_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TerminalHeader(
                title = "SYSTEM DIAGNOSTICS",
                statusText = if (isScanning) "SCAN IN PROGRESS" else "AUDIT COMPLETE",
                statusColor = if (isScanning) CyanAccent else NeonGreen
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Diagnostic Progress Banner
            TerminalPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Text(
                    text = "> RUNNING DEVICE DIAGNOSTICS...",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Terminal ASCII Progress Bar: ████████████████████ 100%
                val totalBlocks = 20
                val filledBlocks = if (isScanning) {
                    (scanProgress * totalBlocks).roundToInt().coerceIn(0, totalBlocks)
                } else {
                    totalBlocks
                }
                val percent = if (isScanning) (scanProgress * 100).roundToInt() else 100

                val bar = buildString {
                    repeat(filledBlocks) { append("█") }
                    repeat(totalBlocks - filledBlocks) { append("░") }
                    append(" ")
                    append(percent)
                    append("%")
                }

                Text(
                    text = "> $bar",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NeonGreen
                    )
                )

                if (!isScanning && report != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "> VULNERABILITIES FOUND: ${report.warningCount + report.dangerCount} (${report.warningCount} WARN / ${report.dangerCount} FAIL)",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = if (report.dangerCount > 0) NeonRed else if (report.warningCount > 0) TerminalAmber else NeonGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of Checks
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val results = report?.results.orEmpty()
                items(results, key = { it.id }) { item ->
                    CheckResultRow(
                        item = item,
                        onClick = { onSelectCheck(item) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TerminalButton(
                    text = "[ < RETURN ]",
                    onClick = onBack,
                    color = DimGreen,
                    modifier = Modifier.weight(1f),
                    testTag = "device_check_return"
                )

                TerminalButton(
                    text = "[ RE-SCAN ]",
                    onClick = onRunScan,
                    enabled = !isScanning,
                    color = CyanAccent,
                    modifier = Modifier.weight(1f),
                    testTag = "device_check_rescan"
                )
            }
        }

        // Detail Dialog
        if (selectedCheck != null) {
            CheckDetailDialog(
                check = selectedCheck,
                onDismiss = onDismissDetail
            )
        }
    }
}

@Composable
private fun CheckResultRow(
    item: CheckResult,
    onClick: () -> Unit
) {
    val statusColor = when (item.status) {
        CheckStatus.SAFE -> NeonGreen
        CheckStatus.WARNING -> TerminalAmber
        CheckStatus.DANGER -> NeonRed
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalDark, shape = RoundedCornerShape(2.dp))
            .border(1.dp, TerminalBorderDim, shape = RoundedCornerShape(2.dp))
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tag [OK] / [WARN] / [FAIL]
        Text(
            text = item.status.tag,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = statusColor
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Title
        Text(
            text = item.title,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = NeonGreen,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.weight(1f)
        )

        // Summary
        Text(
            text = item.summary,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = statusColor
            )
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = ">",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = DimGreen
            )
        )
    }
}

@Composable
fun CheckDetailDialog(
    check: CheckResult,
    onDismiss: () -> Unit
) {
    val statusColor = when (check.status) {
        CheckStatus.SAFE -> NeonGreen
        CheckStatus.WARNING -> TerminalAmber
        CheckStatus.DANGER -> NeonRed
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalBlack)
                .border(1.dp, statusColor, RoundedCornerShape(2.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${check.status.tag} ${check.title}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = statusColor,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = check.summary,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = statusColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "> DIAGNOSTIC ANALYSIS:",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = CyanAccent,
                    letterSpacing = 0.8.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = check.description,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = NeonGreen
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "> STEP-BY-STEP REMEDIATION:",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = CyanAccent,
                    letterSpacing = 0.8.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            check.fixInstructions.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "[${index + 1}] ",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = DimGreen
                        )
                    )
                    Text(
                        text = step,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = NeonGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TerminalButton(
                text = "[ ACKNOWLEDGE ]",
                onClick = onDismiss,
                color = statusColor,
                modifier = Modifier.fillMaxWidth(),
                testTag = "check_detail_dismiss"
            )
        }
    }
}
