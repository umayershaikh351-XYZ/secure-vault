package com.example.ui.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CipherDatabase
import com.example.data.DecryptedVaultItem
import com.example.data.VaultItem
import com.example.data.VaultRepository
import com.example.crypto.CryptoService
import com.example.security.CheckResult
import com.example.security.DeviceCheckService
import com.example.security.RiskLevel
import com.example.security.SecurityReport
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ScreenDestination {
    object Splash : ScreenDestination()
    object Onboarding : ScreenDestination()
    object SetupPin : ScreenDestination()
    object Unlock : ScreenDestination()
    object Dashboard : ScreenDestination()
    object DeviceCheck : ScreenDestination()
    object VaultList : ScreenDestination()
    data class VaultEntry(val itemId: Long? = null) : ScreenDestination()
    object Settings : ScreenDestination()
}

class CipherLockViewModel(
    private val repository: VaultRepository,
    private val deviceCheckService: DeviceCheckService,
    private val context: Context
) : ViewModel() {

    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Splash)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Security Audit State
    private val _securityReport = MutableStateFlow<SecurityReport?>(null)
    val securityReport: StateFlow<SecurityReport?> = _securityReport.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    // Lockout & PIN State
    private val _wrongPinAttempts = MutableStateFlow(0)
    val wrongPinAttempts: StateFlow<Int> = _wrongPinAttempts.asStateFlow()

    private val _isLockedOut = MutableStateFlow(false)
    val isLockedOut: StateFlow<Boolean> = _isLockedOut.asStateFlow()

    private val _lockoutSecondsRemaining = MutableStateFlow(0)
    val lockoutSecondsRemaining: StateFlow<Int> = _lockoutSecondsRemaining.asStateFlow()

    private val _pinErrorFlash = MutableStateFlow(false)
    val pinErrorFlash: StateFlow<Boolean> = _pinErrorFlash.asStateFlow()

    private val _accessDeniedMessage = MutableStateFlow<String?>(null)
    val accessDeniedMessage: StateFlow<String?> = _accessDeniedMessage.asStateFlow()

    // Auto-lock timer
    private val _autoLockSeconds = MutableStateFlow(repository.getAutoLockSeconds())
    val autoLockSeconds: StateFlow<Int> = _autoLockSeconds.asStateFlow()

    private var lastBackgroundTimestamp: Long = 0L

    // Clipboard Auto-purge State
    private val _clipboardPurgeCountdown = MutableStateFlow<Int?>(null)
    val clipboardPurgeCountdown: StateFlow<Int?> = _clipboardPurgeCountdown.asStateFlow()
    private var clipboardJob: Job? = null

    // Vault search & list
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredVaultItems: StateFlow<List<VaultItem>> = combine(
        repository.allItems,
        _searchQuery
    ) { items, query ->
        if (query.isBlank()) {
            items
        } else {
            items.filter { it.title.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current Entry for View/Edit
    private val _editingItem = MutableStateFlow<DecryptedVaultItem?>(null)
    val editingItem: StateFlow<DecryptedVaultItem?> = _editingItem.asStateFlow()

    // Detail dialog for Security Check
    private val _selectedCheck = MutableStateFlow<CheckResult?>(null)
    val selectedCheck: StateFlow<CheckResult?> = _selectedCheck.asStateFlow()

    init {
        // Run initial diagnostics scan in background
        runDeviceScan()
    }

    fun onSplashFinished() {
        if (!repository.isOnboardingDone()) {
            _currentScreen.value = ScreenDestination.Onboarding
        } else if (!repository.isPinConfigured()) {
            _currentScreen.value = ScreenDestination.SetupPin
        } else {
            _currentScreen.value = ScreenDestination.Unlock
        }
    }

    fun completeOnboarding() {
        repository.setOnboardingDone(true)
        if (!repository.isPinConfigured()) {
            _currentScreen.value = ScreenDestination.SetupPin
        } else {
            _currentScreen.value = ScreenDestination.Unlock
        }
    }

    fun setupPin(pin: String) {
        repository.setPin(pin)
        _currentScreen.value = ScreenDestination.Dashboard
    }

    fun attemptUnlock(pinInput: String) {
        if (_isLockedOut.value) return

        if (repository.verifyPin(pinInput)) {
            _wrongPinAttempts.value = 0
            _accessDeniedMessage.value = null
            _pinErrorFlash.value = false
            _currentScreen.value = ScreenDestination.Dashboard
        } else {
            val attempts = _wrongPinAttempts.value + 1
            _wrongPinAttempts.value = attempts
            _pinErrorFlash.value = true
            _accessDeniedMessage.value = "> ACCESS DENIED [ATTEMPT $attempts/3]"

            viewModelScope.launch {
                delay(700)
                _pinErrorFlash.value = false
            }

            if (attempts >= 3) {
                triggerLockout()
            }
        }
    }

    private fun triggerLockout() {
        _isLockedOut.value = true
        _lockoutSecondsRemaining.value = 30
        _accessDeniedMessage.value = "> TERMINAL LOCKED: 3 FAILED ATTEMPTS"

        viewModelScope.launch {
            while (_lockoutSecondsRemaining.value > 0) {
                delay(1000)
                _lockoutSecondsRemaining.value -= 1
            }
            _isLockedOut.value = false
            _wrongPinAttempts.value = 0
            _accessDeniedMessage.value = null
        }
    }

    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticateWithBiometrics(activity: FragmentActivity) {
        if (!isBiometricAvailable() || _isLockedOut.value) return

        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    _wrongPinAttempts.value = 0
                    _accessDeniedMessage.value = null
                    _currentScreen.value = ScreenDestination.Dashboard
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        _accessDeniedMessage.value = "> BIOMETRIC AUTH ERROR: $errString"
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    _pinErrorFlash.value = true
                    _accessDeniedMessage.value = "> BIOMETRIC MISMATCH"
                    viewModelScope.launch {
                        delay(600)
                        _pinErrorFlash.value = false
                    }
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("CIPHERLOCK TERMINAL ACCESS")
            .setSubtitle("Hardware Biometric Verification")
            .setNegativeButtonText("CANCEL")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun runDeviceScan() {
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0f

            for (p in 1..20) {
                delay(40)
                _scanProgress.value = p / 20f
            }

            val report = deviceCheckService.performFullAudit()
            _securityReport.value = report
            _isScanning.value = false
        }
    }

    fun openCheckDetail(check: CheckResult) {
        _selectedCheck.value = check
    }

    fun closeCheckDetail() {
        _selectedCheck.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun navigateTo(screen: ScreenDestination) {
        if (screen is ScreenDestination.VaultEntry) {
            val id = screen.itemId
            if (id != null) {
                viewModelScope.launch {
                    val item = repository.getItemDecrypted(id)
                    _editingItem.value = item
                    _currentScreen.value = screen
                }
                return
            } else {
                _editingItem.value = null
            }
        }
        _currentScreen.value = screen
    }

    fun lockNow() {
        _currentScreen.value = ScreenDestination.Unlock
    }

    // Auto-lock lifecycle
    fun onAppBackgrounded() {
        lastBackgroundTimestamp = System.currentTimeMillis()
    }

    fun onAppForegrounded() {
        if (lastBackgroundTimestamp > 0 && repository.isPinConfigured()) {
            val elapsedSeconds = (System.currentTimeMillis() - lastBackgroundTimestamp) / 1000
            if (elapsedSeconds >= _autoLockSeconds.value) {
                if (_currentScreen.value !is ScreenDestination.Splash &&
                    _currentScreen.value !is ScreenDestination.Onboarding &&
                    _currentScreen.value !is ScreenDestination.SetupPin
                ) {
                    _currentScreen.value = ScreenDestination.Unlock
                }
            }
        }
        lastBackgroundTimestamp = 0L
    }

    fun cycleAutoLockTimer() {
        val current = _autoLockSeconds.value
        val next = when (current) {
            15 -> 30
            30 -> 60
            60 -> 300
            else -> 15
        }
        _autoLockSeconds.value = next
        repository.setAutoLockSeconds(next)
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        if (repository.verifyPin(oldPin)) {
            repository.setPin(newPin)
            return true
        }
        return false
    }

    fun clearAllVaultData(pinConfirm: String): Boolean {
        if (repository.verifyPin(pinConfirm)) {
            viewModelScope.launch {
                repository.wipeAllVault()
                _currentScreen.value = ScreenDestination.SetupPin
            }
            return true
        }
        return false
    }

    fun saveVaultEntry(
        id: Long = 0L,
        title: String,
        user: String,
        pass: String,
        notes: String,
        category: String
    ) {
        viewModelScope.launch {
            repository.saveItem(
                id = id,
                title = title,
                username = user,
                password = pass,
                notes = notes,
                category = category
            )
            _editingItem.value = null
            _currentScreen.value = ScreenDestination.VaultList
        }
    }

    fun deleteVaultEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteItem(id)
            if (_editingItem.value?.id == id) {
                _editingItem.value = null
            }
            _currentScreen.value = ScreenDestination.VaultList
        }
    }

    fun generateStrongPassword(): String {
        return repository.generatePassword()
    }

    /**
     * Copies password to system clipboard and schedules auto-purge after 30 seconds.
     */
    fun copyPasswordToClipboard(password: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("CipherLock", password)
        clipboard.setPrimaryClip(clip)

        clipboardJob?.cancel()
        _clipboardPurgeCountdown.value = 30

        clipboardJob = viewModelScope.launch {
            for (sec in 29 downTo 0) {
                delay(1000)
                _clipboardPurgeCountdown.value = sec
            }
            // Clear clipboard
            try {
                val emptyClip = ClipData.newPlainText("CipherLock", "")
                clipboard.setPrimaryClip(emptyClip)
            } catch (e: Exception) {
                // ignore
            }
            _clipboardPurgeCountdown.value = null
        }
    }

    companion object {
        fun provideFactory(
            context: Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = CipherDatabase.getInstance(context)
                val crypto = CryptoService()
                val repo = VaultRepository(db.vaultDao(), crypto, context)
                val deviceCheck = DeviceCheckService(context)
                return CipherLockViewModel(repo, deviceCheck, context) as T
            }
        }
    }
}
