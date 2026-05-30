package com.example.exame2.domain.model

data class Ticket(
    val id: String,
    val title: String,
    val description: String,
    val priority: TicketPriority,
    val status: TicketStatus,
    val category: TicketCategory,
    val supplier: String,
    val createdAt: String,
    val reportedBy: String
)

enum class TicketPriority(val label: String, val order: Int) {
    CRITICAL("Critical", 0),
    HIGH("High", 1),
    MEDIUM("Medium", 2),
    LOW("Low", 3)
}

enum class TicketStatus(val label: String) {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed")
}

enum class TicketCategory(val label: String) {
    DISTRIBUTION("Distribution"),
    INVENTORY("Inventory"),
    SUPPLIER("Supplier"),
    LOGISTICS("Logistics"),
    PACKAGING("Packaging")
}
