package com.tgt.backpackregistrychecklists.service

import com.tgt.backpack.redsky.components.manager.RedskyHydrationManager
import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import com.tgt.backpackregistrychecklists.transport.*
import com.tgt.backpackregistryclient.client.BackpackRegistryClient
import com.tgt.backpackregistryclient.transport.RegistryDetailsResponseTO
import com.tgt.backpackregistryclient.transport.RegistryItemsTO
import com.tgt.backpackregistryclient.util.RegistryChannel
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.lists.common.components.exception.BadRequestException
import com.tgt.lists.common.components.exception.BaseErrorCodes.BAD_REQUEST_ERROR_CODE
import io.micronaut.context.annotation.Value
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetRegistryChecklistsService(
    @Inject private val checklistTemplateRepository: ChecklistTemplateRepository,
    @Inject private val registryChecklistRepository: RegistryChecklistRepository,
    @Inject private val checkedSubCategoriesRepository: CheckedSubCategoriesRepository,
    @Inject private val backpackClient: BackpackRegistryClient,
    @Inject private val redskyHydrationManager: RedskyHydrationManager
) {
    private val logger = KotlinLogging.logger { GetRegistryChecklistsService::class.java.name }
    @Value("\${list.default-web-store-id}") private val storeId: Long = 3991

    fun getChecklistsForRegistryId(
        registryId: UUID,
        guestId: String,
        channel: RegistryChannel,
        subChannel: RegistrySubChannel
    ): Mono<ChecklistResponseTO> {
        return registryChecklistRepository.find(registryId)
            .switchIfEmpty {
                throw BadRequestException(BAD_REQUEST_ERROR_CODE(listOf("RegistryId-$registryId doesn't have an active checklist")))
            }
            .flatMap { registryChecklist ->
                val templateId = registryChecklist.templateId
                // TO-DO Check against cache, if exists, else, populate cache
                checklistTemplateRepository.findByTemplateId(templateId).collectList()
                    .map {
                        if (it.isNullOrEmpty())
                            throw BadRequestException(BAD_REQUEST_ERROR_CODE(listOf("No checklist exists for the given templateId - $templateId")))
                        val categoryMap = hashMapOf<String, ChecklistCategoryTO>()
                        it.map { checklistTemplate ->
                            val subCategories = SubcategoryTO(checklistTemplate)
                            val checklistCategory = ChecklistCategoryTO(checklistTemplate, listOf(subCategories))
                            if (categoryMap.containsKey(checklistCategory.categoryId)) {
                                checklistCategory.subcategories = categoryMap[checklistCategory.categoryId]?.subcategories?.plus(subCategories)
                                categoryMap.put(checklistCategory.categoryId!!, checklistCategory)
                            } else categoryMap.put(checklistCategory.categoryId!!, checklistCategory)
                        }
                        categoryMap
                    }
                    .zipWhen {
                        getDetailsResponse(registryId, guestId, channel, subChannel)
                            .flatMap {
                                if (it?.registryItems.isNullOrEmpty())
                                    Mono.just(listOf(ChecklistItemTO()))
                                else
                                    it?.registryItems?.let { it1 -> getItemDetailsFromRedsky(it1) }
                            }.switchIfEmpty {
                                Mono.just(listOf(ChecklistItemTO()))
                            }
                    }
                    .flatMap {
                        val itemDetails = it.t2
                        updateSubcategories(it.t1, itemDetails).collectList()
                            .flatMap {
                                markSubcategories(registryId, templateId, it)
                                    .map { categoryList ->
                                        ChecklistResponseTO(registryId = registryId, registryItemCount = itemDetails.size.toLong(),
                                            categories = categoryList)
                                    }
                            }
                    }
            }
    }

    fun updateSubcategories(
        categoryMap: HashMap<String, ChecklistCategoryTO>,
        registryDetails: List<ChecklistItemTO>?
    ): Flux<ChecklistCategoryTO> {
        val categoryList = mutableListOf<ChecklistCategoryTO>()
        val itemDetailsMap = hashMapOf<String, List<ChecklistItemTO>>()
        registryDetails?.map {
            if (itemDetailsMap.containsKey(it.nodeId)) {
                val modifiedValue = itemDetailsMap[it.nodeId]?.plus(it)
                itemDetailsMap.put(it.nodeId!!, modifiedValue!!)
            } else it.nodeId?.let { it1 -> itemDetailsMap.put(it1, listOf(it)) }
        }

        categoryMap.forEach { (_, categoryTO) ->
            categoryTO.subcategories?.forEach {
                val subcategoryIdList = it.subcategoryChildIds?.split(",")?.map { it.trim() }
                subcategoryIdList?.forEach { subcategoryId ->
                    if (itemDetailsMap.containsKey(subcategoryId)) {
                        itemDetailsMap[subcategoryId]?.forEach { checklistItemDetails ->
                            val lastUpdatedItem = it.lastUpdatedItem
                            if (isLatestItem(checklistItemDetails, lastUpdatedItem)) {
                                it.lastUpdatedItem = ItemDetailsTO(checklistItemDetails.itemDetails?.tcin, checklistItemDetails.itemDetails?.description,
                                    checklistItemDetails.itemDetails?.imageUrl, checklistItemDetails.itemDetails?.addedTs, checklistItemDetails.itemDetails?.lastModifiedTs)
                            }
                            it.itemCount = it.itemCount?.inc()
                        }
                        it.checked = true
                    }
                }
            }
            categoryList.add(categoryTO)
        }
        return categoryList.toFlux()
    }

    fun isLatestItem(
        checklistItemDetails: ChecklistItemTO,
        lastUpdatedItem: ItemDetailsTO?
    ): Boolean {
        when (lastUpdatedItem) {
            null -> return true
            else -> {
                checklistItemDetails.itemDetails?.lastModifiedTs?.let {
                    if (lastUpdatedItem.lastModifiedTs != null && it.isAfter(lastUpdatedItem.lastModifiedTs))
                        return true
                }
                checklistItemDetails.itemDetails?.addedTs?.let {
                    if ((lastUpdatedItem.lastModifiedTs != null && it.isAfter(lastUpdatedItem.lastModifiedTs)) ||
                        (lastUpdatedItem.addedTs != null && it.isAfter(lastUpdatedItem.addedTs)))
                        return true
                }
                return false
            }
        }
    }

    fun markSubcategories(
        registryId: UUID,
        templateId: Int,
        categoryList: List<ChecklistCategoryTO>
    ): Mono<List<ChecklistCategoryTO>> {
        return checkedSubCategoriesRepository.findByRegistryIdAndTemplateId(registryId, templateId).collectList()
            .map { checklistIdList ->
                categoryList.map {
                    it.subcategories?.map { subCategory ->
                        if (checklistIdList.map { it.checkedSubcategoriesId.checklistId }.contains(subCategory.checklistId))
                            subCategory.checked = true
                    }
                }
                categoryList
            }
    }

    fun getDetailsResponse(
        registryId: UUID,
        guestId: String,
        channel: RegistryChannel,
        subChannel: RegistrySubChannel
    ): Mono<RegistryDetailsResponseTO?> {
        return backpackClient.getRegistryDetails(guestId, registryId, storeId, channel, subChannel, false)
            .switchIfEmpty {
                logger.error("Empty response from get Details API")
                Mono.empty()
            }.onErrorResume {
                logger.error("Error response from get Details API" + it.printStackTrace())
                Mono.empty()
            }
    }

    fun getItemDetailsFromRedsky(
        registryDetails: List<RegistryItemsTO>
    ): Mono<List<ChecklistItemTO>> {
        // Make call to Redsky and get ItemDetails
        return registryDetails.toFlux()
            .flatMap { registryItems ->
                registryItems.tcin?.let { it1 -> redskyHydrationManager.getDetailsForChecklistItems(it1) }
                    ?.map {
                        ChecklistItemTO(nodeId = it.taxonomy?.category?.nodeId, itemDetails = ItemDetailsTO(it.tcin, it.items?.productDescription?.title, it.items?.enrichment?.images?.primaryImageUrl,
                            registryItems?.addedTs, registryItems?.lastModifiedTs))
                    }
            }.collectList()
    }
}