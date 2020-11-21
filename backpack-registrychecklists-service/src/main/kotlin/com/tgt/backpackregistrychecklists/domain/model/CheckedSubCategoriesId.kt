package com.tgt.backpackregistrychecklists.domain.model

import io.micronaut.data.annotation.MappedProperty
import io.micronaut.data.model.DataType
import java.util.*
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class CheckedSubCategoriesId(
    @Column(name = "registry_id")
    @MappedProperty(type = DataType.OBJECT)
    val registryId: UUID,

    @Column(name = "template_id")
    val templateId: Int,

    @Column(name = "checklist_id")
    val checklistId: Int
)
