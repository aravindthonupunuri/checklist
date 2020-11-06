package com.tgt.backpackregistrychecklists.test.util

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tgt.backpackregistryclient.util.RegistryChannel
import com.tgt.lists.cart.transport.*
import com.tgt.lists.lib.api.transport.*
import com.tgt.lists.lib.api.util.Constants
import com.tgt.lists.lib.api.util.ItemType
import com.tgt.lists.lib.api.util.LIST_ITEM_STATE
import com.tgt.lists.lib.api.util.UnitOfMeasure
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Suppress("UNCHECKED_CAST")
class CartDataProvider(val listType: String) {

    val mapper = jacksonObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)

    fun getCartResponse(
        cartId: UUID?,
        guestId: String,
        cartChannel: RegistryChannel?,
        cartType: CartType,
        tenantCartName: String?,
        tenantCartDescription: String,
        agentId: String?,
        metadata: Map<String, Any>
    ): CartResponse {
        return CartResponse(cartId = cartId, cartNumber = "", guestId = guestId, cartChannel = cartChannel?.toString(),
            cartSubchannel = listType,
            cartType = cartType.value, tenantCartName = tenantCartName,
            tenantCartDescription = tenantCartDescription, agentId = agentId, metadata = metadata,
            createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now(),
            abandonAfterDuration = AbandonAfterDuration(BigDecimal.valueOf(730), AbandonAfterDuration.Type.DAYS))
    }

    fun getCartResponse(
        cartId: UUID?,
        guestId: String,
        cartChannel: RegistryChannel?,
        cartSubchannel: String,
        cartType: CartType,
        tenantCartName: String?,
        tenantCartDescription: String,
        agentId: String?,
        metadata: Map<String, Any>
    ): CartResponse {
        return CartResponse(cartId = cartId, cartNumber = "", guestId = guestId, cartChannel = cartChannel?.toString(),
            cartSubchannel = listType,
            cartType = cartType.value, tenantCartName = tenantCartName,
            tenantCartDescription = tenantCartDescription, agentId = agentId, metadata = metadata,
            createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now(),
            abandonAfterDuration = AbandonAfterDuration(BigDecimal.valueOf(730), AbandonAfterDuration.Type.DAYS))
    }

    fun getCartItemResponse(
        cartId: UUID,
        cartItemId: UUID,
        tenantRefId: String,
        tcin: String?,
        itemTitle: String?,
        requestedQuantity: Int = 0,
        itemNote: String?,
        price: Float?,
        listPrice: Float?,
        relationshipType: String?,
        itemState: String?,
        imageBaseUrl: String?,
        primaryImage: String?,
        metadata: Map<String, Any>,
        serialNumber: String?,
        createdAt: LocalDateTime?,
        updatedAt: LocalDateTime?
    ): CartItemResponse {
        return CartItemResponse(cartId = cartId, cartItemId = cartItemId,
            tcin = tcin, tenantItemName = itemTitle, notes = itemNote, requestedQuantity = requestedQuantity, price = price,
            listPrice = listPrice, eligibleDiscounts = getEligibleDiscounts(2), relationshipType = relationshipType, itemState = itemState,
            tenantReferenceId = tenantRefId, images = Image(baseUrl = imageBaseUrl, primaryImage = primaryImage),
            metadata = metadata, serialNumber = serialNumber, createdAt = createdAt, updatedAt = updatedAt, locationId = "1375")
    }

    fun getCartItemDeleteResponse(cartId: UUID, cartItemId: UUID): CartItemDeleteResponse {
        return CartItemDeleteResponse(cartId = cartId, cartItemId = cartItemId)
    }

    fun getDeleteMultiCartItemResponse(cartId: UUID, deletedCartItemIds: List<UUID>, failedCartItemIds: List<UUID>? = null): DeleteMultiCartItemsResponse {
        return DeleteMultiCartItemsResponse(cartId = cartId, deletedCartItemIds = deletedCartItemIds.toTypedArray(), failedCartItemIds = failedCartItemIds?.toTypedArray(), cartContents = null)
    }

    fun getCartResponse(cartId: UUID, guestId: String?, metadata: Map<String, Any>?): CartResponse {
        return CartResponse(cartId = cartId, guestId = guestId, cartSubchannel = listType, cartChannel = " web", tenantCartName = cartId.toString(), metadata = metadata, createdAt = LocalDateTime.now())
    }

    fun getCartResponse(cartId: UUID, guestId: String?, cartNumber: String, metadata: Map<String, Any>?): CartResponse {
        return CartResponse(cartId = cartId, guestId = guestId, cartNumber = cartNumber, cartSubchannel = listType, cartChannel = " web", tenantCartName = cartId.toString(), metadata = metadata)
    }

    fun getCartPutRequest(metadata: Map<String, Any>): CartPutRequest {
        return CartPutRequest(metadata = metadata)
    }

    fun getCartContentsResponse(cartId: UUID, itemCount: Int): CartContentsResponse {
        if (itemCount == 0) {
            return CartContentsResponse()
        }

        val itemList = ArrayList<CartItemResponse>()

        for (i in 1..itemCount) {
            if (i % 2 == 0) {
                itemList.add(CartItemResponse(tenantReferenceId = UUID.randomUUID().toString(), metadata = mutableMapOf<String, Any>("SOME_STATUS" to "PENDING")))
            } else {
                itemList.add(CartItemResponse(tenantReferenceId = UUID.randomUUID().toString(), metadata = mutableMapOf<String, Any>("SOME_STATUS" to "COMPLETED")))
            }
        }

        return CartContentsResponse(cartItems = itemList.toTypedArray())
    }

    fun getCartContentsResponse(cartResponse: CartResponse, cartItemResponses: List<CartItemResponse>? = null): CartContentsResponse {
        return CartContentsResponse(cart = cartResponse, cartItems = cartItemResponses?.toTypedArray())
    }

    fun getEligibleDiscounts(offerCount: Int): Array<EligibleDiscount> {
        val eligibleDiscountList = ArrayList<EligibleDiscount>()
        for (i in 1..offerCount) {
            eligibleDiscountList.add(EligibleDiscount(promotionId = "$i", legalDescription = "LegalDescription$i",
                subscriptionPromoFlag = false, rewardType = "PercentageOff", rewardValue = 15F, appliedPromoText = "15% applied",
                promotionGroup = "Buy and Save", offerText = "15% promo"))
        }

        return eligibleDiscountList.toTypedArray()
    }

    fun getCartDeleteResponse(cartId: UUID): CartDeleteResponse {
        return CartDeleteResponse(cartId = cartId)
    }

    fun getCartDeleteRequest(cartId: UUID, forceDeletion: Boolean?): CartDeleteRequest {
        return CartDeleteRequest(cartId = cartId, forceDeletion = forceDeletion)
    }

    fun getMetaData(listMetadata: ListMetaDataTO, userMetadata: UserMetaDataTO): MutableMap<String, Any> {
        val metadata = mutableMapOf<String, Any>()

        // Push un-mapped list to cart attributes into cart meta data
        val listMetaData = ListMetaDataTO(
            defaultList = listMetadata.defaultList,
            listStatus = listMetadata.listStatus)

        val userData = UserMetaDataTO(
            userMetaData = userMetadata.userMetaData
        )

        metadata[Constants.LIST_METADATA] = mapper.writeValueAsString(listMetaData)
        metadata[Constants.USER_METADATA] = mapper.writeValueAsString(userData)
        return metadata
    }

    fun getListMetaDataFromCart(cartMetadata: Map<String, Any>?): ListMetaDataTO? {
        return mapper.readValue<ListMetaDataTO>((cartMetadata?.get(Constants.LIST_METADATA) as? String).toString())
    }

    fun getUserMetaDataFromCart(cartMetadata: Map<String, Any>?): UserMetaDataTO? {
        return mapper.readValue<UserMetaDataTO>((cartMetadata?.get(Constants.USER_METADATA) as? String).toString())
    }

    fun getItemMetaData(listItemMetadata: ListItemMetaDataTO, userItemMetadata: UserItemMetaDataTO): MutableMap<String, Any> {
        val metadata = mutableMapOf<String, Any>()

        // Push un-mapped list to cart attributes into cart meta data
        val listItemMetaData = ListItemMetaDataTO(
            itemType = listItemMetadata.itemType,
            itemState = listItemMetadata.itemState
        )

        val userData = UserMetaDataTO(
            userMetaData = userItemMetadata.userMetaData
        )

        metadata[Constants.LIST_ITEM_METADATA] = mapper.writeValueAsString(listItemMetaData)
        metadata[Constants.USER_ITEM_METADATA] = mapper.writeValueAsString(userData)
        return metadata
    }

    fun getListItemMetaDataFromCart(cartMetadata: Map<String, Any>?): ListItemMetaDataTO? {
        return mapper.readValue<ListItemMetaDataTO>((cartMetadata?.get(Constants.LIST_ITEM_METADATA) as? String).toString())
    }

    fun getItemUserMetaDataFromCart(cartMetadata: Map<String, Any>?): UserItemMetaDataTO? {
        return mapper.readValue<UserItemMetaDataTO>((cartMetadata?.get(Constants.USER_ITEM_METADATA) as? String).toString())
    }

    fun getListItemUpdateRequest(itemState: LIST_ITEM_STATE): ListItemUpdateRequestTO {
        return ListItemUpdateRequestTO(null, null, null, itemState, null)
    }

    fun getDeleteMultiCartItemsResponse(cartId: UUID, deletedCartItemIds: List<UUID>, failedCartItemIds: List<UUID>): DeleteMultiCartItemsResponse {
        return DeleteMultiCartItemsResponse(cartId = cartId, deletedCartItemIds = deletedCartItemIds.toTypedArray(),
            failedCartItemIds = failedCartItemIds.toTypedArray())
    }

    fun getListItemRequestTO(itemType: ItemType, tcin: String): ListItemRequestTO {
        return ListItemRequestTO(itemType = itemType, itemRefId = tcin, tcin = tcin, itemTitle = null)
    }

    fun jsonToCartPostRequest(json: String): CartPostRequest {
        return mapper.readValue<CartPostRequest>(json)
    }

    fun getListResponse(
        listId: UUID,
        channel: String?,
        listType: String?,
        listTitle: String?,
        metadata: Map<String, Any>?
    ): ListResponseTO {
        return ListResponseTO(
            listId = listId,
            channel = channel,
            listType = listType,
            listTitle = listTitle,
            shortDescription = "shortDescription",
            agentId = "1234",
            defaultList = false,
            metadata = metadata,
            pendingListItems = null,
            completedListItems = null,
            addedTs = null,
            lastModifiedTs = null,
            maxCompletedItemsCount = 0,
            maxPendingItemsCount = 100,
            maxCompletedPageCount = null,
            maxPendingPageCount = null
        )
    }

    fun getAllListResponse(
        listId: UUID?,
        channel: String?
    ): ListGetAllResponseTO {
        return ListGetAllResponseTO(
            listId = listId,
            channel = channel,
            completedListId = null,
            listType = null,
            listTitle = null,
            defaultList = false,
            shortDescription = "shortdescription",
            agentId = "agentId",
            metadata = null,
            addedTs = null,
            lastModifiedTs = null,
            maxListsCount = 0,
            pendingItemsCount = 0,
            completedItemsCount = 0,
            totalItemsCount = 0,
            pendingItems = null,
            compeletedItems = null
        )
    }

    fun getListItemResponse(
        listItemId: UUID?,
        itemRefId: String,
        tcin: String?,
        itemNote: String?,
        requestedQuantity: Int?
    ): ListItemResponseTO {
        return ListItemResponseTO(
            listItemId = listItemId,
            itemType = ItemType.TCIN,
            itemRefId = itemRefId,
            channel = RegistryChannel.WEB.toString(),
            tcin = tcin,
            itemTitle = null,
            itemNote = itemNote,
            requestedQuantity = requestedQuantity,
            unitOfMeasure = UnitOfMeasure.EACHES,
            metadata = null,
            price = null,
            listPrice = null,
            offerCount = 0,
            images = null,
            relationshipType = null,
            itemState = null,
            addedTs = null,
            lastModifiedTs = null
        )
    }

    fun getListResponse(
        listId: UUID,
        channel: String?,
        listType: String?,
        listTitle: String?,
        metadata: Map<String, Any>?,
        items: List<ListItemResponseTO>?
    ): ListResponseTO {
        return ListResponseTO(
            listId = listId,
            channel = channel,
            listType = listType,
            listTitle = listTitle,
            shortDescription = "shortDescription",
            agentId = "1234",
            defaultList = false,
            metadata = metadata,
            pendingListItems = items,
            completedListItems = null,
            addedTs = null,
            lastModifiedTs = null,
            maxCompletedItemsCount = 0,
            maxPendingItemsCount = 100,
            maxCompletedPageCount = null,
            maxPendingPageCount = null
        )
    }
}
