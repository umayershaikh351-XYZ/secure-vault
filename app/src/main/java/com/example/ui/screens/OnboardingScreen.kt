package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalHeader
import com.example.ui.components.TerminalPanel
import com.example.ui.components.TypewriterText
import com.example.ui.components.terminalScanlines
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DimGreen
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TerminalBlack

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSlide by remember { mutableIntStateOf(0) }

    val slides = listOf(
        "> THIS APP IS 100% OFFLINE. YOUR DATA NEVER LEAVES YOUR PHONE.",
        "> AES-256 ENCRYPTION. HARDWARE-BACKED KEYS. NO CLOUD.",
        "> MONITORS YOUR DEVICE SECURITY AND GIVES YOU A SCORE."
    )

    val slideSubtitles = listOf(
        "ZERO NETWORK PERMISSION DECLARED // ZERO ANALYTICS // ZERO SERVER",
        "KEYS GENERATED INSIDE ANDROID HARDWARE KEYSTORE TEE/SE MODULE",
        "REAL-TIME VULNERABILITY AUDIT, ROOT CHECKS & DIAGNOSTICS"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .terminalScanlines()
            .testTag("onboarding_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TerminalHeader(
                title = "SYSTEM BOOTSTRAP",
                statusText = "FIRST LAUNCH PROTOCOL",
                statusColor = CyanAccent
            )

            Spacer(modifier = Modifier.height(16.dp))

            TerminalPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .weight(1f, fill = false)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "NODE PHASE [0${currentSlide + 1}/03]",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CyanAccent,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "PROTOCOL://SECURE",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = DimGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Terminal typing text animation
                TypewriterText(
                    text = slides[currentSlide],
                    speedMs = 28L,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 26.sp,
                        letterSpacing = 1.sp,
                        color = NeonGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = slideSubtitles[currentSlide],
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.8.sp,
                        color = DimGreen
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ASCII Terminal Progress Indicator
                val progressAscii = buildString {
                    append("[ ")
                    for (i in 0..2) {
                        if (i == currentSlide) append("■ ") else append("□ ")
                    }
                    append("]")
                }
                Text(
                    text = progressAscii,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = CyanAccent
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            TerminalButton(
                text = if (currentSlide < 2) "[ NEXT ]" else "[ INITIALIZE ]",
                onClick = {
                    if (currentSlide < 2) {
                        currentSlide++
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                testTag = if (currentSlide < 2) "onboarding_next" else "onboarding_initialize"
            )
        }
    }
}
