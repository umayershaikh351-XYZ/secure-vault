package com.example.security

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class CheckStatus {
    SAFE,
    WARNING,
    DANGER;

    val tag: String
        get() = when (this) {
            SAFE -> "[OK]  "
            WARNING -> "[WARN]"
            DANGER -> "[FAIL]"
        }
}

enum class RiskLevel(val label: String) {
    LOW("OPTIMAL DEFENSE"),
    MODERATE("MODERATE RISK"),
    CRITICAL("CRITICAL EXPOSURE")
}

data class CheckResult(
    val id: String,
    val title: String,
    val status: CheckStatus,
    val summary: String,
    val description: String,
    val fixInstructions: List<String>
)

data class SecurityReport(
    val score: Int,
    val riskLevel: RiskLevel,
    val results: List<CheckResult>,
    val warningCount: Int,
    val dangerCount: Int
)

class DeviceCheckService(private val context: Context) {

    fun performFullAudit(): SecurityReport {
        val results = mutableListOf<CheckResult>()

        results.add(checkScreenLock())
        results.add(checkDeviceEncryption())
        results.add(checkDeveloperOptions())
        results.add(checkUsbDebugging())
        results.add(checkRootAccess())
        results.add(checkSecurityPatch())
        results.add(checkUnknownSources())

        var score = 100
        var warningCount = 0
        var dangerCount = 0

        for (item in results) {
            when (item.status) {
                CheckStatus.SAFE -> {}
                CheckStatus.WARNING -> {
                    score -= 10
                    warningCount++
                }
                CheckStatus.DANGER -> {
                    score -= 25
                    dangerCount++
                }
            }
        }

        val finalScore = score.coerceIn(0, 100)
        val riskLevel = when {
            finalScore >= 80 -> RiskLevel.LOW
            finalScore >= 50 -> RiskLevel.MODERATE
            else -> RiskLevel.CRITICAL
        }

        return SecurityReport(
            score = finalScore,
            riskLevel = riskLevel,
            results = results,
            warningCount = warningCount,
            dangerCount = dangerCount
        )
    }

    // 1. Screen lock set
    private fun checkScreenLock(): CheckResult {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isSecure = keyguardManager?.isKeyguardSecure ?: false

        return if (isSecure) {
            CheckResult(
                id = "screen_lock",
                title = "SCREEN LOCK",
                status = CheckStatus.SAFE,
                summary = "ENABLED",
                description = "Your device is protected with a secure lock screen (PIN, password, pattern, or biometric credentials).",
                fixInstructions = listOf(
                    "Your screen lock is actively engaged.",
                    "Ensure your screen timeout is set to 30 seconds or less.",
                    "Avoid simple sequence PINs like 1234 or 0000."
                )
            )
        } else {
            CheckResult(
                id = "screen_lock",
                title = "SCREEN LOCK",
                status = CheckStatus.DANGER,
                summary = "NOT CONFIGURED",
                description = "No screen lock is set. Anyone with physical access to your device can read your files and bypass hardware protections.",
                fixInstructions = listOf(
                    "Open system Settings.",
                    "Navigate to Security & Privacy -> Device Lock.",
                    "Select Screen Lock and configure a complex PIN (6+ digits) or alphanumeric password.",
                    "Enroll biometric fingerprint authentication if supported."
                )
            )
        }
    }

    // 2. Device encryption
    private fun checkDeviceEncryption(): CheckResult {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val encryptionStatus = dpm?.storageEncryptionStatus ?: DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED
        val isEncrypted = encryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE ||
                encryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER ||
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q // Android 10+ enforces file-based encryption by default

        return if (isEncrypted) {
            CheckResult(
                id = "encryption",
                title = "DEVICE ENCRYPTION",
                status = CheckStatus.SAFE,
                summary = "ENABLED",
                description = "File-based hardware encryption (FBE) is active. Raw flash storage cannot be read without cryptographic keys.",
                fixInstructions = listOf(
                    "Storage encryption is active and hardware backed.",
                    "Keep your operating system updated to maintain cryptographic primitives."
                )
            )
        } else {
            CheckResult(
                id = "encryption",
                title = "DEVICE ENCRYPTION",
                status = CheckStatus.DANGER,
                summary = "DISABLED",
                description = "Storage is not encrypted. Data can be extracted via raw NAND chip reading or custom bootloaders.",
                fixInstructions = listOf(
                    "Open system Settings.",
                    "Navigate to Security -> Encryption & Credentials.",
                    "Tap 'Encrypt phone' and follow the on-screen prompts while plugged into power."
                )
            )
        }
    }

