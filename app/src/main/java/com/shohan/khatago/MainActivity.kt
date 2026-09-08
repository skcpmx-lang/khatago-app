package com.shohan.khatago

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.appcompat.app.AppCompatDelegate
import com.shohan.khatago.ui.KhataGoApp
import com.shohan.khatago.ui.theme.KhataGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContent {
            KhataGoTheme {
                KhataGoApp((application as KhataGoApplication).container)
            }
        }
    }
}
