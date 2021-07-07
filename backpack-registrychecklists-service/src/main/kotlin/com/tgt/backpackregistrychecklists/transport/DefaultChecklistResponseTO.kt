package com.tgt.backpackregistrychecklists.transport

import com.tgt.backpackregistryclient.util.RegistryType

data class DefaultChecklistResponseTO(
    val registryType: RegistryType,
    val defaultTemplateId: Int? = null,
    val categories: List<ChecklistCategoryTO>? = null,
    val checklistTotalCount: Int = 0
)
