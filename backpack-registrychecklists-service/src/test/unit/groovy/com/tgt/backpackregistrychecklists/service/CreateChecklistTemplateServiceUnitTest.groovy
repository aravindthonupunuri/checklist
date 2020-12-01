package com.tgt.backpackregistrychecklists.service

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplatePK
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.transport.Category
import com.tgt.backpackregistrychecklists.transport.Checklist
import com.tgt.backpackregistryclient.util.RegistryType
import com.tgt.lists.common.components.exception.ErrorCode
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.LocalDateTime

class CreateChecklistTemplateServiceUnitTest extends Specification{
    ChecklistTemplateRepository checklistTemplateRepository
    CreateChecklistTemplateService createChecklistTemplateService

    def setup() {
        checklistTemplateRepository = Mock(ChecklistTemplateRepository)
        createChecklistTemplateService = new CreateChecklistTemplateService(checklistTemplateRepository)
    }

    def "Test uploadChecklistToDatabase() integrity"() {
        given:
        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK( 1, 101)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, RegistryType.BABY, "checklistName", true, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "subCategoryName", "subcategory_child_ids", 1, "subCategoryUrl", "plpParam", LocalDateTime.now(), LocalDateTime.now())

        Category category = new Category(1, "l1TaxonomyId", "l1AliasName", 1, "l2TaxonomyId", "l2ChildIds", "l2TaxonomyUrl", "plpParm", "l2AliasName",
            1, 0, "defaultImage", "imageUrl")
        Category category1 = new Category(2, "l1TaxonomyId1", "l1AliasName1", 2, "l2TaxonomyId", "l2ChildIds", "l2TaxonomyUrl", "plpParm", "l2AliasName",
            1, 0, "defaultImage", "imageUrl")
        List<Category> categories = new ArrayList<Category>()
        categories.add(category)
        categories.add(category1)
        Checklist checklist = new Checklist(categories)
        when:
        def result = createChecklistTemplateService.uploadChecklistToDatabase(RegistryType.BABY, checklist, 1, "checklistName").block()

