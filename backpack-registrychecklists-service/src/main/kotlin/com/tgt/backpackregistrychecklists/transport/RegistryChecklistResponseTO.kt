package com.tgt.backpackregistrychecklists.transport

import java.util.*

data class RegistryChecklistResponseTO(
    val registryId: UUID,
    val checked: Boolean,
    val checklistId: Int,
    val templateId: Int
)
