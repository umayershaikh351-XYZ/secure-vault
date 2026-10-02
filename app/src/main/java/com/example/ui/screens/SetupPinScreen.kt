package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalHeader
import com.example.ui.components.TerminalPanel
import com.example.ui.components.terminalScanlines
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DimGreen
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TerminalBlack
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalBorderDim
import com.example.ui.theme.TerminalDark

@Composable
fun SetupPinScreen(
    onPinCreated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var primaryPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun handleKeyPress(key: String) {
        errorMessage = null
        if (!isConfirming) {
            if (primaryPin.length < 4) {
                primaryPin += key
                if (primaryPin.length == 4) {
                    isConfirming = true
                }
            }
        } else {
            if (confirmPin.length < 4) {
                confirmPin += key
            }
        }
    }

    fun handleDelete() {
        errorMessage = null
        if (isConfirming) {
            if (confirmPin.isNotEmpty()) {
                confirmPin = confirmPin.dropLast(1)
            } else {
                isConfirming = false
            }
        } else {
            if (primaryPin.isNotEmpty()) {
                primaryPin = primaryPin.dropLast(1)
            }
        }
    }

    fun submitPin() {
        if (primaryPin.length != 4) {
            errorMessage = "> ACCESS CODE MUST BE 4 DIGITS"
            return
        }
        if (confirmPin.length != 4) {
            errorMessage = "> CONFIRMATION INCOMPLETE"
            return
        }
        if (primaryPin != confirmPin) {
            errorMessage = "> ACCESS CODES DO NOT MATCH"
            confirmPin = ""
            return
        }
        onPinCreated(primaryPin)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .terminalScanlines()
            .testTag("setup_pin_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TerminalHeader(
                title = "MASTER KEY GEN",
                statusText = "SETUP ACCESS CODE",
                statusColor = CyanAccent
            )

            Spacer(modifier = Modifier.height(10.dp))

            TerminalPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
            ) {
                Text(
                    text = "> CREATE ACCESS CODE",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = if (!isConfirming) CyanAccent else DimGreen
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                PinDigitRow(pin = primaryPin, isActive = !isConfirming)

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "> CONFIRM ACCESS CODE",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = if (isConfirming) CyanAccent else DimGreen
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                PinDigitRow(pin = confirmPin, isActive = isConfirming)

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NeonRed,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Numeric keypad
            TerminalNumericKeypad(
                onKeyPress = { handleKeyPress(it) },
                onDelete = { handleDelete() },
                onClear = {
                    primaryPin = ""
                    confirmPin = ""
                    isConfirming = false
                    errorMessage = null
                },
                modifier = Modifier.widthIn(max = 380.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            TerminalButton(
                text = "[ LOCK IN ]",
                onClick = { submitPin() },
                enabled = primaryPin.length == 4 && confirmPin.length == 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                testTag = "lock_in_button"
            )
        }
    }
}

@Composable
fun PinDigitRow(
    pin: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 0 until 4) {
            val char = pin.getOrNull(i)
            val isCurrentSlot = isActive && i == pin.length

            val borderColor = when {
                char != null -> NeonGreen
                isCurrentSlot -> CyanAccent
                else -> TerminalBorderDim
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(width = 54.dp, height = 58.dp)
                    .background(TerminalDark, shape = RoundedCornerShape(2.dp))
                    .border(1.dp, borderColor, shape = RoundedCornerShape(2.dp))
                    .drawBehind {
                        if (char != null || isCurrentSlot) {
                            drawRect(
                                color = borderColor.copy(alpha = 0.08f),
                                topLeft = Offset.Zero,
                                size = size
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (char != null) "*" else if (isCurrentSlot) "_" else "-",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = if (char != null) NeonGreen else if (isCurrentSlot) CyanAccent else DimGreen
                    )
                )
            }
        }
    }
}

@Composable
fun TerminalNumericKeypad(
    onKeyPress: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("CLR", "0", "DEL")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in keys) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (key in row) {
                    val isAction = key == "CLR" || key == "DEL"
                    val keyColor = if (isAction) CyanAccent else NeonGreen

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .background(TerminalDark, RoundedCornerShape(2.dp))
                            .border(1.dp, TerminalBorderDim, RoundedCornerShape(2.dp))
                            .clickable(
                                role = Role.Button,
                                onClick = {
                                    when (key) {
                                        "DEL" -> onDelete()
                                        "CLR" -> onClear()
                                        else -> onKeyPress(key)
                                    }
                                }
                            )
                            .padding(vertical = 12.dp)
                            .testTag("key_$key"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp,
                                color = keyColor
                            )
                        )
                    }
                }
            }
        }
    }
}
