package algorithmx.engage.webview

import algorithmx.engage.utils.JsonUtil
import org.junit.Assert.*
import org.junit.Test

class InAppViewTest {
    @Test
    fun parsesInlineAssetsAndKeepsUuidsAndFractionalAllocation() {
        val view = InAppView.fromMap(fixture())!!
        assertEquals("campaign-uuid", view.campaignId)
        assertEquals(2.5, view.delaySeconds, 0.0)
        assertEquals(35.5, view.variations[0].allocation, 0.0)
        assertTrue(view.variations[0].template.document.contains("color:red"))
        assertTrue(view.variations[0].template.document.contains("window.webkit"))
        assertTrue(view.variations[0].template.translationScript("ar-SA").contains("معلومات"))
        assertTrue(view.variations[0].template.translationScript("ar-SA").contains("\"rtl\":true"))
        assertTrue(view.variations[0].template.translationScript("fr").contains("Info"))
    }

    @Test
    fun allocationIsStableAndZeroAllocationNeverWins() {
        val view = InAppView.fromMap(fixture())!!
        val selections = (0..999).map { account ->
            val id = "customer_$account"
            val choice = view.variation(id)!!.id
            assertEquals(choice, view.variation(id)!!.id)
            assertNotEquals("z", choice)
            choice
        }.toSet()
        assertEquals(setOf("a", "b"), selections)
        // Cross-platform FNV-1a allocation fixture, also asserted in Swift.
        assertEquals("a", view.variation("customer_123")!!.id)
    }

    @Test
    fun rejectsMalformedAndOverallocatedCampaigns() {
        assertTrue(JsonUtil.fromJsonArray("[null,{}]").mapNotNull(InAppView::fromMap).isEmpty())
        assertNull(InAppView.fromMap(emptyMap()))
        val data = fixture().toMutableMap()
        data["frequency"] = 0
        assertNull(InAppView.fromMap(data))
        data["frequency"] = 1
        data["configs"] = mapOf("delaySeconds" to -1)
        assertNull(InAppView.fromMap(data))
    }

    @Suppress("UNCHECKED_CAST")
    private fun fixture(): Map<String, Any> = JsonUtil.fromJson("""{
      "id":"view-uuid","campaignId":"campaign-uuid","language":"ar","priority":1,"frequency":1,
      "configs":{"delaySeconds":2.5},"variations":[
        {"id":"a","allocation":35.5,"parameters":{"coupon":"SAVE10"},"template":{
          "html":"<p data-t-key='title'>Info</p>","css":"p{color:red}","js":"function engage(){}",
          "builderState":{"translations":{"en":{"title":{"type":"text","value":"Info"}},
          "ar":{"title":{"type":"text","value":"معلومات"}}}}}},
        {"id":"b","allocation":64.5,"template":{"html":"<p>Other</p>"}},
        {"id":"z","allocation":0,"template":{"html":"<p>Zero</p>"}}
      ]} """) as Map<String, Any>
}
