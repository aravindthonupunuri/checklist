package com.tgt.backpackregistrychecklists.transport

import java.time.LocalDate

data class ItemDetailsTO(
    val tcin: String?,
    val description: String? = null,
    val imageUrl: String? = null,
    val addedTs: LocalDate? = null,
    val lastModifiedTs: LocalDate? = null
)
