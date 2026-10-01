package com.example.heliora

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.heliora.ui.theme.HelioraTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private var isReady by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        // Request Camera Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        splashScreen.setKeepOnScreenCondition { !isReady }

        var startScreen = "onboarding"
        var email = ""
        var name = "User"

        try {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            if (currentUser != null) {
                startScreen = "feed"
                email = currentUser.email ?: ""
                name = currentUser.displayName ?: "User"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            HelioraTheme {
                var currentScreen by remember { mutableStateOf(startScreen) }
                var userEmail by remember { mutableStateOf(email) } 
                var userName by remember { mutableStateOf(name) } 


                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        "onboarding" -> {
                            OnboardingScreen(onFinished = { 
                                currentScreen = "login"
                            })
                        }
                        "login" -> {
                            LoginScreen(
                                onNavigateToRegister = { currentScreen = "register" },
                                onLoginSuccess = { email, loginName -> 
                                    userEmail = email
                                    userName = loginName
                                    currentScreen = "feed" 
                                }
                            )
                        }
                        "register" -> {
                            RegisterScreen(
                                onBackToLogin = { currentScreen = "login" },
                                onRegisterSuccess = { 
                                    currentScreen = "login" 
                                }
                            )
                        }
                        "feed" -> {
                            MainFeedScreen(
                                loggedInEmail = userEmail,
                                initialName = userName,
                                onLogout = {
                                try {
                                    FirebaseAuth.getInstance().signOut()
                                } catch (e: Exception) {}
                                userEmail = ""
                                userName = "User"
                                currentScreen = "login"
                            })
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    isReady = true
                }
            }
        }
    }
}
