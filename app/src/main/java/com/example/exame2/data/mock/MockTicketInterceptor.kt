package com.example.exame2.data.mock

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MockTicketInterceptor : Interceptor {

    private val gson = Gson()

    companion object {
        // Shared in-memory store — simulates backend state for the PoC
        val ticketStore: LinkedHashMap<String, JsonObject> = LinkedHashMap<String, JsonObject>().apply {
            put("TKT-001", buildTicket(
                id = "TKT-001",
                title = "Critical stockout of Group A sticker packs at Mexico City",
                description = "Distributor reports zero remaining units of Group A (Mexico, USA, Canada) sticker packs at the Mexico City distribution center. FIFA deadline in 48 hours.",
                priority = "CRITICAL",
                status = "OPEN",
                category = "INVENTORY",
                supplier = "Distribuidora Centroamericana S.A.",
                createdAt = "2026-05-28",
                reportedBy = "Carlos Mendoza"
            ))
            put("TKT-002", buildTicket(
                id = "TKT-002",
                title = "Damaged packaging batch from Brazil printing facility",
                description = "Batch #BR-2026-447 arrived with moisture damage affecting ~30% of sticker packs. Supplier: Panini Group Mexico. Requires full replacement before retail distribution.",
                priority = "HIGH",
                status = "IN_PROGRESS",
                category = "PACKAGING",
                supplier = "Panini Group Mexico",
                createdAt = "2026-05-27",
                reportedBy = "Ana Torres"
            ))
            put("TKT-003", buildTicket(
                id = "TKT-003",
                title = "Missing shipment #SH-2026-0442 to Costa Rica",
                description = "Shipment containing 5,000 standard album units and 20,000 sticker packs has not arrived at San Jose warehouse. Last tracking update: Panama City customs, 72 hours ago.",
                priority = "HIGH",
                status = "OPEN",
                category = "LOGISTICS",
                supplier = "LogiSticker Costa Rica",
                createdAt = "2026-05-26",
                reportedBy = "Luis Vargas"
            ))
            put("TKT-004", buildTicket(
                id = "TKT-004",
                title = "Sticker duplicate error in print run batch #PR-789",
                description = "Quality control detected sticker #234 (Lionel Messi) printed twice in batch #PR-789, replacing sticker #198 (Rodrigo De Paul). Affects approximately 12,000 album kits.",
                priority = "MEDIUM",
                status = "IN_PROGRESS",
                category = "SUPPLIER",
                supplier = "Editorial SportCards S.A.",
                createdAt = "2026-05-25",
                reportedBy = "Maria Fernandez"
            ))
            put("TKT-005", buildTicket(
                id = "TKT-005",
                title = "Distribution route delay: Central America zone 3",
                description = "Logistics partner reports 5-day delay in Central America Zone 3 (Guatemala, Honduras, El Salvador) due to road infrastructure issues. 8 distribution points affected.",
                priority = "MEDIUM",
                status = "OPEN",
                category = "DISTRIBUTION",
                supplier = "Distribuidora FIFA LATAM",
                createdAt = "2026-05-24",
                reportedBy = "Roberto Jimenez"
            ))
            put("TKT-006", buildTicket(
                id = "TKT-006",
                title = "Inventory count mismatch at Guadalajara warehouse",
                description = "Physical inventory count shows 3,200 fewer sticker packs than system records at Guadalajara warehouse. Audit initiated. No distribution impact at this time.",
                priority = "LOW",
                status = "RESOLVED",
                category = "INVENTORY",
                supplier = "Distribuidora Centroamericana S.A.",
                createdAt = "2026-05-22",
                reportedBy = "Sofia Reyes"
            ))
        }

        private fun buildTicket(
            id: String,
            title: String,
            description: String,
            priority: String,
            status: String,
            category: String,
            supplier: String,
            createdAt: String,
            reportedBy: String
        ): JsonObject = JsonObject().apply {
            addProperty("id", id)
            addProperty("title", title)
            addProperty("description", description)
            addProperty("priority", priority)
            addProperty("status", status)
            addProperty("category", category)
            addProperty("supplier", supplier)
            addProperty("created_at", createdAt)
            addProperty("reported_by", reportedBy)
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.encodedPath
        val method = request.method

        Thread.sleep(400)

        val (code, body) = when {
            url.endsWith("/auth/login") && method == "POST" -> handleLogin()
            url.matches(Regex(".*/tickets/[^/]+/status")) && method == "PATCH" -> handleUpdateStatus(url, request)
            url.matches(Regex(".*/tickets/[^/]+/priority")) && method == "PATCH" -> handleUpdatePriority(url, request)
            url.matches(Regex(".*/tickets/[^/]+")) && method == "GET" -> handleGetById(url)
            url.endsWith("/tickets") && method == "GET" -> handleGetAll()
            url.endsWith("/tickets") && method == "POST" -> handleCreate(request)
            else -> 404 to """{"error":"Not found"}"""
        }

        return Response.Builder()
            .code(code)
            .message(if (code in 200..299) "OK" else "Error")
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .body(body.toResponseBody("application/json; charset=utf-8".toMediaTypeOrNull()))
            .build()
    }

    private fun handleLogin(): Pair<Int, String> {
        val response = JsonObject().apply {
            addProperty("token", "mock-jwt-token-panini-2026")
            addProperty("username", "admin")
            addProperty("role", "OPERATOR")
        }
        return 200 to gson.toJson(response)
    }

    private fun handleGetAll(): Pair<Int, String> {
        val list = ticketStore.values.toList()
        return 200 to gson.toJson(list)
    }

    private fun handleGetById(url: String): Pair<Int, String> {
        val id = url.substringAfterLast("/")
        val ticket = ticketStore[id]
        return if (ticket != null) {
            200 to gson.toJson(ticket)
        } else {
            404 to """{"error":"Ticket $id not found"}"""
        }
    }

    private fun handleCreate(request: okhttp3.Request): Pair<Int, String> {
        val bodyJson = readBody(request)
        val newId = "TKT-${System.currentTimeMillis().toString().takeLast(5)}"
        val newTicket = buildTicket(
            id = newId,
            title = bodyJson?.get("title")?.asString ?: "New Ticket",
            description = bodyJson?.get("description")?.asString ?: "",
            priority = bodyJson?.get("priority")?.asString ?: "MEDIUM",
            status = "OPEN",
            category = bodyJson?.get("category")?.asString ?: "SUPPLIER",
            supplier = bodyJson?.get("supplier")?.asString ?: "Unknown",
            createdAt = "2026-05-30",
            reportedBy = "Sistema"
        )
        ticketStore[newId] = newTicket
        return 201 to gson.toJson(newTicket)
    }

    private fun handleUpdateStatus(url: String, request: okhttp3.Request): Pair<Int, String> {
        val id = url.substringAfterLast("/status").let {
            url.removePrefix("/").split("/").let { parts ->
                parts.getOrNull(parts.indexOf("tickets") + 1) ?: ""
            }
        }
        val bodyJson = readBody(request)
        val newStatus = bodyJson?.get("status")?.asString ?: "OPEN"
        val ticket = ticketStore[id]
        return if (ticket != null) {
            ticket.addProperty("status", newStatus)
            200 to gson.toJson(ticket)
        } else {
            val fallback = ticketStore.values.firstOrNull() ?: JsonObject()
            fallback.addProperty("status", newStatus)
            200 to gson.toJson(fallback)
        }
    }

    private fun handleUpdatePriority(url: String, request: okhttp3.Request): Pair<Int, String> {
        val id = url.removePrefix("/").split("/").let { parts ->
            parts.getOrNull(parts.indexOf("tickets") + 1) ?: ""
        }
        val bodyJson = readBody(request)
        val newPriority = bodyJson?.get("priority")?.asString ?: "MEDIUM"
        val ticket = ticketStore[id]
        return if (ticket != null) {
            ticket.addProperty("priority", newPriority)
            200 to gson.toJson(ticket)
        } else {
            val fallback = ticketStore.values.firstOrNull() ?: JsonObject()
            fallback.addProperty("priority", newPriority)
            200 to gson.toJson(fallback)
        }
    }

    private fun readBody(request: okhttp3.Request): JsonObject? {
        return try {
            val buffer = okio.Buffer()
            request.body?.writeTo(buffer)
            gson.fromJson(buffer.readUtf8(), JsonObject::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
