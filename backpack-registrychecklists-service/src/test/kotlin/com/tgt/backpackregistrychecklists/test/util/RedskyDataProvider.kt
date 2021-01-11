package com.tgt.backpackregistrychecklists.test.util

import com.tgt.backpackregistryclient.transport.redsky.getitemtaxonomy.*

class RedskyDataProvider {
    fun getChecklistItemDetails(tcin: String, nodeId: String): ItemAndTaxonomyDetailsVO {
        return ItemAndTaxonomyDetailsVO(ItemTaxonomyDetails(tcin, ItemVO(ProductDescriptionVO("Item Title"), Enrichment(ImageVO("primary.image.url"))),
            TaxonomyVO(Category(nodeId = nodeId))))
    }
}
