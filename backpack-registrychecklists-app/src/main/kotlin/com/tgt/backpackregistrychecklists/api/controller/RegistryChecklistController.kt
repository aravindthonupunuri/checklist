package com.tgt.backpackregistrychecklists.api.controller

import com.tgt.backpackregistrychecklists.service.AddChecklistService
import com.tgt.backpackregistrychecklists.service.GetCheckListTemplatesService
import com.tgt.backpackregistrychecklists.service.RemoveChecklistService
import com.tgt.backpackregistrychecklists.transport.RegistryChecklistResponseTO
import com.tgt.backpackregistrychecklists.transport.RegistryChecklistTemplateResponseTO
import com.tgt.backpackregistryclient.util.RegistryChannel
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import reactor.core.publisher.Mono
import java.util.*

@Controller("/registries/v2")
class RegistryChecklistController(
) {

    @Get("/checklists")
    @Status(HttpStatus.OK)
    @ApiResponse(
        content = [Content(mediaType = "application/json", schema = Schema(implementation = RegistryChecklistTemplateResponseTO::class))]
    )
    fun getAllChecklistTemplates(
        @Header("profile_id") guestId: String,
        @QueryValue("registry_type") registryType: String,
        @QueryValue("location_id") locationId: Long?,
        @QueryValue("channel") channel: RegistryChannel,
        @QueryValue("sub_channel") subChannel: RegistrySubChannel
    ): Mono<RegistryChecklistTemplateResponseTO> {
        return Mono.empty()
//        return getCheckListTemplatesService.getTemplatesForRegistryType(registryType)
    }

    @Post("/{registry_id}/checklists/{checklist_id}")
    @Status(HttpStatus.CREATED)
    @ApiResponse(
        content = [Content(mediaType = "application/json", schema = Schema(implementation = RegistryChecklistResponseTO::class))]
    )
    fun markChecklist(
        @Header("profile_id") guestId: String,
        @PathVariable("registry_id") registryId: UUID,
        @PathVariable("checklist_id") checklistId: String,
        @QueryValue("location_id") locationId: Long?,
        @QueryValue("channel") channel: RegistryChannel,
        @QueryValue("sub_channel") subChannel: RegistrySubChannel
    ): Mono<RegistryChecklistResponseTO> {
        return Mono.empty()
//        return addChecklistService.addChecklistId(registryId, checklistId)
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
        @QueryValue("location_id") locationId: Long?,
        @QueryValue("channel") channel: RegistryChannel,
        @QueryValue("sub_channel") subChannel: RegistrySubChannel
    ): Mono<RegistryChecklistResponseTO> {
        return Mono.empty()
//        return removeChecklistService.removeChecklistId(registryId, checklistId)
    }

}
