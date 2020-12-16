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
import reactor.kotlin.core.publisher.toFlux
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
            .flatMap { checkIfRegistryTypeIsDefault(registryType) }
            .flatMap { default ->
            checklist.categories!!.toFlux()
                    .flatMap {
                        val checklistTemplate = formChecklistEntity(registryType, it, default, templateId, checklistName)
                        checklistTemplateRepository.save(checklistTemplate)
                    }.collectList()
            }
            .onErrorResume {
                throw BadRequestException(BAD_REQUEST_ERROR_CODE(listOf(it.stackTrace.toString())))
            }
            .then()
    }

    private fun checkIfRegistryTypeIsDefault(registryType: RegistryType): Mono<Boolean> {
        return checklistTemplateRepository.countByRegistryType(registryType).map {
            it == 0L
        }
    }

    private fun deleteDuplicateChecklistIfExists(templateId: Int, checklistName: String): Mono<Int> {
        return checklistTemplateRepository.findByTemplateId(templateId).collectList().map {
            val checklistNameList = it.map { checklistTemplate ->
                checklistTemplate.checklistName
            }
            checklistNameList.contains(checklistName)
        }.flatMap {
            if (it == true) {
                checklistTemplateRepository.deleteByTemplateId(templateId)
            } else {
                checkIfChecklistNameIsUnique(checklistName).flatMap {
                    checklistTemplateRepository.countByTemplateId(templateId).flatMap { count ->
                        if (count != 0L)
                            checklistTemplateRepository.deleteByTemplateId(templateId)
                        else Mono.just(0)
                    }
                }
            }
        }
    }

    private fun checkIfChecklistNameIsUnique(checklistName: String): Mono<Unit> {
        return checklistTemplateRepository.countByChecklistName(checklistName).map {
            if (it != 0L)
                throw BadRequestException(BAD_REQUEST_ERROR_CODE(listOf("The checklist name already exists enter a new checklist name")))
        }
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
            defaultChecklist = true,
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
