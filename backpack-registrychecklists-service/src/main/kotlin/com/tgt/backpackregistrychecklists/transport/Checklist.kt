package com.tgt.backpackregistrychecklists.transport

import com.fasterxml.jackson.annotation.JsonProperty

data class Checklist(
    @JsonProperty("Categories")
    val categories: List<Category>? = null
)
