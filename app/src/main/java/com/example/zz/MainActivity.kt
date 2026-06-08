package com.example.zz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.zz.data.repository.UserRepositoryImpl
import com.example.zz.domain.model.UserProfile
import com.example.zz.ui.dashboard.DashboardScreen
import com.example.zz.ui.dashboard.DashboardViewModel
import com.example.zz.ui.onboarding.OnboardingScreen
import com.example.zz.ui.onboarding.OnboardingViewModel
import com.example.zz.ui.theme.ZzTheme

import com.example.zz.ui.auth.AuthScreen
import com.example.zz.ui.auth.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val userRepository = UserRepositoryImpl(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            ZzTheme {
                FitnessApp(userRepository)
            }
        }
    }
}

@Composable
fun FitnessApp(userRepository: UserRepositoryImpl) {
    val navController = rememberNavController()
    
    val userProfile by userRepository.getUserProfile().collectAsStateWithLifecycle(initialValue = null)
    
    // Sprawdzamy stan zalogowania
    val isLoggedIn = userRepository.isUserLoggedIn()
    val startDestination = if (!isLoggedIn) "auth" else {
        if (userProfile == null) "loading"
        else if (userProfile!!.age == 0) "onboarding" 
        else "dashboard"
    }

    NavHost(navController = navController, startDestination = if (!isLoggedIn) "auth" else "loading") {
        composable("auth") {
            val authViewModel: AuthViewModel = viewModel()
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate("onboarding") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("loading") {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
            LaunchedEffect(userProfile) {
                if (userProfile != null) {
                    val destination = if (userProfile!!.age == 0) "onboarding" else "dashboard"
                    navController.navigate(destination) {
                        popUpTo("loading") { inclusive = true }
                    }
                }
            }
        }

        composable("onboarding") {
            val onboardingViewModel: OnboardingViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return OnboardingViewModel(userRepository) as T
                    }
                }
            )
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onFinish = {
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        
        composable("dashboard") {
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return DashboardViewModel(userRepository) as T
                    }
                }
            )
            DashboardScreen(
                viewModel = dashboardViewModel,
                onLogout = {
                    userRepository.logout()
                    navController.navigate("auth") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
    }
}
