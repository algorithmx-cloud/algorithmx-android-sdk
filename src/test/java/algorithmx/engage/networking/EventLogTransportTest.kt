package algorithmx.engage.networking

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.ServerSocket
import java.time.OffsetDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class EventLogTransportTest {
    @Test
    fun postsEventArrayWithProfileHeadersAndPreservesCustomData() {
        ServerSocket(0).use { server ->
            val executor = Executors.newSingleThreadExecutor()
            val received = executor.submit<Pair<List<String>, String>> {
                server.accept().use { socket ->
                    socket.soTimeout = 5000
                    val reader = socket.getInputStream().bufferedReader()
                    val headers = mutableListOf<String>()
                    while (true) {
                        val line = reader.readLine() ?: error("Incomplete request")
                        if (line.isEmpty()) break
                        headers.add(line)
                    }
                    val length = headers.first { it.startsWith("Content-Length:", true) }
                        .substringAfter(":").trim().toInt()
                    val body = CharArray(length)
                    var offset = 0
                    while (offset < length) {
                        val count = reader.read(body, offset, length - offset)
                        check(count > 0)
                        offset += count
                    }
                    val response = "{\"eventsQueued\":1}"
                    socket.getOutputStream().write(
                        ("HTTP/1.1 202 Accepted\r\nContent-Type: application/json\r\n" +
                            "Content-Length: ${response.length}\r\nConnection: close\r\n\r\n$response").toByteArray()
                    )
                    headers to String(body)
                }
            }
            try {
                NetworkClient.partnerId = "test-partner"
                NetworkClient.postJson(
                    "http://127.0.0.1:${server.localPort}/api/v1/Event/Log",
                    listOf(mapOf(
                        "eventType" to "Purchase_COMPLETED/Custom",
                        "data" to mapOf("Product_ID" to "SKU-123", "nested" to listOf(1, true)),
                        "timestamp" to "2026-10-07T12:00:00.123Z"
                    )),
                    mapOf("X-Anonymous-Id" to "customer_123")
                )
                val (headers, body) = received.get(5, TimeUnit.SECONDS)
                assertEquals("POST /api/v1/Event/Log HTTP/1.1", headers.first())
                assertTrue(headers.any { it.equals("x-partner-id: test-partner", true) })
                assertTrue(headers.any { it.equals("X-Anonymous-Id: customer_123", true) })
                val events = JsonParser.parseString(body).asJsonArray
                assertEquals(1, events.size())
                val event = events[0].asJsonObject
                assertEquals("Purchase_COMPLETED/Custom", event["eventType"].asString)
                assertEquals("SKU-123", event["data"].asJsonObject["Product_ID"].asString)
                assertTrue(event["data"].asJsonObject["nested"].asJsonArray[1].asBoolean)
                assertFalse(event.has("fingerprintDevice"))
                OffsetDateTime.parse(event["timestamp"].asString)
            } finally {
                NetworkClient.partnerId = ""
                executor.shutdownNow()
            }
        }
    }
}
