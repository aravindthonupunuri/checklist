package com.tgt.backpackregistrychecklists.domain.model

import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import io.micronaut.data.annotation.MappedEntity
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Table

@MappedEntity
@Table(name = "checked_subcategories")
data class CheckedSubCategories(

    @EmbeddedId
    val checkedSubcategoriesId: CheckedSubCategoriesId,

    @Column(name = "created_ts")
    @DateCreated
    var createdTs: LocalDateTime? = null,

    @Column(name = "created_user")
    val createdUser: String? = null,

    @Column(name = "updated_ts")
    @DateUpdated
    var updatedTs: LocalDateTime? = null,

    @Column(name = "updated_user")
    val updatedUser: String? = null
)