        then:
        1 * checklistTemplateRepository.countByRegistryType(RegistryType.BABY) >> Mono.just(0L)
        1 * checklistTemplateRepository.findByTemplateId(1) >> Flux.empty()
        1 * checklistTemplateRepository.countByTemplateId(1) >> Mono.just(0L)
        1 * checklistTemplateRepository.countByChecklistName("checklistName") >> Mono.just(0L)
        2 * checklistTemplateRepository.save(_) >> Mono.just(checklistTemplate)
    }

    def "Test uploadChecklistToDatabase() - replace an existing checklist for a given templateId and checklistName"() {
        given:
        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK( 1, 101)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, RegistryType.BABY, "name", true, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "subCategoryName", "subcategory_child_ids", 1, "subCategoryUrl", "plpParam", LocalDateTime.now(), LocalDateTime.now())

        Category category = new Category(1, "l1TaxonomyId", "l1AliasName", 1, "l2TaxonomyId", "l2ChildIds", "l2TaxonomyUrl", "plpParm", "l2AliasName",
            1, 0, "defaultImage", "imageUrl")
        Category category1 = new Category(2, "l1TaxonomyId1", "l1AliasName1", 2, "l2TaxonomyId", "l2ChildIds", "l2TaxonomyUrl", "plpParm", "l2AliasName",
            1, 0, "defaultImage", "imageUrl")
        List<Category> categories = new ArrayList<Category>()
        categories.add(category)
        categories.add(category1)
        Checklist checklist = new Checklist(categories)
        ErrorCode errorCode = new ErrorCode(404, "Not Found", ["not found"])
        when:
        def result = createChecklistTemplateService.uploadChecklistToDatabase(RegistryType.BABY, checklist, 1, "checklistName").block()

        then:
        1 * checklistTemplateRepository.countByRegistryType(RegistryType.BABY) >> Mono.just(0L)
        1 * checklistTemplateRepository.deleteByTemplateId(1) >> Mono.just(1)
        1 * checklistTemplateRepository.findByTemplateId(1) >> Flux.just(checklistTemplate)
        1 * checklistTemplateRepository.countByTemplateId(1) >> Mono.just(1L)
        1 * checklistTemplateRepository.countByChecklistName("checklistName") >> Mono.just(0L)
        2 * checklistTemplateRepository.save(_) >> Mono.just(checklistTemplate)
    }

    def "Test uploadChecklistToDatabase() - checklist template table already have a default checklist"() {
        given:
        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK( 1, 101)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, RegistryType.BABY, "name", false, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "subCategoryName", "subcategory_child_ids", 1, "subCategoryUrl", "plpParam", LocalDateTime.now(), LocalDateTime.now())

        Category category = new Category(1, "l1TaxonomyId", "l1AliasName", 1, "l2TaxonomyId", "l2ChildIds", "l2TaxonomyUrl", "plpParm", "l2AliasName",
            1, 0, "defaultImage", "imageUrl")
        Category category1 = new Category(2, "l1TaxonomyId1", "l1AliasName1", 2, "l2TaxonomyId", "l2ChildIds", "l2TaxonomyUrl", "plpParm", "l2AliasName",
            1, 0, "defaultImage", "imageUrl")
        List<Category> categories = new ArrayList<Category>()
        categories.add(category)
        categories.add(category1)
        Checklist checklist = new Checklist(categories)
        when:
        def result = createChecklistTemplateService.uploadChecklistToDatabase(RegistryType.BABY, checklist, 1, "checklistName").block()

        then:
        1 * checklistTemplateRepository.countByRegistryType(RegistryType.BABY) >> Mono.just(1L)
        1 * checklistTemplateRepository.findByTemplateId(1) >> Flux.empty()
        1 * checklistTemplateRepository.countByTemplateId(1) >> Mono.just(0L)
        1 * checklistTemplateRepository.countByChecklistName("checklistName") >> Mono.just(0L)
        2 * checklistTemplateRepository.save(_) >> Mono.just(checklistTemplate)
    }

    def "Test uploadChecklistToDatabase() - checklist name already exists for the given template id"() {
        given:
        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK( 1, 101)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, RegistryType.BABY, "checklistName", true, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "subCategoryName", "subcategory_child_ids", 1, "subCategoryUrl", "plpParam", LocalDateTime.now(), LocalDateTime.now())

        Category category = new Category(1, "l1TaxonomyId", "l1AliasName", 1, "l2TaxonomyId", "l2ChildIds", "l2TaxonomyUrl", "plpParm", "l2AliasName",
            1, 0, "defaultImage", "imageUrl")
        Category category1 = new Category(2, "l1TaxonomyId1", "l1AliasName1", 2, "l2TaxonomyId", "l2ChildIds", "l2TaxonomyUrl", "plpParm", "l2AliasName",
            1, 0, "defaultImage", "imageUrl")
        List<Category> categories = new ArrayList<Category>()
        categories.add(category)
        categories.add(category1)
        Checklist checklist = new Checklist(categories)
        when:
        def result = createChecklistTemplateService.uploadChecklistToDatabase(RegistryType.BABY, checklist, 1, "checklistName").block()

        then:
        1 * checklistTemplateRepository.countByRegistryType(RegistryType.BABY) >> Mono.just(0L)
        1 * checklistTemplateRepository.deleteByTemplateId(1) >> Mono.just(1)
        1 * checklistTemplateRepository.findByTemplateId(1) >> Flux.just(checklistTemplate)
        0 * checklistTemplateRepository.countByTemplateId(1) >> Mono.just(1L)
        0 * checklistTemplateRepository.countByChecklistName("checklistName") >> Mono.just(0L)
        2 * checklistTemplateRepository.save(_) >> Mono.just(checklistTemplate)
    }

    def "Test uploadChecklistToDatabase() - checklist name already exists for a different templateId"() {
        given:
        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK( 1, 101)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, RegistryType.BABY, "name", true, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "subCategoryName", "subcategory_child_ids", 1, "subCategoryUrl", "plpParam", LocalDateTime.now(), LocalDateTime.now())

        Category category = new Category(1, "l1TaxonomyId", "l1AliasName", 1, "l2TaxonomyId", "l2ChildIds", "l2TaxonomyUrl", "plpParm", "l2AliasName",
            1, 0, "defaultImage", "imageUrl")
        Category category1 = new Category(2, "l1TaxonomyId1", "l1AliasName1", 2, "l2TaxonomyId", "l2ChildIds", "l2TaxonomyUrl", "plpParm", "l2AliasName",
            1, 0, "defaultImage", "imageUrl")
        List<Category> categories = new ArrayList<Category>()
        categories.add(category)
        categories.add(category1)
        Checklist checklist = new Checklist(categories)
        when:
        def result = createChecklistTemplateService.uploadChecklistToDatabase(RegistryType.BABY, checklist, 1, "checklistName").block()

        then:
        0 * checklistTemplateRepository.countByRegistryType(RegistryType.BABY) >> Mono.just(0L)
        0 * checklistTemplateRepository.deleteByTemplateId(1) >> Mono.just(1)
        1 * checklistTemplateRepository.findByTemplateId(1) >> Flux.just(checklistTemplate)
        0 * checklistTemplateRepository.countByTemplateId(1) >> Mono.just(1L)
        1 * checklistTemplateRepository.countByChecklistName("checklistName") >> Mono.just(1L)
        0 * checklistTemplateRepository.save(_) >> Mono.just(checklistTemplate)
        thrown(com.tgt.lists.common.components.exception.BadRequestException)
    }

    def "Test uploadChecklistToDatabase() - error while saving the checklist"() {
        given:
        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK( 1, 101)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, RegistryType.BABY, "name", true, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "subCategoryName", "subcategory_child_ids", 1, "subCategoryUrl", "plpParam", LocalDateTime.now(), LocalDateTime.now())

        Category category = new Category(1, "l1TaxonomyId", "l1AliasName", 1, "l2TaxonomyId", "l2ChildIds", "l2TaxonomyUrl", "plpParm", "l2AliasName",
            1, 0, "defaultImage", "imageUrl")
        Category category1 = new Category(2, "l1TaxonomyId1", "l1AliasName1", 2, "l2TaxonomyId", "l2ChildIds", "l2TaxonomyUrl", "plpParm", "l2AliasName",
            1, 0, "defaultImage", "imageUrl")
        List<Category> categories = new ArrayList<Category>()
        categories.add(category)
        categories.add(category1)
        Checklist checklist = new Checklist(categories)
        ErrorCode errorCode = new ErrorCode(404, "Not Found", ["not found"])
        when:
        def result = createChecklistTemplateService.uploadChecklistToDatabase(RegistryType.BABY, checklist, 1, "checklistName").block()

        then:
        1 * checklistTemplateRepository.countByRegistryType(RegistryType.BABY) >> Mono.just(1L)
        1 * checklistTemplateRepository.findByTemplateId(1) >> Flux.empty()
        1 * checklistTemplateRepository.countByTemplateId(1) >> Mono.just(0L)
        1 * checklistTemplateRepository.countByChecklistName("checklistName") >> Mono.just(0L)
        1 * checklistTemplateRepository.save(_) >> Mono.error(new com.tgt.lists.common.components.exception.BadRequestException(errorCode, null))
        thrown(com.tgt.lists.common.components.exception.BadRequestException)
    }
}
