package algorithmx.engage.core

import algorithmx.engage.interfaces.CampaignInteractionListener
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AlgorithmXContractTest {
    @Test
    fun publicInteractionsPreserveCustomTypesAndPayloads() {
        val payload = mapOf<String, Any>(
            "Product_ID" to "sku123",
            "action_type" to "open_screen",
            "nested_payload" to mapOf("campaign_close" to listOf("custom_value"))
        )
        var receivedType: String? = null
        var receivedPayload: Map<String, Any>? = null
        AlgorithmX.setCampaignInteractionListener(object : CampaignInteractionListener {
            override fun onCampaignInteraction(
                campaignId: String,
                variationId: String,
                interactionType: String,
                payload: Map<String, Any>
            ) {
                receivedType = interactionType
                receivedPayload = payload
            }
        })
        try {
            AlgorithmX.trackCampaignInteraction("12", "34", "close_dismiss", payload)

            assertEquals("close_dismiss", receivedType)
            assertSame(payload, receivedPayload)
            assertEquals("open_screen", receivedPayload?.get("action_type"))
        } finally {
            AlgorithmX.removeCampaignInteractionListener()
        }
    }
}
