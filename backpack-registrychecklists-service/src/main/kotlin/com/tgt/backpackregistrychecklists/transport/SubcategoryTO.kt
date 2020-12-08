package com.tgt.backpackregistrychecklists.transport

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate

data class SubcategoryTO(
    val checklistId: Int?,
    val subcategoryChildIds: String?,
    val subcategoryName: String?,
    val subcategoryDisplayOrder: Int?,
    val subcategoryUrl: String?,
    val plpParam: String?,
    var itemCount: Long?,
    var checked: Boolean?,
    var lastUpdatedItem: ItemDetailsTO? = null
) {
    constructor(
        checklistTemplate: ChecklistTemplate
    ) :
        this(
            checklistId = checklistTemplate.checklistTemplatePK.checklistId, subcategoryChildIds = checklistTemplate.subcategoryChildIds, subcategoryName = checklistTemplate.subcategoryName,
            subcategoryDisplayOrder = checklistTemplate.subcategoryOrder, subcategoryUrl = checklistTemplate.subcategoryUrl, checked = false, plpParam = checklistTemplate.plpParam, itemCount = 0
        )
}
