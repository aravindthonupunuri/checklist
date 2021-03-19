package com.tgt.backpackregistrychecklists.transport

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate

data class SubcategoryTO(
    val checklistId: Int?,
    val subcategoryTaxonomyIds: String?,
    val subcategoryName: String?,
    val subcategoryDisplayOrder: Int?,
    val subcategoryImageUrl: String?,
    val plpParam: String?,
    var itemCount: Long = 0,
    var checked: Boolean = false,
    var lastUpdatedItem: ItemDetailsTO? = null
) {
    constructor(
        checklistTemplate: ChecklistTemplate
    ) :
        this(
            checklistId = checklistTemplate.checklistTemplatePK.checklistId, subcategoryTaxonomyIds = checklistTemplate.subcategoryChildIds, subcategoryName = checklistTemplate.subcategoryName,
            subcategoryDisplayOrder = checklistTemplate.subcategoryOrder, subcategoryImageUrl = checklistTemplate.subcategoryUrl, checked = false, plpParam = checklistTemplate.plpParam, itemCount = 0
        )
}
