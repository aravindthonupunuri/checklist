package com.tgt.backpackregistrychecklists.api.controller

import com.tgt.backpackregistrychecklists.service.GetChecklistTemplatesService
import com.tgt.backpackregistrychecklists.service.UnmarkChecklistService
import com.tgt.backpackregistrychecklists.transport.RegistryChecklistResponseTO
import com.tgt.backpackregistrychecklists.transport.RegistryChecklistTemplateResponseTO
import com.tgt.backpackregistryclient.util.RegistryChannel
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import reactor.core.publisher.Mono
import java.util.*

@Controller("/registry_checklists/v1")
class RegistryChecklistController(
    private val getCheckListTemplatesService: GetChecklistTemplatesService,
    private val unmarkChecklistService: UnmarkChecklistService
) {

    @Get("/checklists")
    @Status(HttpStatus.OK)
    @ApiResponse(
        content = [Content(mediaType = "application/json", schema = Schema(implementation = RegistryChecklistTemplateResponseTO::class))]
    )
    fun getAllChecklistTemplates(
        @Header("profile_id") guestId: String,
        @QueryValue("registry_type") registryType: RegistryType,
        @QueryValue("location_id") locationId: Long?,
        @QueryValue("channel") channel: RegistryChannel,
        @QueryValue("sub_channel") subChannel: RegistrySubChannel
    ): Mono<RegistryChecklistTemplateResponseTO> {
        return getCheckListTemplatesService.getTemplatesForRegistryType(registryType)
    }

    @Delete("/{registry_id}/checklists/{checklist_id}")
    @Status(HttpStatus.OK)
    @ApiResponse(
        content = [Content(mediaType = "application/json", schema = Schema(implementation = RegistryChecklistResponseTO::class))]
    )
    fun unmarkChecklist(
        @Header("profile_id") guestId: String,
        @PathVariable("registry_id") registryId: UUID,
        @PathVariable("checklist_id") checklistId: String,
        @QueryValue("template_id") templateId: Int,
        @QueryValue("location_id") locationId: Long?,
        @QueryValue("channel") channel: RegistryChannel,
        @QueryValue("sub_channel") subChannel: RegistrySubChannel
    ): Mono<RegistryChecklistResponseTO> {
        return unmarkChecklistService.unmarkChecklistId(registryId, checklistId, templateId)
    }
}
