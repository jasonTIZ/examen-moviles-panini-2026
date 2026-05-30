package com.example.exame2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.exame2.ui.navigation.AppNavGraph
import com.example.exame2.ui.theme.Examen2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Examen2Theme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}
