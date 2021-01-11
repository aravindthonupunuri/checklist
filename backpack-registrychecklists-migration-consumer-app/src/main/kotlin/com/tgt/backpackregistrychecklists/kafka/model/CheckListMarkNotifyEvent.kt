package com.tgt.backpackregistrychecklists.kafka.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.lists.msgbus.EventType
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class CheckListMarkNotifyEvent(

    @JsonProperty("list_id")
    val listId: UUID,

    @JsonProperty("checklist_id")
    val checklistId: Int,

    @JsonProperty("template_id")
    val templateId: Int,

    @JsonProperty("checked")
    val checked: Boolean,

    @JsonProperty("sub_channel")
    val subChannel: RegistrySubChannel,

    @JsonProperty("retry_state")
    var retryState: String? = null
) {
    companion object {
        // jacksonObjectMapper() returns a normal ObjectMapper with the KotlinModule registered
        private val jsonMapper = jacksonObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

        @JvmStatic
        fun getEventType(): EventType {
            return "CHECKLIST-MARK-NOTIFY-EVENT"
        }

        @JvmStatic
        fun deserialize(byteArray: ByteArray): CheckListMarkNotifyEvent {
            return jsonMapper.readValue(byteArray, CheckListMarkNotifyEvent::class.java)
        }
    }
}
