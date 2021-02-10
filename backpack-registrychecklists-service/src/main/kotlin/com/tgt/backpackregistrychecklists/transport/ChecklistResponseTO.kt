package com.tgt.backpackregistrychecklists.transport

import java.util.*

data class ChecklistResponseTO(
    val registryId: UUID,
    val registryItemCount: Long?,
    val templateId: Int?,
    val categories: List<ChecklistCategoryTO>,
    val checklistCheckedCount: Int = 0,
    val checklistTotalCount: Int = 0
)
