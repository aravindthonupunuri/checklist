package com.tgt.backpackregistrychecklists.domain.model

import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.data.annotation.Embeddable
import io.micronaut.data.annotation.MappedProperty

@Embeddable
data class ChecklistTemplatePK(
    @MappedProperty(value = "registry_type")
    val registryType: RegistryType,
    @MappedProperty(value = "template_id")
    val templateId: Int,
    @MappedProperty(value = "category_order")
    val categoryOrder: Int
)
