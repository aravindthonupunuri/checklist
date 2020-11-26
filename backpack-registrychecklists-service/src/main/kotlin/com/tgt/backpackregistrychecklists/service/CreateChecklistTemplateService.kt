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
import java.time.LocalDateTime
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
                        val checklistTemplate = formChecklistDTO(registryType, it, default, templateId, checklistName)
                        checklistTemplateRepository.save(checklistTemplate)
                    }.collectList()
            }
            .onErrorResume {
                throw BadRequestException(BAD_REQUEST_ERROR_CODE(listOf(it.stackTrace.toString())))
            }
            .then()
    }

    fun checkIfRegistryTypeIsDefault(registryType: RegistryType): Mono<Boolean> {
        return checklistTemplateRepository.countByRegistryType(registryType).map {
            it == 0L
        }
    }

    fun deleteDuplicateChecklistIfExists(templateId: Int, checklistName: String): Mono<Boolean> {
        return checklistTemplateRepository.findByTemplateId(templateId).collectList().map {
            val checklistNameList = it.map { checklistTemplate ->
                checklistTemplate.checklistName
            }
            checklistNameList.contains(checklistName)
        }.map {
            if (it == true) {
                checklistTemplateRepository.deleteByTemplateId(templateId)
                return@map true
            }
            checkIfChecklistNameIsUnique(checklistName)
                if (checklistTemplateRepository.countByTemplateId(templateId).block() != 0L)
                    checklistTemplateRepository.deleteByTemplateId(templateId)
            true
        }
    }

    fun checkIfChecklistNameIsUnique(checklistName: String) {
        if (checklistTemplateRepository.countByChecklistName(checklistName).block() != 0L)
            throw BadRequestException(BAD_REQUEST_ERROR_CODE(listOf("The checklist name already exists enter a new checklist name")))
    }

    fun formChecklistDTO(
        registryType: RegistryType,
        category: Category,
        default: Boolean,
        templateId: Int,
        checklistName: String
    ): ChecklistTemplate {
        val checkListTemplatePK = ChecklistTemplatePK(registryType, templateId, category.l1DisplayOrder!!)
        return ChecklistTemplate(
            checklistTemplatePK = checkListTemplatePK,
            checklistName = checklistName,
            defaultChecklist = true,
            checklistId = category.checklistId,
            categoryId = category.l1TaxonomyId,
            categoryName = category.l1AliasName,
            categoryImageUr = category.defaultImage,
            subcategoryId = category.l2TaxonomyId,
            subcategoryName = category.l2AliasName,
            subcategoryOrder = category.l2DisplayOrder,
            subcategoryUrl = category.imageUrl,
            plpParam = category.plpParam,
            createdTs = LocalDateTime.now(),
            updatedTs = LocalDateTime.now()
        )
    }
}
