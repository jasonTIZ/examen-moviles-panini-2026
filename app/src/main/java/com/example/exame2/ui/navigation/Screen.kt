package com.example.exame2.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object TicketList : Screen("ticket_list")
    data object CreateTicket : Screen("create_ticket")
    data object TicketDetail : Screen("ticket_detail/{ticketId}") {
        fun createRoute(ticketId: String) = "ticket_detail/$ticketId"
    }
}