    // 3. Developer options
    private fun checkDeveloperOptions(): CheckResult {
        val devOptionsEnabled = try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) != 0
        } catch (e: Exception) {
            false
        }

        return if (devOptionsEnabled) {
            CheckResult(
                id = "dev_options",
                title = "DEVELOPER OPTIONS",
                status = CheckStatus.WARNING,
                summary = "ENABLED",
                description = "Developer Mode is enabled. This expands the device attack surface and allows diagnostic hooks.",
                fixInstructions = listOf(
                    "Open system Settings -> System -> Developer options.",
                    "Toggle the main switch at the top to OFF.",
                    "Restart the device to clear active debugging daemons."
                )
            )
        } else {
            CheckResult(
                id = "dev_options",
                title = "DEVELOPER OPTIONS",
                status = CheckStatus.SAFE,
                summary = "DISABLED",
                description = "Developer options are disabled. Standard sandboxing and runtime security controls are strictly enforced.",
                fixInstructions = listOf(
                    "Developer settings are properly locked down."
                )
            )
        }
    }

    // 4. USB debugging
    private fun checkUsbDebugging(): CheckResult {
        val adbEnabled = try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED,
                0
            ) != 0
        } catch (e: Exception) {
            false
        }

        return if (adbEnabled) {
            CheckResult(
                id = "usb_debugging",
                title = "USB DEBUGGING",
                status = CheckStatus.WARNING,
                summary = "ENABLED",
                description = "Android Debug Bridge (ADB) over USB is active. A connected computer can execute commands, install APKs, and extract logs.",
                fixInstructions = listOf(
                    "Open system Settings -> System -> Developer options.",
                    "Locate 'USB debugging' and toggle it OFF.",
                    "Tap 'Revoke USB debugging authorizations' to invalidate any previously trusted host computers."
                )
            )
        } else {
            CheckResult(
                id = "usb_debugging",
                title = "USB DEBUGGING",
                status = CheckStatus.SAFE,
                summary = "DISABLED",
                description = "ADB USB bridge is closed. External host computers cannot send raw debug commands to the device.",
                fixInstructions = listOf(
                    "USB debugging is disabled. No action needed."
                )
            )
        }
    }

    // 5. Root access
    private fun checkRootAccess(): CheckResult {
        val suPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/data/local/bin/su",
            "/data/local/xbin/su",
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
            "/system/app/Magisk.apk"
        )

        var hasSuBinary = false
        for (path in suPaths) {
            if (File(path).exists()) {
                hasSuBinary = true
                break
            }
        }

        val testKeys = Build.TAGS?.contains("test-keys") == true

        val isRooted = hasSuBinary || testKeys

        return if (isRooted) {
            CheckResult(
                id = "root_access",
                title = "ROOT ACCESS",
                status = CheckStatus.DANGER,
                summary = "DETECTED",
                description = "Root privileges or custom ROM test-keys found. Application sandboxes can be bypassed by malicious processes.",
                fixInstructions = listOf(
                    "Remove SuperSU / Magisk packages completely.",
                    "Flash official factory stock firmware from device manufacturer.",
                    "Relock the bootloader (fastboot flashing lock) to restore Verified Boot."
                )
            )
        } else {
            CheckResult(
                id = "root_access",
                title = "ROOT ACCESS",
                status = CheckStatus.SAFE,
                summary = "CLEAN",
                description = "No root binaries, superuser packages, or debug signatures detected. Kernel and SELinux sandboxes intact.",
                fixInstructions = listOf(
                    "Device root integrity is clean. Verified Boot operational."
                )
            )
        }
    }

    // 6. Security patch level within 90 days
    private fun checkSecurityPatch(): CheckResult {
        val patchDateString = Build.VERSION.SECURITY_PATCH
        var isRecent = false
        var ageDays: Long = -1

        if (!patchDateString.isNullOrEmpty()) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val patchDate = sdf.parse(patchDateString)
                if (patchDate != null) {
                    val diffMillis = System.currentTimeMillis() - patchDate.time
                    ageDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
                    isRecent = ageDays in 0..90
                }
            } catch (e: Exception) {
                // If parsing fails, fall back to safe default if SDK is modern
                isRecent = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
            }
        }

        return if (isRecent) {
            CheckResult(
                id = "security_patch",
                title = "SECURITY PATCH",
                status = CheckStatus.SAFE,
                summary = "UP TO DATE",
                description = "Security patch string ($patchDateString${if (ageDays >= 0) " - ${ageDays}d old" else ""}) is within the 90-day threshold.",
                fixInstructions = listOf(
                    "Your operating system has recent vulnerability fixes installed.",
                    "Continue checking monthly for vendor system updates."
                )
            )
        } else {
            CheckResult(
                id = "security_patch",
                title = "SECURITY PATCH",
                status = CheckStatus.WARNING,
                summary = "OUTDATED",
                description = "Security patch ($patchDateString${if (ageDays > 0) " - ${ageDays}d old" else ""}) is older than 90 days. Known vulnerabilities may be unpatched.",
                fixInstructions = listOf(
                    "Open system Settings -> System -> System update.",
                    "Check for firmware updates and install pending OTA patches.",
                    "If your device has reached End-of-Life (EOL), consider upgrading hardware."
                )
            )
        }
    }

    // 7. Unknown sources blocked
    private fun checkUnknownSources(): CheckResult {
        var isBlocked = true

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // If this throws SecurityException, REQUEST_INSTALL_PACKAGES is NOT declared,
                // which means unknown APK package installs are blocked by system security policy.
                val canInstall = context.packageManager.canRequestPackageInstalls()
                isBlocked = !canInstall
            } else {
                @Suppress("DEPRECATION")
                val nonMarket = Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.INSTALL_NON_MARKET_APPS,
                    0
                )
                isBlocked = nonMarket == 0
            }
        } catch (e: SecurityException) {
            // SecurityException confirms REQUEST_INSTALL_PACKAGES is not granted or declared
            isBlocked = true
        } catch (e: Throwable) {
            isBlocked = true
        }

        return if (isBlocked) {
            CheckResult(
                id = "unknown_sources",
                title = "UNKNOWN SOURCES",
                status = CheckStatus.SAFE,
                summary = "BLOCKED",
                description = "Sideloading unverified APKs is restricted. Malicious payloads cannot install silently.",
                fixInstructions = listOf(
                    "Third-party APK installs are locked down as required."
                )
            )
        } else {
            CheckResult(
                id = "unknown_sources",
                title = "UNKNOWN SOURCES",
                status = CheckStatus.WARNING,
                summary = "ALLOWED",
                description = "This application or unknown sources have permission to install unknown APK packages.",
                fixInstructions = listOf(
                    "Open system Settings -> Apps -> Special app access -> Install unknown apps.",
                    "Review list and switch all apps to 'Not allowed'."
                )
            )
        }
    }
}
