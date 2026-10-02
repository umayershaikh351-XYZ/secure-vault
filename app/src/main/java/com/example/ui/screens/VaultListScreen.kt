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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
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
import com.example.data.VaultItem
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalHeader
import com.example.ui.components.TerminalInputField
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
fun VaultListScreen(
    items: List<VaultItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectItem: (Long) -> Unit,
    onAddNewEntry: () -> Unit,
    onDeleteItem: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var itemToDelete by remember { mutableStateOf<VaultItem?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .terminalScanlines()
            .testTag("vault_list_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TerminalHeader(
                title = "ENCRYPTED VAULT",
                statusText = "KEYSTORE ARMORED",
                statusColor = NeonGreen
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search input
            TerminalInputField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = "SEARCH VAULT",
                placeholder = "QUERY TITLE OR TAG...",
                trailing = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NeonGreen
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                testTag = "vault_search_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Vault Header Line
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Text(
                    text = "> VAULT CONTENTS [${items.size} ITEMS]",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
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
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vault Items List
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .widthIn(max = 600.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "[ NO ENCRYPTED ENTRIES FOUND ]",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = DimGreen,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "> INITIALIZE FIRST SECRET ENTRY BELOW",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = VeryDimColor()
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .widthIn(max = 600.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
                        VaultRow(
                            index = index + 1,
                            item = item,
                            onClick = { onSelectItem(item.id) },
                            onDelete = { itemToDelete = item }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
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

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
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
                    testTag = "vault_return"
                )

                TerminalButton(
                    text = "[+] ADD NEW ENTRY",
                    onClick = onAddNewEntry,
                    color = NeonGreen,
                    modifier = Modifier.weight(1.3f),
                    testTag = "vault_add_entry"
                )
            }
        }

        // Delete Confirmation Dialog
        if (itemToDelete != null) {
            val item = itemToDelete!!
            Dialog(onDismissRequest = { itemToDelete = null }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TerminalBlack)
                        .border(1.dp, NeonRed, RoundedCornerShape(2.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "> CONFIRM PURGE [Y/N]",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NeonRed,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ENTRY: ${item.title.uppercase()}",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = NeonGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "> This will permanently shred the AES ciphertext from SQLite storage.",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = DimGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TerminalButton(
                            text = "[ CANCEL ]",
                            onClick = { itemToDelete = null },
                            color = DimGreen,
                            modifier = Modifier.weight(1f),
                            testTag = "cancel_delete_button"
                        )

                        TerminalButton(
                            text = "[ PURGE ]",
                            onClick = {
                                val id = item.id
                                itemToDelete = null
                                onDeleteItem(id)
                            },
                            color = NeonRed,
                            modifier = Modifier.weight(1f),
                            testTag = "confirm_delete_button"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultRow(
    index: Int,
    item: VaultItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalDark, RoundedCornerShape(2.dp))
            .border(1.dp, TerminalBorderDim, RoundedCornerShape(2.dp))
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "[$index]",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = CyanAccent
            )
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NeonGreen,
                    letterSpacing = 0.5.sp
                ),
                maxLines = 1
            )
            Text(
                text = "[${item.category}] // AES-256 ARMORED",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DimGreen
                )
            )
        }

        Text(
            text = "> ACCESS",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = CyanAccent
            )
        )

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
            onClick = onDelete,
            modifier = Modifier.padding(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete entry",
                tint = NeonRed.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun VeryDimColor() = DimGreen.copy(alpha = 0.5f)
