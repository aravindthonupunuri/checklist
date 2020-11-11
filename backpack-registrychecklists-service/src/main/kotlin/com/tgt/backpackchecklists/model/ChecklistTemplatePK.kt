package com.tgt.backpackchecklists.model

import io.micronaut.data.annotation.Embeddable
import io.micronaut.data.annotation.MappedProperty

@Embeddable
data class ChecklistTemplatePK(
    @MappedProperty(value = "registry_type")
    val registryType: String,
    @MappedProperty(value = "template_id")
    val templateId: Int,
    @MappedProperty(value = "category_order")
    val categoryOrder: Int
)
