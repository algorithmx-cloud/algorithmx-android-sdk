package algorithmx.engage.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IdentityPayloadTest {
    @Test
    fun separatesTrustedIdentifiersFromCustomerTraits() {
        val attributes = mapOf<String, Any>("email" to "user@example.com", "phone" to "+905315013673",
            "firstName" to "Amina", "pushOptIn" to true, "emailOptIn" to false)
        assertEquals(mapOf("ExternalId" to "customer_123", "Email" to "user@example.com", "Phone" to "+905315013673"),
            IdentityPayload.identifiers("customer_123", attributes))
        assertEquals(mapOf("firstName" to "Amina", "pushOptIn" to true, "emailOptIn" to false), IdentityPayload.profile(attributes))
    }

    @Test
    fun identityWithoutTraitsDoesNotWipeCustomerProfile() {
        assertTrue(IdentityPayload.profile(mapOf("email" to "user@example.com")).isEmpty())
        assertEquals(mapOf("ExternalId" to "customer_123"), IdentityPayload.identifiers("customer_123", null))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnsupportedCustomerTraits() { IdentityPayload.profile(mapOf("plan" to "premium")) }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsConsentAsText() { IdentityPayload.profile(mapOf("emailOptIn" to "true")) }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsBlankExternalId() { IdentityPayload.identifiers("  ", null) }
}
