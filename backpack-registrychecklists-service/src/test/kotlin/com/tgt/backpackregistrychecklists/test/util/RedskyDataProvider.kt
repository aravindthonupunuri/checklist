package com.tgt.backpackregistrychecklists.test.util

import com.tgt.backpackregistryclient.transport.redsky.getchecklistitem.*

class RedskyDataProvider {
    fun getChecklistItemDetails(tcin: String, nodeId: String): ChecklistItemDetailsVO {
        return ChecklistItemDetailsVO(ChecklistItemDetails(tcin, ItemVO(ProductDescriptionVO("Item Title"), Enrichment(ImageVO("primary.image.url"))),
            TaxonomyVO(Category(nodeId))))
    }
}
