package com.tgt.backpackregistrychecklists.api.controller

import com.tgt.backpackregistrychecklists.service.GetChecklistTemplatesService
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

@Controller("/registry_checklists/v1")
class RegistryChecklistController(
    private val getCheckListTemplatesService: GetChecklistTemplatesService
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
}
