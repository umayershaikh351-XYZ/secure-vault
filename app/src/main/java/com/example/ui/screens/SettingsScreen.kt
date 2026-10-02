package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalHeader
import com.example.ui.components.TerminalInputField
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

@Composable
fun SettingsScreen(
    autoLockSeconds: Int,
    onCycleAutoLock: () -> Unit,
    onChangePin: (oldPin: String, newPin: String) -> Boolean,
    onClearAllData: (pinConfirm: String) -> Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var showChangePinDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val autoLockLabel = when (autoLockSeconds) {
        15 -> "15s"
        30 -> "30s"
        60 -> "1min"
        300 -> "5min"
        else -> "${autoLockSeconds}s"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .terminalScanlines()
            .testTag("settings_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TerminalHeader(
                title = "SYSTEM CONFIGURATION",
                statusText = "CONTROL NODE",
                statusColor = CyanAccent
            )

            Spacer(modifier = Modifier.height(18.dp))

            TerminalPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Text(
                    text = "> SETTINGS",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = CyanAccent
                    )
                )

                Text(
                    text = "--------------------------------------------------",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TerminalBorderDim
                    ),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Change PIN
                    TerminalButton(
                        text = "[ CHANGE PIN ]",
                        onClick = { showChangePinDialog = true },
                        color = NeonGreen,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "settings_change_pin"
                    )

                    // Auto-lock timer
                    TerminalButton(
                        text = "[ AUTO-LOCK TIMER: $autoLockLabel ]",
                        onClick = onCycleAutoLock,
                        color = CyanAccent,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "settings_auto_lock_timer"
                    )

                    // Clear all data
                    TerminalButton(
                        text = "[ CLEAR ALL VAULT DATA ]",
                        onClick = { showClearDataDialog = true },
                        color = NeonRed,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "settings_clear_vault"
                    )

                    // About
                    TerminalButton(
                        text = "[ ABOUT CIPHERLOCK ]",
                        onClick = { showAboutDialog = true },
                        color = DimGreen,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "settings_about"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "--------------------------------------------------",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TerminalBorderDim
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            TerminalButton(
                text = "[ < RETURN ]",
                onClick = onBack,
                color = DimGreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                testTag = "settings_return"
            )
        }

        // Change PIN Dialog
        if (showChangePinDialog) {
            ChangePinDialog(
                onSubmit = { old, new ->
                    val success = onChangePin(old, new)
                    if (success) showChangePinDialog = false
                    success
                },
                onDismiss = { showChangePinDialog = false }
            )
        }

        // Clear Data Dialog
        if (showClearDataDialog) {
            ClearDataDialog(
                onConfirm = { pin ->
                    val success = onClearAllData(pin)
                    if (success) showClearDataDialog = false
                    success
                },
                onDismiss = { showClearDataDialog = false }
            )
        }

        // About Dialog
        if (showAboutDialog) {
            AboutDialog(onDismiss = { showAboutDialog = false })
        }
    }
}

@Composable
private fun ChangePinDialog(
    onSubmit: (oldPin: String, newPin: String) -> Boolean,
    onDismiss: () -> Unit
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmNewPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalBlack)
                .border(1.dp, TerminalBorder, RoundedCornerShape(2.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "> CHANGE ACCESS CODE",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CyanAccent,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            TerminalInputField(
                value = oldPin,
                onValueChange = { if (it.length <= 4) oldPin = it },
                label = "CURRENT PIN",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                testTag = "input_old_pin"
            )

            Spacer(modifier = Modifier.height(10.dp))

            TerminalInputField(
                value = newPin,
                onValueChange = { if (it.length <= 4) newPin = it },
                label = "NEW PIN (4 DIGITS)",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                testTag = "input_new_pin"
            )

            Spacer(modifier = Modifier.height(10.dp))

            TerminalInputField(
                value = confirmNewPin,
                onValueChange = { if (it.length <= 4) confirmNewPin = it },
                label = "CONFIRM NEW PIN",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                testTag = "input_confirm_new_pin"
            )

            if (errorText != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorText ?: "",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = NeonRed
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TerminalButton(
                    text = "[ CANCEL ]",
                    onClick = onDismiss,
                    color = DimGreen,
                    modifier = Modifier.weight(1f)
                )

                TerminalButton(
                    text = "[ UPDATE ]",
                    onClick = {
                        if (oldPin.length != 4 || newPin.length != 4 || confirmNewPin.length != 4) {
                            errorText = "> ALL PINS MUST BE 4 DIGITS"
                            return@TerminalButton
                        }
                        if (newPin != confirmNewPin) {
                            errorText = "> NEW PINS DO NOT MATCH"
                            return@TerminalButton
                        }
                        val success = onSubmit(oldPin, newPin)
                        if (!success) {
                            errorText = "> CURRENT PIN INCORRECT"
                        }
                    },
                    color = NeonGreen,
                    modifier = Modifier.weight(1f),
                    testTag = "confirm_change_pin_button"
                )
            }
        }
    }
}

@Composable
private fun ClearDataDialog(
    onConfirm: (pin: String) -> Boolean,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalBlack)
                .border(1.dp, NeonRed, RoundedCornerShape(2.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "> PURGE ALL VAULT DATA [CRITICAL]",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeonRed,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "> This action shreds all AES-256 records, deletes the hardware Keystore key, and wipes system preferences permanently. It CANNOT be undone.",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = DimGreen
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            TerminalInputField(
                value = pin,
                onValueChange = { if (it.length <= 4) pin = it },
                label = "CONFIRM WITH ACCESS CODE",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                testTag = "purge_pin_input"
            )

            if (errorText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorText ?: "",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = NeonRed
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TerminalButton(
                    text = "[ CANCEL ]",
                    onClick = onDismiss,
                    color = DimGreen,
                    modifier = Modifier.weight(1f)
                )

                TerminalButton(
                    text = "[ EXECUTE SHRED ]",
                    onClick = {
                        if (pin.length != 4) {
                            errorText = "> ENTER 4-DIGIT PIN"
                            return@TerminalButton
                        }
                        val success = onConfirm(pin)
                        if (!success) {
                            errorText = "> PIN INCORRECT"
                        }
                    },
                    color = NeonRed,
                    modifier = Modifier.weight(1.3f),
                    testTag = "confirm_purge_button"
                )
            }
        }
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalBlack)
                .border(1.dp, CyanAccent, RoundedCornerShape(2.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "> CIPHERLOCK v1.0",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = CyanAccent,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "STATUS: SECURE // AIR-GAPPED",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = NeonGreen
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "NO DATA LEAVES THIS DEVICE.",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NeonGreen,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            val details = listOf(
                "• Cryptography: AES-256-GCM hardware-backed",
                "• Key Storage: Android KeyStore TEE/StrongBox",
                "• Storage: Local Room SQLite (Encrypted fields)",
                "• Network: ZERO INTERNET PERMISSION IN MANIFEST",
                "• Telemetry: ZERO analytics, trackers, or SDKs",
                "• Auto-purge: Memory buffer cleared after 30s"
            )

            details.forEach { detail ->
                Text(
                    text = detail,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 17.sp,
                        color = DimGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            TerminalButton(
                text = "[ CLOSE ]",
                onClick = onDismiss,
                color = CyanAccent,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
