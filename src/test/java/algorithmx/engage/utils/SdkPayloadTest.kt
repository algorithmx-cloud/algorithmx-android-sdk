package algorithmx.engage.utils

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SdkPayloadTest {
    @Test
    fun legacyNotificationFieldsAndBuiltInActionsUseCanonicalNames() {
        val input = mapOf(
            "engage_action" to "algo_show_notification",
            "algo_campaign_id" to "12",
            "algo_notification_id" to "34",
            "engage_variation_id" to "56",
            "engage_webview_url" to "https://example.com",
            "engage_user_id" to "user1",
            "engage_dynamic_content" to "{\"product_id\":\"sku123\"}",
            "engage_meta_image_url" to "https://example.com/image.png",
            "action_type" to "open_web_page",
            "action_buttons" to "[{\"action_text\":\"show_offer\"}]",
            "image_url" to "https://example.com/hero.png"
        )
        val result = SdkPayload.notification(input)

        assertEquals("algoShowNotification", result["engageAction"])
        assertEquals("openWebPage", result["actionType"])
        assertEquals("12", result["algoCampaignId"])
        assertEquals("34", result["algoNotificationId"])
        assertEquals("56", result["engageVariationId"])
        assertEquals(input["engage_webview_url"], result["engageWebviewUrl"])
        assertEquals("user1", result["engageUserId"])
        assertEquals(input["engage_dynamic_content"], result["engageDynamicContent"])
        assertEquals(input["engage_meta_image_url"], result["engageMetaImageUrl"])
        assertEquals("[{\"actionText\":\"show_offer\"}]", result["actionButtons"])
        assertEquals(input["image_url"], result["imageUrl"])
        input.keys.forEach { assertFalse(result.containsKey(it)) }
        assertTrue(input.containsKey("engage_action"))
    }

    @Test
    fun canonicalFieldsTakePrecedenceEvenWhenNullOrEmpty() {
        val result = SdkPayload.notification(mapOf(
            "engageAction" to "algoTriggerWebview",
            "engage_action" to "algo_show_notification",
            "algoCampaignId" to "",
            "algo_campaign_id" to "12",
            "actionType" to null,
            "action_type" to "open_screen"
        ))

        assertEquals("algoTriggerWebview", result["engageAction"])
        assertEquals("", result["algoCampaignId"])
        assertTrue(result.containsKey("actionType"))
        assertEquals(null, result["actionType"])
        assertFalse(result.containsKey("engage_action"))
        assertFalse(result.containsKey("action_type"))
    }

    @Test
    fun customKeysNestedDataAndActionTextRemainUntouched() {
        val custom = mapOf("Product_ID" to listOf("campaign_close", "open_screen"))
        val input = mapOf<String, Any>(
            "action_type" to "custom_action",
            "actionData" to custom,
            "engage_dynamic_content" to custom,
            "action_text" to "open_screen",
            "custom_field" to "algo_show_notification"
        )
        val result = SdkPayload.notification(input)

        assertEquals("customAction", result["actionType"])
        assertSame(custom, result["actionData"])
        assertSame(custom, result["engageDynamicContent"])
        assertEquals("open_screen", result["actionText"])
        assertEquals("algo_show_notification", result["custom_field"])
        assertEquals("open_screen", input["action_text"])
    }

    @Test
    fun buttonJsonCallbacksUseCanonicalKeysAndPreserveCustomValues() {
        val raw = """[{"id":"special_offer","title":"Offer Title!","action_text":"open_screen","custom_number":1,"actionData":{"action_text":"unchanged"}},{"actionText":"customAction","action_text":"old_action"}]"""
        val input = mapOf("action_buttons" to raw)
        val result = SdkPayload.notification(input)
        val buttons = JsonParser.parseString(result.getValue("actionButtons")).asJsonArray
        val button = buttons[0].asJsonObject

        assertFalse(button.has("action_text"))
        assertEquals("open_screen", button.get("actionText").asString)
        assertEquals("special_offer", button.get("id").asString)
        assertEquals("Offer Title!", button.get("title").asString)
        assertEquals("1", button.get("custom_number").toString())
        assertEquals("unchanged", button.getAsJsonObject("actionData").get("action_text").asString)
        assertEquals("customAction", buttons[1].asJsonObject.get("actionText").asString)
        assertFalse(buttons[1].asJsonObject.has("action_text"))
        assertEquals(raw, input["action_buttons"])
    }

    @Test
    fun buttonArraysPreserveCustomDataAndCanonicalNullPrecedence() {
        val nested = mapOf("custom_key" to "custom_value")
        val input = mapOf<String, Any>("actionButtons" to listOf(
            mapOf("action_text" to "show_offer", "custom_data" to nested),
            mapOf("actionText" to null, "action_text" to "ignored"),
            "custom_item"
        ))
        val result = SdkPayload.notification(input)["actionButtons"] as List<*>
        val first = result[0] as Map<*, *>
        val second = result[1] as Map<*, *>

        assertEquals("show_offer", first["actionText"])
        assertSame(nested, first["custom_data"])
        assertFalse(first.containsKey("action_text"))
        assertTrue(second.containsKey("actionText"))
        assertEquals(null, second["actionText"])
        assertFalse(second.containsKey("action_text"))
        assertEquals("custom_item", result[2])
        assertEquals("{invalid", SdkPayload.notification(mapOf("actionButtons" to "{invalid"))["actionButtons"])
    }

    @Test
    fun webViewAliasesOnlyMatchKnownBuiltInEvents() {
        mapOf(
            "campaign_close" to "campaignClose",
            "campaign_click" to "campaignClick",
            "campaign_submit" to "campaignSubmit",
            "campaign_coupon_copy" to "campaignCouponCopy",
            "copy_coupon" to "copyCoupon"
        ).forEach { (legacy, canonical) ->
            assertEquals(canonical, SdkPayload.builtInEvent(legacy))
            assertEquals(canonical, SdkPayload.builtInEvent(canonical))
        }
        listOf("Purchase_Completed", "campaign_click_custom", "open_screen").forEach {
            assertEquals(it, SdkPayload.builtInEvent(it))
        }
    }
}
