package com.tgt.backpackregistrychecklists.transport

import java.util.*

data class ChecklistResponseTO(
    val registryId: UUID,
    val registryItemCount: Long?,
    val categories: List<ChecklistCategoryTO>?
)
