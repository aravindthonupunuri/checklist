package com.tgt.backpackregistrychecklists.domain.model

import io.micronaut.data.annotation.Embeddable
import io.micronaut.data.annotation.MappedProperty

@Embeddable
data class ChecklistTemplatePK(
    @MappedProperty(value = "template_id")
    val templateId: Int,
    @MappedProperty(value = "checklist_id")
    val checklistId: Int
)
