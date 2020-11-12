package com.tgt.backpackregistrychecklists.domain.model

import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.MappedProperty
import io.micronaut.data.model.DataType
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Id
import javax.persistence.Table

@MappedEntity
@Table(name = "registry_checklist")
data class RegistryChecklist(
    @Id
    @Column(name = "registry_id")
    @MappedProperty(type = DataType.OBJECT)
    val registryId: UUID,

    @Column(name = "template_id")
    val templateId: Int,

    @Column(name = "CREATED_TS")
    @DateCreated
    val createdTs: LocalDateTime? = null,

    @Column(name = "CREATED_USER")
    val createdUser: String? = null,

    @Column(name = "UPDATED_TS")
    @DateUpdated
    val updatedTs: LocalDateTime? = null,

    @Column(name = "UPDATED_USER")
    val updatedUser: String? = null
)
