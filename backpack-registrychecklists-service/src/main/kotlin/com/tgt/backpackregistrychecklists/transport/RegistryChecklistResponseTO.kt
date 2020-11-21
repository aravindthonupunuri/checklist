package com.tgt.backpackregistrychecklists.transport

import java.util.*

data class RegistryChecklistResponseTO(
    val registryId: UUID,
    val isChecked: Boolean,
    val checklistId: Int,
    val templateId: Int
)
