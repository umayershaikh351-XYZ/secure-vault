package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlitchText
import com.example.ui.components.MatrixRain
import com.example.ui.components.terminalScanlines
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DimGreen
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TerminalBlack
import com.example.ui.theme.TerminalBorder
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        delay(2400)
        onTimeout()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .terminalScanlines()
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Matrix rain digital background
        MatrixRain(modifier = Modifier.fillMaxSize(), alpha = 0.45f)

        // Center Glitch Terminal Logo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(TerminalBlack.copy(alpha = 0.85f))
                .border(1.dp, TerminalBorder, RoundedCornerShape(2.dp))
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Text(
                text = "> INITIALIZING SECURE ENVIRONMENT...",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = DimGreen,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            GlitchText(
                text = "CIPHERLOCK",
                fontSize = 32
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "MIL-SPEC OFFLINE VAULT // v1.0",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = CyanAccent,
                    letterSpacing = 1.2.sp
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "[ AES-256-GCM / KEYSTORE ARMORED ]",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = NeonGreen,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}
