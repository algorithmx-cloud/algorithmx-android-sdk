package algorithmx.engage.core

import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class IdentityStorageTest {
    @Test
    fun generatedUuidCanBeRestoredAcrossRestarts() {
        val generated = IdentityStorage.uuid(null)
        assertNotNull(UUID.fromString(generated))
        assertEquals(generated, IdentityStorage.uuid(generated))
        assertEquals(generated, IdentityStorage.uuid(generated.uppercase()))
    }

    @Test
    fun invalidStoredValuesCannotBecomeAnonymousIdentifiers() {
        for (legacy in listOf("customer_123", "abcdef123456", "1-1-1-1-1", "")) {
            val value = IdentityStorage.uuid(legacy)
            assertNotEquals(legacy, value)
            assertNotNull(UUID.fromString(value))
        }
    }
}
