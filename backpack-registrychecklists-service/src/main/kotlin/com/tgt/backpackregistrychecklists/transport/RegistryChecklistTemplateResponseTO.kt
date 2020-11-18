package com.tgt.backpackregistrychecklists.transport

import com.tgt.backpackregistryclient.util.RegistryType

data class RegistryChecklistTemplateResponseTO(
    val registryType: RegistryType,
    val checklists: List<ChecklistTemplateTO>? = null
)
