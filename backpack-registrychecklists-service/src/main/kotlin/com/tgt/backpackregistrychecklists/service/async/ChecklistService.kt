package com.tgt.backpackregistrychecklists.service.async

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import mu.KotlinLogging
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChecklistService(
    @Inject val registryChecklistRepository: RegistryChecklistRepository,
    @Inject val checkedSubCategoriesRepository: CheckedSubCategoriesRepository
) {
    private val logger = KotlinLogging.logger { ChecklistService::class.java.name }
    fun processDeleteRetryState(
        templateId: Int,
        retryState: RetryState
    ): Mono<RetryState> {
        return when {
            retryState.initialState() -> {
                logger.debug("From processDeleteRegistryState(), starting processing")

                registryChecklistRepository.deleteByTemplateId(templateId).map { retryState.deleteChecklistTemplateFromChecklistRepository = true }
                    .map {
                        checkedSubCategoriesRepository.deleteByTemplateId(templateId)
                        retryState.deleteChecklistTemplateFromCheckedSubCategoriesRepository = true
                        retryState
                    }
                    .onErrorResume { Mono.just(retryState) }
                    .switchIfEmpty {
                        Mono.just(retryState)
                    }
            }
            retryState.partialCompleteLevel1State() -> {
                logger.debug("From processDeleteRegistryState, starting processing of partially completed level 1 state")
                registryChecklistRepository.deleteByTemplateId(templateId)
                    .map {
                        retryState.deleteChecklistTemplateFromCheckedSubCategoriesRepository = true
                        retryState
                    }
                    .onErrorResume { Mono.just(retryState) }
                    .switchIfEmpty { Mono.just(retryState) }
            }
            retryState.partialCompleteLevel2State() -> {
                logger.debug("From processDeleteRegistryState, starting processing of partially completed level 2 state")
                checkedSubCategoriesRepository.deleteByTemplateId(templateId)
                    .map {
                        retryState.deleteChecklistTemplateFromChecklistRepository = true
                        retryState
                    }
                    .onErrorResume { Mono.just(retryState) }
                    .switchIfEmpty { Mono.just(retryState) }
            }
            retryState.completeState() -> {
                logger.debug("From processSaveRegistryState(), processing complete")
                Mono.just(retryState)
            }
            else -> {
                logger.error("Unknown step for processDeleteRegistryState()")
                retryState.deleteChecklistTemplateFromChecklistRepository = true
                retryState.deleteChecklistTemplateFromCheckedSubCategoriesRepository = true
                Mono.just(retryState)
            }
        }
    }

    data class RetryState(
        var deleteChecklistTemplateFromChecklistRepository: Boolean = false,
        var deleteChecklistTemplateFromCheckedSubCategoriesRepository: Boolean = false
    ) {
        fun completeState(): Boolean {
            return deleteChecklistTemplateFromChecklistRepository && deleteChecklistTemplateFromCheckedSubCategoriesRepository
        }

        fun initialState(): Boolean {
            return !deleteChecklistTemplateFromChecklistRepository && !deleteChecklistTemplateFromCheckedSubCategoriesRepository
        }

        fun partialCompleteLevel1State(): Boolean {
            return deleteChecklistTemplateFromChecklistRepository && !deleteChecklistTemplateFromCheckedSubCategoriesRepository
        }

        fun partialCompleteLevel2State(): Boolean {
            return !deleteChecklistTemplateFromChecklistRepository && deleteChecklistTemplateFromCheckedSubCategoriesRepository
        }

        companion object {
            // jacksonObjectMapper() returns a normal ObjectMapper with the KotlinModule registered
            private val jsonMapper: ObjectMapper = jacksonObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

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
