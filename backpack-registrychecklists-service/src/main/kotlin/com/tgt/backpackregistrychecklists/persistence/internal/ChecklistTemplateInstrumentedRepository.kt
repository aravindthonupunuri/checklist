package com.tgt.backpackregistrychecklists.persistence.internal

import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.lists.micronaut.persistence.instrumentation.InstrumentedRepository
import io.micronaut.context.annotation.Primary

@Primary // make it primary to instrument ChecklistTemplateCrudRepository
@InstrumentedRepository("ChecklistTemplateCrudRepository")
interface ChecklistTemplateInstrumentedRepository : ChecklistTemplateRepository
