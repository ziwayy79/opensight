package com.opensight.sift

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.opensight.sift.features.sensoryshield.SensoryShieldManager
import com.opensight.sift.ui.screens.AirPodsScreen
import com.opensight.sift.ui.screens.HomeScreen
import com.opensight.sift.ui.theme.SiftTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            SiftTheme {
                val navController = rememberNavController()
                val sensoryShieldManager = remember { SensoryShieldManager(this@MainActivity) }
                
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        HomeScreen(
                            onNavigateToAirPods = {
                                navController.navigate("airpods")
                            }
                        )
                    }
                    
                    composable("airpods") {
                        AirPodsScreen(
                            sensoryShieldManager = sensoryShieldManager,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
