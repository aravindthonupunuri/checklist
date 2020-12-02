package com.tgt.backpackregistrychecklists.transport

import java.time.LocalDateTime

data class ItemDetailsTO(
    val tcin: String?,
    val description: String? = null,
    val imageUrl: String?,
    val addedTs: LocalDateTime?,
    val lastModifiedTs: LocalDateTime?
)
