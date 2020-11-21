package com.tgt.backpackregistrychecklists.transport

import javax.validation.constraints.NotEmpty

data class RegistryChecklistRequestTO(
    @field:NotEmpty(message = "templateId must not be empty") val templateId: Int
)
