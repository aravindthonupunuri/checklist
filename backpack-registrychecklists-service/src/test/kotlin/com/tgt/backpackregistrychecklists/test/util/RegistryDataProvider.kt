package com.tgt.backpackregistrychecklists.test.util

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tgt.backpackregistryclient.transport.*
import com.tgt.backpackregistryclient.util.*
import com.tgt.lists.lib.api.transport.ListItemResponseTO
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Suppress("UNCHECKED_CAST")
class RegistryDataProvider {

    val mapper = jacksonObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

    fun getRecipient(recipientType: RecipientType, recipientRole: String?, firstName: String?, lastName: String?): RegistryRecipientTO {
        return RegistryRecipientTO(recipientType = recipientType, recipientRole = recipientRole, firstName = firstName, lastName = lastName)
    }

    fun getRegistryEvent(city: String?, state: String?, country: String?, eventDate: LocalDate): RegistryEventTO {
        return RegistryEventTO(city = city, state = state, country = country, eventDate = eventDate)
    }

    fun getBabyExtension(babyName: String?, babyGender: String?, firstChild: Boolean?, showerDate: LocalDate?): RegistryBabyTO {
        return RegistryBabyTO(babyName = babyName, babyGender = babyGender, firstChild = firstChild, showerDate = showerDate)
    }

    fun getRegistryMetaData(
        subChannel: RegistrySubChannel?,
        profileAddressId: String?,
        registryType: RegistryType?,
        giftCardsEnabled: Boolean?,
        groupGiftEnabled: Boolean?,
        groupGiftAmount: String?,
        registryStatus: RegistryStatus?,
        recipients: List<RegistryRecipientTO>?,
        event: RegistryEventTO?,
        babyRegistry: RegistryBabyTO?,
        guestRulesMetaData: RegistryGuestRuleMetaDataTO?,
        imageMetaData: RegistryImageMetaDataTO?
    ): Map<String, Any>? {
        return RegistryMetaDataTO.getRegistryMetadataMap(RegistryMetaDataTO(subChannel, profileAddressId, registryType, giftCardsEnabled,
            groupGiftEnabled, groupGiftAmount, registryStatus, recipients, event, babyRegistry, guestRulesMetaData, imageMetaData))
    }

    fun getGuestRulesMetaData(guestRulesMetaData: RegistryGuestRuleMetaDataTO?): Map<String, Any>? {
        return RegistryMetaDataTO.getRegistryMetadataMap(RegistryMetaDataTO(guestRulesMetaData = guestRulesMetaData))
    }

    fun getMultiDeleteItemRequest(itemIdList: List<UUID>): RegistryItemMultiDeleteRequestTO {
        return RegistryItemMultiDeleteRequestTO(itemIdList)
    }

    fun getRegistryItemUpdateRequest(
        itemType: RegistryItemType,
        itemNote: String?,
        genericItemName: String?,
        externalProductUrl: String?,
        addedByRecipient: Boolean?,
        mostWantedFlag: Boolean?,
        requestedQuantity: Int?,
        purchasedQuantity: Int?
    ): RegistryItemUpdateRequestTO {
        return RegistryItemUpdateRequestTO(
            itemNote = itemNote,
            genericItemName = genericItemName,
            externalProductUrl = externalProductUrl,
            addedByRecipient = addedByRecipient,
            mostWantedFlag = mostWantedFlag,
            requestedQuantity = requestedQuantity,
            purchasedQuantity = purchasedQuantity
        )
    }

    fun getRegistryUpdateRequest(
        registryTitle: String?,
        shortDescription: String?,
        registryType: RegistryType?,
        giftCardsEnabled: Boolean?,
        groupGiftEnabled: Boolean?
    ): RegistryUpdateRequestTO {
        return RegistryUpdateRequestTO(
            registryTitle = registryTitle,
            shortDescription = shortDescription,
            registryType = registryType,
            giftCardsEnabled = giftCardsEnabled,
            groupGiftEnabled = groupGiftEnabled
        )
    }

    fun getCreateGuestRulesRequest(
        guestrules: Map<String, String>
    ): RegistryGuestRuleRequestTO {
        return RegistryGuestRuleRequestTO(guestrules)
    }

