package algorithmx.engage.networking

import org.junit.Assert.*
import org.junit.Test
import java.net.ServerSocket
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class InAppViewsTransportTest {
    @Test
    fun fetchesEncodedCustomerQueryAndAnonymousDeviceHeaders() {
        ServerSocket(0).use { server ->
            val executor = Executors.newSingleThreadExecutor()
            val received = executor.submit<List<String>> {
                server.accept().use { socket ->
                    val headers = mutableListOf<String>()
                    val reader = socket.getInputStream().bufferedReader()
                    while (true) { val line = reader.readLine() ?: break; if (line.isEmpty()) break; headers.add(line) }
                    val body = "[]"
                    socket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: 2\r\nConnection: close\r\n\r\n$body").toByteArray())
                    headers
                }
            }
            try {
                val anonymous = "a781ba16-c6fb-469d-83fb-2d2cc0eb0057"
                val device = "b781ba16-c6fb-469d-83fb-2d2cc0eb0057"
                NetworkClient.partnerId = "partner-test"
                val url = NetworkClient.inAppViewsUrl("http://127.0.0.1:${server.localPort}", "ar-SA", "customer & one")
                assertTrue(NetworkClient.getJsonArray(url, mapOf("X-Anonymous-Id" to anonymous, "X-Device-Id" to device)).isEmpty())
                val headers = received.get(5, TimeUnit.SECONDS)
                assertTrue(headers.first().contains("language=ar-SA&customerId=customer%20%26%20one"))
                assertTrue(headers.any { it.equals("X-Anonymous-Id: $anonymous", true) })
                assertTrue(headers.any { it.equals("X-Device-Id: $device", true) })
                assertTrue(headers.any { it.equals("x-partner-id: partner-test", true) })
            } finally { NetworkClient.partnerId = ""; executor.shutdownNow() }
        }
    }
}
