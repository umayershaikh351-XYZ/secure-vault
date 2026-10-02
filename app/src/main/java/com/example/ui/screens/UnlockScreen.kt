package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun UnlockScreen(
    onUnlockAttempt: (String) -> Unit,
    isLockedOut: Boolean,
    lockoutSecondsRemaining: Int,
    wrongPinAttempts: Int,
    pinErrorFlash: Boolean,
    accessDeniedMessage: String?,
    isBiometricAvailable: Boolean,
    onBiometricClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pinInput by remember { mutableStateOf("") }
    val shakeOffset = remember { Animatable(0f) }

    // Trigger shake animation on wrong pin
    LaunchedEffect(pinErrorFlash) {
        if (pinErrorFlash) {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    -20f at 50
                    20f at 100
                    -15f at 150
                    15f at 200
                    -10f at 250
                    10f at 300
                    0f at 400
                }
            )
            pinInput = ""
        }
    }

    // Auto submit upon 4 digits
    LaunchedEffect(pinInput) {
        if (pinInput.length == 4 && !isLockedOut) {
            onUnlockAttempt(pinInput)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (pinErrorFlash) NeonRed.copy(alpha = 0.25f) else TerminalBlack)
            .terminalScanlines()
            .testTag("unlock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TerminalHeader(
                title = "AUTHENTICATION GATE",
                statusText = if (isLockedOut) "LOCKOUT ACTIVE" else if (wrongPinAttempts > 0) "WARNING [FAILURES: $wrongPinAttempts]" else "LOCKED",
                statusColor = if (isLockedOut) NeonRed else if (wrongPinAttempts > 0) TerminalAmber else NeonGreen
            )

            Spacer(modifier = Modifier.height(10.dp))

            TerminalPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                borderColor = if (pinErrorFlash || isLockedOut) NeonRed else TerminalBorder
            ) {
                Text(
                    text = "> ENTER ACCESS CODE:",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        color = if (isLockedOut) NeonRed else CyanAccent
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PIN Boxes showing * with green glow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 0 until 4) {
                        val isEntered = i < pinInput.length
                        val isCurrentSlot = i == pinInput.length && !isLockedOut

                        val boxBorder = when {
                            pinErrorFlash || isLockedOut -> NeonRed
                            isEntered -> NeonGreen
                            isCurrentSlot -> CyanAccent
                            else -> TerminalBorderDim
                        }

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .size(width = 54.dp, height = 60.dp)
                                .background(TerminalDark, shape = RoundedCornerShape(2.dp))
                                .border(1.dp, boxBorder, shape = RoundedCornerShape(2.dp))
                                .drawBehind {
                                    if (isEntered) {
                                        drawRect(
                                            color = (if (pinErrorFlash) NeonRed else NeonGreen).copy(alpha = 0.12f),
                                            topLeft = Offset.Zero,
                                            size = size
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isEntered) "*" else if (isCurrentSlot) "_" else "-",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp,
                                    color = if (pinErrorFlash) NeonRed else if (isEntered) NeonGreen else if (isCurrentSlot) CyanAccent else DimGreen
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status or Lockout countdown
                if (isLockedOut) {
                    Text(
                        text = "> ACCESS DENIED. TERMINAL LOCKDOWN: ${lockoutSecondsRemaining}s",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NeonRed,
                            letterSpacing = 1.sp
                        )
                    )
                } else if (accessDeniedMessage != null) {
                    Text(
                        text = accessDeniedMessage,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NeonRed,
                            letterSpacing = 1.sp
                        )
                    )
                } else {
                    Text(
                        text = "> HARDWARE KEYSTORE ENCRYPTION ENGAGED",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = DimGreen,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Numeric Keypad
            TerminalNumericKeypad(
                onKeyPress = { key ->
                    if (!isLockedOut && pinInput.length < 4) {
                        pinInput += key
                    }
                },
                onDelete = {
                    if (!isLockedOut && pinInput.isNotEmpty()) {
                        pinInput = pinInput.dropLast(1)
                    }
                },
                onClear = {
                    if (!isLockedOut) {
                        pinInput = ""
                    }
                },
                modifier = Modifier.widthIn(max = 380.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Biometric Option if device supports it
            if (isBiometricAvailable) {
                Column(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "> BIOMETRIC SCAN AVAILABLE",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = CyanAccent,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TerminalButton(
                        text = "[ SCAN FINGERPRINT ]",
                        onClick = onBiometricClick,
                        enabled = !isLockedOut,
                        color = CyanAccent,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "scan_fingerprint_button"
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