    fun getRegistryItemRequest(
        itemType: RegistryItemType,
        tcin: String?,
        genericItemName: String?,
        externalProductUrl: String?,
        externalRetailer: String?,
        externalProductPrice: String?,
        externalProductSize: String?,
        externalProductColor: String?,
        externalProductImageUrl: String?,
        addedByRecipient: Boolean?,
        requestedQuantity: Int = 1,
        purchasedQuantity: Int = 0,
        itemTitle: String?,
        itemNote: String?,
        agentId: String?,
        mostWantedFlag: Boolean? = false
    ): RegistryItemRequestTO {
        return RegistryItemRequestTO(
            itemType = itemType,
            tcin = tcin,
            genericItemName = genericItemName,
            externalProductUrl = externalProductUrl,
            externalRetailer = externalRetailer,
            externalProductPrice = externalProductPrice,
            externalProductSize = externalProductSize,
            externalProductColor = externalProductColor,
            externalProductImageUrl = externalProductImageUrl,
            addedByRecipient = addedByRecipient,
            requestedQuantity = requestedQuantity,
            purchasedQuantity = purchasedQuantity,
            itemTitle = itemTitle,
            itemNote = itemNote,
            agentId = agentId,
            mostWantedFlag = mostWantedFlag
        )
    }

    fun getMultiAddItemRequest(
        items: List<RegistryItemRequestTO>
    ): RegistryItemMultiAddRequestTO {
        return RegistryItemMultiAddRequestTO(items)
    }

    fun getRegistryItem(
        listItemId: UUID,
        tcin: String?,
        channel: RegistryChannel,
        itemTitle: String?,
        itemType: RegistryItemType,
        itemRelationship: String?,
        externalProductSize: String?,
        externalProductColor: String?,
        metadata: Map<String, Any>?
    ): ListItemResponseTO {
        return ListItemResponseTO(listItemId = listItemId, tcin = tcin, itemTitle = itemTitle,
            channel = channel.toString(),
            itemRefId = populateItemRefId(itemType, tcin, itemTitle, externalProductSize, externalProductColor),
            itemType = itemType.toListItemType(), relationshipType = itemRelationship, addedTs = Instant.now().toString(),
            metadata = metadata)
    }

    fun getRegistryResponseTO(
        registryId: UUID,
        registryTitle: String,
        itemsCount: Int,
        registryStatus: RegistryStatus
    ): RegistryResponseTO {
        return RegistryResponseTO(registryId = registryId, channel = RegistryChannel.WEB, listType = "REGISTRY",
            registryTitle = registryTitle, shortDescription = null, agentId = null, addedTs = null, lastModifiedTs = null,
            registryItems = null, itemsCount = itemsCount, subChannel = RegistrySubChannel.KIOSK, profileAddressId = "10",
            registryType = RegistryType.BABY, giftCardsEnabled = true, groupGiftEnabled = false, registryStatus = registryStatus,
            recipients = null, event = null, babyRegistry = null, honeyFundItems = null)
    }

    fun getRegistryItemResponseTO(
        registryItemId: UUID,
        tcin: String,
        requestedQuantity: Int,
        purchasedQuantity: Int
    ): RegistryItemResponseTO {
        return RegistryItemResponseTO(registryItemId = registryItemId, itemType = null, channel = RegistryChannel.WEB,
            subChannel = RegistrySubChannel.KIOSK, tcin = tcin, requestedQuantity = requestedQuantity, purchasedQuantity = purchasedQuantity)
    }

    fun getImageRequest(
        imageInfo: RegistryImageInfoTO?
    ): RegistryImageRequestTO {
        return RegistryImageRequestTO(imageUrl = imageInfo?.imageUrl,
            imageId = imageInfo?.imageId,
            dimension = imageInfo?.dimension,
            type = imageInfo?.type,
            imageStatus = imageInfo?.imageStatus)
    }

    fun getImageMetaData(imageMetaData: RegistryImageMetaDataTO?): Map<String, Any>? {
        return RegistryMetaDataTO.getRegistryMetadataMap(RegistryMetaDataTO(imageMetaData = imageMetaData))
    }

    fun getUTCLocalDateTimeStamp(): String {
        return LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC).toString()
    }

    fun getRegistryCustomUrlRequest(
        customUrl: String
    ): RegistryCustomUrlRequestTO {
        return RegistryCustomUrlRequestTO(customUrl = customUrl)
    }
}
