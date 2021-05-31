package com.tgt.backpackregistrychecklists.service

import com.tgt.backpack.redsky.components.manager.RedskyHydrationManager
import com.tgt.backpackregistrychecklists.persistence.CheckedSubCategoriesRepository
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.persistence.RegistryChecklistRepository
import com.tgt.backpackregistrychecklists.transport.*
import com.tgt.backpackregistryclient.client.BackpackRegistryClient
import com.tgt.backpackregistryclient.transport.RegistryDetailsResponseTO
import com.tgt.backpackregistryclient.transport.RegistryItemsBasicInfoTO
import com.tgt.backpackregistryclient.util.RegistryChannel
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.lists.common.components.exception.BadRequestException
import com.tgt.lists.common.components.exception.BaseErrorCodes.BAD_REQUEST_ERROR_CODE
import com.tgt.lists.common.components.exception.ErrorCode
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
                throw BadRequestException(ErrorCode(BAD_REQUEST_ERROR_CODE, listOf("RegistryId-$registryId doesn't have an active checklist")))
            }
            .flatMap { registryChecklist ->
                val templateId = registryChecklist.templateId
                // TODO Check against cache, if exists, else, populate cache
                checklistTemplateRepository.findByTemplateId(templateId).collectList()
                    .map {
                        if (it.isNullOrEmpty())
                            throw BadRequestException(ErrorCode(BAD_REQUEST_ERROR_CODE, listOf("No checklist exists for the given templateId - $templateId")))
                        val categoryMap = hashMapOf<String, ChecklistCategoryTO>()
                        // Form a hashMap with key as categoryId and value as ChecklistCategoryTO
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
                        // Make a call to getDetails api of bp-registry
                        getDetailsResponse(registryId, guestId, channel, subChannel)
                            .flatMap {
                                if (it.registryItems.isNullOrEmpty())
                                    Mono.just(emptyList())
                                else
                                    getItemDetailsFromRedsky(it.registryItems!!)
                            }
                            .switchIfEmpty {
                                Mono.just(emptyList())
                            }
                    }
                    .flatMap {
                        val itemDetails = it.t2
                        assembleSubcategories(it.t1, itemDetails).collectList()
                            .flatMap {
                                markSubcategories(registryId, templateId, it)
                                    .map { categoryList ->
                                        val checklistTotalCount = categoryList.map { it.categoryTotalCount }.sum()
                                        val checklistCheckedCount = categoryList.map { it.categoryCheckedCount }.sum()
                                        logger.info { "checkedCount - $checklistCheckedCount, totalCount = $checklistTotalCount, categoryList - $categoryList" }
                                        ChecklistResponseTO(registryId = registryId, registryItemCount = itemDetails.size.toLong(),
                                            templateId = templateId, categories = categoryList, checklistCheckedCount = checklistCheckedCount, checklistTotalCount = checklistTotalCount)
                                    }
                            }
                    }
            }
    }

    private fun assembleSubcategories(
        categoryMap: HashMap<String, ChecklistCategoryTO>,
        registryDetails: List<ChecklistItemTO>?
    ): Flux<ChecklistCategoryTO> {
        val categoryList = mutableListOf<ChecklistCategoryTO>()
        val itemDetailsMap = hashMapOf<String, List<ChecklistItemTO>>()
        // Form a hashMap with key as nodeId(taxonomyId) and value as ChecklistItemTO(Item details)
        registryDetails?.map {
            if (itemDetailsMap.containsKey(it.nodeId)) {
                val modifiedValue = itemDetailsMap[it.nodeId]?.plus(it)
                itemDetailsMap.put(it.nodeId!!, modifiedValue!!)
            } else it.nodeId?.let { it1 -> itemDetailsMap.put(it1, listOf(it)) }
        }
        // Iterating over the categoryMap and itemDetails map, if the taxonomy matches -> updating the lastUpdatedItem details
        categoryMap.forEach { (_, categoryTO) ->
            categoryTO.subcategories?.forEach {
                val subcategoryIdList = it.subcategoryTaxonomyIds?.split(",")?.map { it.trim() }
                subcategoryIdList?.forEach { subcategoryId ->
                    if (itemDetailsMap.containsKey(subcategoryId)) {
                        itemDetailsMap[subcategoryId]?.forEach { checklistItemDetails ->
                            val lastUpdatedItem = it.lastUpdatedItem
                            if (isLatestItem(checklistItemDetails, lastUpdatedItem)) {
                                it.lastUpdatedItem = ItemDetailsTO(checklistItemDetails.itemDetails?.tcin, checklistItemDetails.itemDetails?.description,
                                    checklistItemDetails.itemDetails?.imageUrl, checklistItemDetails.itemDetails?.alternateImageUrls, checklistItemDetails.itemDetails?.addedTs,
                                    checklistItemDetails.itemDetails?.lastModifiedTs)
                            }
                            it.itemCount += 1
                        }
                        it.checked = true
                    }
                }
                if (it.checked) categoryTO.categoryCheckedCount += 1
            }
            categoryTO.categoryTotalCount = categoryTO.subcategories?.size ?: 0
            categoryList.add(categoryTO)
        }
        return categoryList.toFlux()
    }

    private fun isLatestItem(
        checklistItemDetails: ChecklistItemTO,
        lastUpdatedItem: ItemDetailsTO?
    ): Boolean {
        // Check if the item is the latest updated item by comparing lastModifiedTs or addedTs
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

    private fun markSubcategories(
        registryId: UUID,
        templateId: Int,
        categoryList: List<ChecklistCategoryTO>
    ): Mono<List<ChecklistCategoryTO>> {
        // Check if the checklist is marked manually by the guest
        return checkedSubCategoriesRepository.findByRegistryIdAndTemplateId(registryId, templateId).collectList()
            .map { checklistIdList ->
                categoryList.map {
                    var categoryCheckedCount = 0
                    it.subcategories?.map { subCategory ->
                        if (checklistIdList.map { it.checkedSubcategoriesId.checklistId }.contains(subCategory.checklistId) && !subCategory.checked) {
                            subCategory.checked = true
                            categoryCheckedCount++
                        }
                    }
                    it.categoryCheckedCount += categoryCheckedCount
                }
                categoryList
            }
    }

    private fun getDetailsResponse(
        registryId: UUID,
        guestId: String,
        channel: RegistryChannel,
        subChannel: RegistrySubChannel
    ): Mono<RegistryDetailsResponseTO> {
        return backpackClient.getRegistryDetails(guestId, registryId, storeId, channel, subChannel, false)
            .switchIfEmpty {
                logger.debug("Empty response from get Details API")
                Mono.empty()
            }.onErrorResume {
                logger.error("Error response from get Details API" + it.printStackTrace())
                Mono.empty()
            }
    }

    private fun getItemDetailsFromRedsky(
        registryDetails: List<RegistryItemsBasicInfoTO>
    ): Mono<List<ChecklistItemTO>> {
        // Make call to Redsky to get ItemDetails
        return registryDetails
            .filter { !it.tcin.isNullOrEmpty() }
            .toFlux()
            .flatMap { registryItems ->
                redskyHydrationManager.getRegistryItemTaxonomyDetails(registryItems.tcin!!)
                    .switchIfEmpty {
                        logger.info("Empty respose from redsky for the tcin - ${registryItems.tcin!!}")
                        Mono.empty()
                    }
                    .onErrorResume {
                        logger.error("Error while getting item taxonomy details from redsky - $it")
                        Mono.empty()
                    }
                    .map {
                        ChecklistItemTO(nodeId = it.taxonomy?.category?.nodeId, itemDetails = ItemDetailsTO(it.tcin, it.item?.productDescription?.title, it.item?.enrichment?.images?.primaryImageUrl,
                            it.item?.enrichment?.images?.alternateImageUrls, registryItems?.addedTs, registryItems?.lastModifiedTs))
                    }
            }
            .collectList()
    }
}
