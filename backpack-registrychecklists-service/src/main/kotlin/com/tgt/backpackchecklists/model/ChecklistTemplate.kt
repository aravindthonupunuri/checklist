package com.tgt.backpackchecklists.model

import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import io.micronaut.data.annotation.MappedEntity
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Table

@MappedEntity
@Table(name = "checklist_template")
data class ChecklistTemplate(
    @EmbeddedId
    val checklistTemplatePK: ChecklistTemplatePK,
    @Column(name = "checklist_name")
    val checklistName: String,
    @Column(name = "default_checklist")
    val defaultChecklist: Boolean? = null,
    @Column(name = "checklist_id")
    val checklistId: Int? = null,
    @Column(name = "category_id")
    val categoryId: String? = null,
    @Column(name = "category_name")
    val categoryName: String? = null,
    @Column(name = "category_image_ur")
    val categoryImageUr: String? = null,
    @Column(name = "subcategory_id")
    val subcategoryId: Int? = null,
    @Column(name = "subcategory_name")
    val subcategoryName: String? = null,
    @Column(name = "subcategory_order")
    val subcategoryOrder: Int? = null,
    @Column(name = "subcategory_url")
    val subcategoryUrl: String? = null,
    @Column(name = "plp_param")
    val plpParam: String? = null,
    @Column(name = "created_ts")
    @DateCreated
    var createdTs: LocalDateTime? = null,
    @Column(name = "updated_ts")
    @DateUpdated
    var updatedTs: LocalDateTime? = null
)
