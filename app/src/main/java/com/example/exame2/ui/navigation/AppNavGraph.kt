package com.example.exame2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.exame2.ui.screen.createticket.CreateTicketScreen
import com.example.exame2.ui.screen.login.LoginScreen
import com.example.exame2.ui.screen.ticketdetail.TicketDetailScreen
import com.example.exame2.ui.screen.ticketlist.TicketListScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.TicketList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TicketList.route) {
            TicketListScreen(
                onTicketClick = { ticketId ->
                    navController.navigate(Screen.TicketDetail.createRoute(ticketId))
                },
                onCreateTicket = {
                    navController.navigate(Screen.CreateTicket.route)
                }
            )
        }

        composable(
            route = Screen.TicketDetail.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: return@composable
            TicketDetailScreen(
                ticketId = ticketId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CreateTicket.route) {
            CreateTicketScreen(
                onBack = { navController.popBackStack() },
                onTicketCreated = { navController.popBackStack() }
            )
        }
    }
}
