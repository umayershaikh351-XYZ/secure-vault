package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.crypto.CryptoService
import com.example.security.CheckResult
import com.example.security.CheckStatus
import com.example.security.DeviceCheckService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CipherLock", appName)
  }

  @Test
  fun `verify password generation strength`() {
    val cryptoService = CryptoService()
    val password = cryptoService.generateSecurePassword(16)
    assertEquals(16, password.length)
    assertTrue("Should contain uppercase", password.any { it.isUpperCase() })
    assertTrue("Should contain lowercase", password.any { it.isLowerCase() })
    assertTrue("Should contain digit", password.any { it.isDigit() })
  }

  @Test
  fun `verify encrypt and decrypt round trip`() {
    val cryptoService = CryptoService()
    val secret = "SecretAccessCode123!"
    val encrypted = cryptoService.encrypt(secret)
    assertTrue(encrypted.contains(":"))
    val decrypted = cryptoService.decrypt(encrypted)
    assertEquals(secret, decrypted)
  }

  @Test
  fun `device security check runs without crashing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val service = DeviceCheckService(context)
    val report = service.performFullAudit()
    assertNotNull(report)
    assertEquals(7, report.results.size)
    assertTrue(report.score in 0..100)
  }
}
