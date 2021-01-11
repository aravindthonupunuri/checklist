package com.tgt.backpackregistrychecklists.kafka.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tgt.backpackregistrychecklists.service.MarkChecklistService
import com.tgt.backpackregistrychecklists.service.UnmarkChecklistService
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import mu.KotlinLogging
import reactor.core.publisher.Mono
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MigrationCheckListMarkNotifyEventService(
    @Inject val markChecklistService: MarkChecklistService,
    @Inject val unMarkChecklistService: UnmarkChecklistService
) {
    private val logger = KotlinLogging.logger { MigrationCheckListMarkNotifyEventService::class.java.name }

    fun processMigrationCheckListMarkNotifyEvent(
        registryId: UUID,
        checked: Boolean,
        checklistId: Int,
        templateId: Int,
        subChannel: RegistrySubChannel? = null,
        retryState: RetryState
    ): Mono<RetryState> {
        return if (retryState.incompleteState()) {
            logger.debug("[MigrationCheckListMarkNotifyEventService] From processMigrationCheckListMarkNotifyEvent(), starting processing")
            if (checked) {
                markChecklistService.markChecklistId(registryId, checklistId, templateId, subChannel!!)
            } else {
                unMarkChecklistService.unmarkChecklistId(registryId, checklistId, templateId)
            }.map {
                retryState.markCheckList = true
                retryState
            }
                .onErrorResume {
                    logger.error("[MigrationCheckListMarkNotifyEventService] Exception from processMigrationCheckListMarkNotifyEvent() for registry id: $registryId, checked: $checked, template Id: $templateId and checklist id: $checklistId", it)
                    Mono.just(retryState)
                }
        } else {
            logger.debug("[MigrationCheckListMarkNotifyEventService] From processMigrationCheckListMarkNotifyEvent(), processing complete")
            Mono.just(retryState)
        }
    }

    data class RetryState(
        var markCheckList: Boolean = false
    ) {
        fun completeState(): Boolean {
            return markCheckList
        }

        fun incompleteState(): Boolean {
            return !markCheckList
        }

        companion object {
            // jacksonObjectMapper() returns a normal ObjectMapper with the KotlinModule registered
            val jsonMapper: ObjectMapper = jacksonObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

            @JvmStatic
            fun deserialize(retryState: String): RetryState {
                return jsonMapper.readValue<RetryState>(retryState, RetryState::class.java)
            }

            @JvmStatic
            fun serialize(retryState: RetryState): String {
                return jsonMapper.writeValueAsString(retryState)
            }
        }
    }
}
