package com.example.dailyquotes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dailyquotes.ui.quotes.QuotesScreen

/**
 * Every route in the app. A single screen today, but routed through a real
 * `NavHost` so additional destinations can be added without restructuring.
 */
sealed class Screen(val route: String) {
    data object Quotes : Screen("quotes")
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Quotes.route,
    ) {
        composable(Screen.Quotes.route) {
            QuotesScreen()
        }
    }
}
