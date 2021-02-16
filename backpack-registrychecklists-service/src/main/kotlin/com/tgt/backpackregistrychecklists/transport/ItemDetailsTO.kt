package com.tgt.backpackregistrychecklists.transport

import java.time.LocalDateTime

data class ItemDetailsTO(
    val tcin: String?,
    val description: String? = null,
    val imageUrl: String? = null,
    val alternateImageUrls: List<String>? = null,
    val addedTs: LocalDateTime? = null,
    val lastModifiedTs: LocalDateTime? = null
)
