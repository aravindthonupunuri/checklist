package com.tgt.backpackregistrychecklists.transport

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate

data class ChecklistCategoryTO(
    val categoryId: String?,
    val categoryName: String?,
    val categoryDisplayOrder: Int?,
    val categoryImageUrl: String?,
    var subcategories: List<SubcategoryTO>?,
    var categoryCheckedCount: Int = 0,
    var categoryTotalCount: Int = 0
) {
    constructor(
        checklistTemplate: ChecklistTemplate,
        subCategories: List<SubcategoryTO>?
    ) :
        this(
            categoryId = checklistTemplate.categoryId,
            categoryDisplayOrder = checklistTemplate.categoryOrder, categoryImageUrl = checklistTemplate.categoryImageUr,
            categoryName = checklistTemplate.categoryName, subcategories = subCategories
        )
}
