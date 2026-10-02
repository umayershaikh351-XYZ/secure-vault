package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeviceCheckScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupPinScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.UnlockScreen
import com.example.ui.screens.VaultEntryScreen
import com.example.ui.screens.VaultListScreen
import com.example.ui.theme.TerminalBlack
import com.example.ui.viewmodel.CipherLockViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun CipherLockApp(
    viewModel: CipherLockViewModel,
    activity: FragmentActivity,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val securityReport by viewModel.securityReport.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val wrongPinAttempts by viewModel.wrongPinAttempts.collectAsState()
    val isLockedOut by viewModel.isLockedOut.collectAsState()
    val lockoutSecondsRemaining by viewModel.lockoutSecondsRemaining.collectAsState()
    val pinErrorFlash by viewModel.pinErrorFlash.collectAsState()
    val accessDeniedMessage by viewModel.accessDeniedMessage.collectAsState()
    val autoLockSeconds by viewModel.autoLockSeconds.collectAsState()
    val clipboardCountdown by viewModel.clipboardPurgeCountdown.collectAsState()
    val vaultItems by viewModel.filteredVaultItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val editingItem by viewModel.editingItem.collectAsState()
    val selectedCheck by viewModel.selectedCheck.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBlack)
            .safeDrawingPadding()
    ) {
        when (val screen = currentScreen) {
            is ScreenDestination.Splash -> {
                SplashScreen(
                    onTimeout = { viewModel.onSplashFinished() }
                )
            }

            is ScreenDestination.Onboarding -> {
                OnboardingScreen(
                    onComplete = { viewModel.completeOnboarding() }
                )
            }

            is ScreenDestination.SetupPin -> {
                SetupPinScreen(
                    onPinCreated = { pin -> viewModel.setupPin(pin) }
                )
            }

            is ScreenDestination.Unlock -> {
                UnlockScreen(
                    onUnlockAttempt = { pin -> viewModel.attemptUnlock(pin) },
                    isLockedOut = isLockedOut,
                    lockoutSecondsRemaining = lockoutSecondsRemaining,
                    wrongPinAttempts = wrongPinAttempts,
                    pinErrorFlash = pinErrorFlash,
                    accessDeniedMessage = accessDeniedMessage,
                    isBiometricAvailable = viewModel.isBiometricAvailable(),
                    onBiometricClick = { viewModel.authenticateWithBiometrics(activity) }
                )
            }

            is ScreenDestination.Dashboard -> {
                DashboardScreen(
                    report = securityReport,
                    vaultItemCount = vaultItems.size,
                    onViewIssues = {
                        viewModel.runDeviceScan()
                        viewModel.navigateTo(ScreenDestination.DeviceCheck)
                    },
                    onRunFullScan = {
                        viewModel.runDeviceScan()
                        viewModel.navigateTo(ScreenDestination.DeviceCheck)
                    },
                    onOpenVault = { viewModel.navigateTo(ScreenDestination.VaultList) },
                    onOpenSettings = { viewModel.navigateTo(ScreenDestination.Settings) },
                    onLockNow = { viewModel.lockNow() }
                )
            }

            is ScreenDestination.DeviceCheck -> {
                DeviceCheckScreen(
                    report = securityReport,
                    isScanning = isScanning,
                    scanProgress = scanProgress,
                    onRunScan = { viewModel.runDeviceScan() },
                    selectedCheck = selectedCheck,
                    onSelectCheck = { viewModel.openCheckDetail(it) },
                    onDismissDetail = { viewModel.closeCheckDetail() },
                    onBack = { viewModel.navigateTo(ScreenDestination.Dashboard) }
                )
            }

            is ScreenDestination.VaultList -> {
                VaultListScreen(
                    items = vaultItems,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onSelectItem = { id -> viewModel.navigateTo(ScreenDestination.VaultEntry(id)) },
                    onAddNewEntry = { viewModel.navigateTo(ScreenDestination.VaultEntry(null)) },
                    onDeleteItem = { id -> viewModel.deleteVaultEntry(id) },
                    onBack = { viewModel.navigateTo(ScreenDestination.Dashboard) }
                )
            }

            is ScreenDestination.VaultEntry -> {
                VaultEntryScreen(
                    editingItem = editingItem,
                    clipboardCountdown = clipboardCountdown,
                    onSave = { id, title, user, pass, notes, cat ->
                        viewModel.saveVaultEntry(id, title, user, pass, notes, cat)
                    },
                    onDelete = { id -> viewModel.deleteVaultEntry(id) },
                    onCancel = { viewModel.navigateTo(ScreenDestination.VaultList) },
                    onGeneratePassword = { viewModel.generateStrongPassword() },
                    onCopyPassword = { pass -> viewModel.copyPasswordToClipboard(pass) }
                )
            }

            is ScreenDestination.Settings -> {
                SettingsScreen(
                    autoLockSeconds = autoLockSeconds,
                    onCycleAutoLock = { viewModel.cycleAutoLockTimer() },
                    onChangePin = { old, new -> viewModel.changePin(old, new) },
                    onClearAllData = { pin -> viewModel.clearAllVaultData(pin) },
                    onBack = { viewModel.navigateTo(ScreenDestination.Dashboard) }
                )
            }
        }
    }
}
