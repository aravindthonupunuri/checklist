package com.tgt.backpackregistrychecklists.transport.kafka.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tgt.lists.lib.api.util.EventType

data class DeleteChecklistActionEvent(

    @JsonProperty("guest_id")
    val guestId: String,
    @JsonProperty("template_id")
    val templateId: Int,
    @JsonProperty("retry_state")
    var retryState: String? = null
) {
    companion object {
        private val jsonMapper = jacksonObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

        @JvmStatic
        fun getEventType(): EventType {
            return "DELETE-CHECKLIST-ACTION-EVENT"
        }

        @JvmStatic
        fun deserialize(byteArray: ByteArray): DeleteChecklistActionEvent {
            return jsonMapper.readValue(byteArray, DeleteChecklistActionEvent::class.java)
        }
    }
}
