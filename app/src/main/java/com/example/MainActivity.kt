package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ui.CipherLockApp
import com.example.ui.theme.CipherLockTheme
import com.example.ui.viewmodel.CipherLockViewModel

class MainActivity : FragmentActivity() {

    private lateinit var viewModel: CipherLockViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = CipherLockViewModel.provideFactory(applicationContext)
        viewModel = ViewModelProvider(this, factory)[CipherLockViewModel::class.java]

        setContent {
            CipherLockTheme {
                CipherLockApp(
                    viewModel = viewModel,
                    activity = this
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAppBackgrounded()
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppForegrounded()
    }
}
