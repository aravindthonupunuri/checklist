package com.tgt.backpackregistrychecklists.transport

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Category(
    @JsonProperty("checklist_id")
    val checklistId: Int? = null,
    @JsonProperty("l1_taxonomy_id")
    val l1TaxonomyId: String? = null,
    @JsonProperty("l1_alias_name")
    val l1AliasName: String? = null,
    @JsonProperty("l1_display_order")
    val l1DisplayOrder: Int? = null,
    @JsonProperty("l2_taxonomy_id")
    val l2TaxonomyId: String? = null,
    @JsonProperty("l2_child_ids")
    val l2ChildIds: String? = null,
    @JsonProperty("l2_taxonomy_url")
    val l2TaxonomyUrl: String? = null,
    @JsonProperty("plp_param")
    val plpParam: String? = null,
    @JsonProperty("l2_alias_name")
    val l2AliasName: String? = null,
    @JsonProperty("l2_display_order")
    val l2DisplayOrder: Int? = null,
    @JsonProperty("suggested_item_count")
    val suggestedItemCount: Int? = null,
    @JsonProperty("default_image")
    val defaultImage: String? = null,
    @JsonProperty("image_url")
    val imageUrl: String? = null
)
