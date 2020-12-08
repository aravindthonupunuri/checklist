package com.tgt.backpackregistrychecklists.api.controller

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.tgt.backpackregistrychecklists.service.*
import com.tgt.backpackregistrychecklists.transport.*
import com.tgt.backpackregistryclient.util.RegistryChannel
import com.tgt.backpackregistryclient.util.RegistrySubChannel
import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.multipart.CompletedFileUpload
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import reactor.core.publisher.Mono
import java.io.IOException
import java.util.*
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException

@Controller("/registry_checklists/v1")
class RegistryChecklistController(
    private val createChecklistTemplateService: CreateChecklistTemplateService,
    private val getCheckListTemplatesService: GetChecklistTemplatesService,
    private val deleteChecklistService: DeleteChecklistService,
    private val markChecklistService: MarkChecklistService,
    private val unmarkChecklistService: UnmarkChecklistService,
    private val getRegistryChecklistsService: GetRegistryChecklistsService
) {
    @Post(value = "/checklists", consumes = [MediaType.MULTIPART_FORM_DATA])
    @Status(HttpStatus.CREATED)
    @Throws(XMLStreamException::class, IOException::class)
    fun createChecklist(
        @Header("profile_id") guestId: String,
        @QueryValue("channel") channel: RegistryChannel,
        @QueryValue("sub_channel") subChannel: RegistrySubChannel,
        @QueryValue("location_id") locationId: Long?,
        @QueryValue("registry_type") registryType: RegistryType,
        @QueryValue("template_id") templateId: Int,
        @QueryValue("checklist_name") checklistName: String,
        @Body("file") requestBody: CompletedFileUpload
    ): Mono<Void> {
        val resourceAsStream = this.javaClass.classLoader.getResourceAsStream(requestBody.filename)
        val inputFactory = XMLInputFactory.newInstance()
        val xmlStreamReader = inputFactory.createXMLStreamReader(resourceAsStream)
        val mapper = XmlMapper()
        val checklist: Checklist = mapper.readValue(xmlStreamReader, Checklist::class.java)

        return createChecklistTemplateService.uploadChecklistToDatabase(registryType, checklist, templateId, checklistName)
    }

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

    @Get("/{registry_id}/checklists")
    @Status(HttpStatus.OK)
    @ApiResponse(
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ChecklistResponseTO::class))]
    )
    fun getRegistryChecklists(
        @Header("profile_id") guestId: String,
        @PathVariable("registry_id") registryId: UUID,
        @QueryValue("location_id") locationId: Long?,
        @QueryValue("channel") channel: RegistryChannel,
        @QueryValue("sub_channel") subChannel: RegistrySubChannel
    ): Mono<ChecklistResponseTO> {
        return getRegistryChecklistsService.getChecklistsForRegistryId(registryId, guestId, channel, subChannel)
    }

    @Put("/{registry_id}/checklists")
    @Status(HttpStatus.OK)
    @ApiResponse(
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ChecklistResponseTO::class))]
    )
    fun updateDefaultTemplate(
        @Header("profile_id") guestId: String,
        @PathVariable("registry_id") registryId: UUID,
        @QueryValue("location_id") locationId: Long?,
        @QueryValue("channel") channel: RegistryChannel,
        @QueryValue("sub_channel") subChannel: RegistrySubChannel,
        @Body registryChecklistRequest: RegistryChecklistRequestTO
    ): Mono<ChecklistResponseTO> {
        return Mono.empty()
    }

    @Post("/{registry_id}/checklists/{checklist_id}")
    @Status(HttpStatus.CREATED)
    @ApiResponse(
        content = [Content(mediaType = "application/json", schema = Schema(implementation = RegistryChecklistResponseTO::class))]
    )
    fun markChecklist(
        @Header("profile_id") guestId: String,
        @PathVariable("registry_id") registryId: UUID,
        @PathVariable("checklist_id") checklistId: Int,
        @QueryValue("location_id") locationId: Long?,
        @QueryValue("channel") channel: RegistryChannel,
        @QueryValue("sub_channel") subChannel: RegistrySubChannel,
        @Body registryChecklistRequest: RegistryChecklistRequestTO
    ): Mono<RegistryChecklistResponseTO> {
        return markChecklistService.markChecklistId(registryId, checklistId, registryChecklistRequest, subChannel)
    }

    @Delete("/{registry_id}/checklists/{checklist_id}")
    @Status(HttpStatus.OK)
    @ApiResponse(
        content = [Content(mediaType = "application/json", schema = Schema(implementation = RegistryChecklistResponseTO::class))]
    )
    fun unmarkChecklist(
        @Header("profile_id") guestId: String,
        @PathVariable("registry_id") registryId: UUID,
        @PathVariable("checklist_id") checklistId: Int,
        @QueryValue("template_id") templateId: Int,
        @QueryValue("location_id") locationId: Long?,
        @QueryValue("channel") channel: RegistryChannel,
        @QueryValue("sub_channel") subChannel: RegistrySubChannel
    ): Mono<RegistryChecklistResponseTO> {
        return unmarkChecklistService.unmarkChecklistId(registryId, checklistId, templateId)
    }

    @Delete(value = "/checklists")
    @Status(HttpStatus.NO_CONTENT)
    fun deleteChecklist(
        @Header("profile_id") guestId: String,
        @QueryValue("channel") channel: RegistryChannel,
        @QueryValue("sub_channel") subChannel: RegistrySubChannel,
        @QueryValue("location_id") locationId: Long?,
        @QueryValue("template_id") templateId: Int
    ): Mono<Void> {
        return deleteChecklistService.deleteChecklist(guestId, templateId)
    }
}
