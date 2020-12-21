package com.tgt.backpackregistrychecklists.service

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplatePK
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.transport.Category
import com.tgt.backpackregistrychecklists.transport.Checklist
import com.tgt.backpackregistryclient.util.RegistryType
import com.tgt.lists.common.components.exception.BadRequestException
import com.tgt.lists.common.components.exception.BaseErrorCodes.BAD_REQUEST_ERROR_CODE
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateChecklistTemplateService(
    @Inject val checklistTemplateRepository: ChecklistTemplateRepository
) {
    fun uploadChecklistToDatabase(
        registryType: RegistryType,
        checklist: Checklist,
        templateId: Int,
        checklistName: String
    ): Mono<Void> {
        return deleteDuplicateChecklistIfExists(templateId, checklistName)
            .flatMap {
                // check if the provided templateId can be default for the provided registryType
                checklistTemplateRepository.countByRegistryTypeAndDefaultChecklist(registryType)
                    .map { it == 0L }
            }
            .flatMap { default ->
            checklist.categories!!.toFlux()
                    .flatMap {
                        val checklistTemplate = formChecklistEntity(registryType, it, default, templateId, checklistName)
                        checklistTemplateRepository.save(checklistTemplate)
                    }.collectList()
                .onErrorResume {
                    throw BadRequestException(BAD_REQUEST_ERROR_CODE(listOf("exception while storing the checklistTemplate to database")))
                }
            }
            .then()
    }

    private fun deleteDuplicateChecklistIfExists(templateId: Int, checklistName: String): Mono<Int> {
        return checklistTemplateRepository.findByTemplateId(templateId).take(1)
            .filter {
                // validate if the checklistName exists for the provided templateId
                it.checklistName == checklistName
            }
            .switchIfEmpty<ChecklistTemplate> {
                // verify if the checklistName already exists for any templateId
                checklistTemplateRepository.countByChecklistName(checklistName).map { count ->
                    if (count != 0L)
                        throw BadRequestException(BAD_REQUEST_ERROR_CODE(listOf("The checklist name already exists enter a new checklist name")))
                    else {
                        // publish a dummy checklistTemplate
                        ChecklistTemplate(checklistTemplatePK = ChecklistTemplatePK(templateId, 0), registryType = RegistryType.BABY, checklistName = "")
                    }
                }
            }
            .flatMap {
                // Delete rows which are having the provided templateId
                checklistTemplateRepository.deleteByTemplateId(templateId)
            }.toMono()
    }

    private fun formChecklistEntity(
        registryType: RegistryType,
        category: Category,
        default: Boolean,
        templateId: Int,
        checklistName: String
    ): ChecklistTemplate {
        val checkListTemplatePK = ChecklistTemplatePK(templateId, category.checklistId!!)
        return ChecklistTemplate(
            checklistTemplatePK = checkListTemplatePK,
            registryType = registryType,
            checklistName = checklistName,
            defaultChecklist = default,
            categoryOrder = category.l1DisplayOrder,
            categoryId = category.l1TaxonomyId,
            categoryName = category.l1AliasName,
            categoryImageUr = category.defaultImage,
            subcategoryId = category.l2TaxonomyId,
            subcategoryChildIds = category.l2ChildIds,
            subcategoryName = category.l2AliasName,
            subcategoryOrder = category.l2DisplayOrder,
            subcategoryUrl = category.imageUrl,
            plpParam = category.plpParam,
            createdTs = LocalDate.now(),
            updatedTs = LocalDate.now()
        )
    }
}
