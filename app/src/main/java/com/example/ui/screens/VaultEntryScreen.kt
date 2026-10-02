package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DecryptedVaultItem
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

@Composable
fun VaultEntryScreen(
    editingItem: DecryptedVaultItem?,
    clipboardCountdown: Int?,
    onSave: (id: Long, title: String, user: String, pass: String, notes: String, category: String) -> Unit,
    onDelete: (Long) -> Unit,
    onCancel: () -> Unit,
    onGeneratePassword: () -> String,
    onCopyPassword: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onCancel() }

    var title by remember(editingItem) { mutableStateOf(editingItem?.title ?: "") }
    var username by remember(editingItem) { mutableStateOf(editingItem?.username ?: "") }
    var password by remember(editingItem) { mutableStateOf(editingItem?.password ?: "") }
    var notes by remember(editingItem) { mutableStateOf(editingItem?.notes ?: "") }
    var category by remember(editingItem) { mutableStateOf(editingItem?.category ?: "LOGIN") }
    var showPassword by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val isEditMode = editingItem != null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .terminalScanlines()
            .testTag("vault_entry_screen"),
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
                title = if (isEditMode) "VAULT://RECORD_EDIT" else "VAULT://RECORD_CREATE",
                statusText = "CIPHER: AES-256-GCM",
                statusColor = CyanAccent
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Clipboard Countdown Notice
            if (clipboardCountdown != null) {
                TerminalPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp),
                    borderColor = TerminalAmber
                ) {
                    Text(
                        text = "> CLIPBOARD BUFFER: PURGE IN ${clipboardCountdown}s",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TerminalAmber,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "> Clipboard memory will be overwritten automatically.",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = DimGreen
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Entry Form
            TerminalPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Text(
                    text = if (isEditMode) "> EDIT ENTRY [ID: ${editingItem?.id}]" else "> NEW ENTRY",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        color = CyanAccent
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                TerminalInputField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorText = null
                    },
                    label = "TITLE",
                    placeholder = "e.g., Primary Work Email",
                    testTag = "entry_title_input"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category selector
                Text(
                    text = "> CATEGORY",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = DimGreen
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val categories = listOf("LOGIN", "FINANCE", "NETWORK", "NOTE")
                    categories.forEach { cat ->
                        val isSelected = category == cat
                        TerminalButton(
                            text = cat,
                            onClick = { category = cat },
                            color = if (isSelected) CyanAccent else DimGreen,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            testTag = "category_$cat"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Username
                TerminalInputField(
                    value = username,
                    onValueChange = { username = it },
                    label = "USERNAME",
                    placeholder = "user@domain.com",
                    testTag = "entry_username_input"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password with Show/Hide, Generate, Copy
                TerminalInputField(
                    value = password,
                    onValueChange = { password = it },
                    label = "PASSWORD",
                    placeholder = "••••••••••••",
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailing = {
                        IconButton(
                            onClick = { showPassword = !showPassword },
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Hide password" else "Show password",
                                tint = NeonGreen
                            )
                        }
                    },
                    testTag = "entry_password_input"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Password utilities: [ GENERATE ] [ COPY ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TerminalButton(
                        text = "[ GENERATE ]",
                        onClick = {
                            val gen = onGeneratePassword()
                            password = gen
                            showPassword = true
                        },
                        color = CyanAccent,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        testTag = "generate_password_button"
                    )

                    TerminalButton(
                        text = "[ COPY ]",
                        onClick = {
                            if (password.isNotEmpty()) {
                                onCopyPassword(password)
                            }
                        },
                        enabled = password.isNotEmpty(),
                        color = NeonGreen,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        testTag = "copy_password_button"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notes
                TerminalInputField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "NOTES",
                    placeholder = "Encrypted private memo...",
                    singleLine = false,
                    modifier = Modifier.height(96.dp),
                    testTag = "entry_notes_input"
                )

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorText ?: "",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NeonRed
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action row: [ SAVE ENCRYPTED ] [ CANCEL ]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TerminalButton(
                    text = "[ SAVE ENCRYPTED ]",
                    onClick = {
                        if (title.isBlank()) {
                            errorText = "> TITLE CANNOT BE EMPTY"
                            return@TerminalButton
                        }
                        onSave(
                            editingItem?.id ?: 0L,
                            title.trim(),
                            username.trim(),
                            password,
                            notes,
                            category
                        )
                    },
                    color = NeonGreen,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "save_entry_button"
                )

                TerminalButton(
                    text = "[ CANCEL ]",
                    onClick = onCancel,
                    color = DimGreen,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "cancel_entry_button"
                )

                if (isEditMode) {
                    TerminalButton(
                        text = "[ PURGE RECORD ]",
                        onClick = { editingItem?.id?.let(onDelete) },
                        color = NeonRed,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "delete_entry_button"
                    )
                }
            }
        }
    }
}
