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
import io.micronaut.http.multipart.StreamingFileUpload
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.io.File
import java.io.FileInputStream
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
    private val getRegistryChecklistsService: GetRegistryChecklistsService,
    private val updateDefaultTemplateService: UpdateDefaultTemplateService
) {

    /**
     *
     * Upload the checklist information from xml to database
     *
     * @param guestId guest id
     * @param channel channel
     * @param subChannel sub channel
     * @param locationId location id
     * @param registryType registry type
     * @param templateId template id
     * @param checklistName checklist name
     * @param requestBody xml file input
     *
     */
    @Post(value = "/checklist_templates", consumes = [MediaType.MULTIPART_FORM_DATA])
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
        @Body("file") requestBody: StreamingFileUpload
    ): Mono<Void> {
        val tempFile = File.createTempFile(requestBody.filename, "temp")
        val uploadPublisher = requestBody.transferTo(tempFile)

        return uploadPublisher.toMono()
            .flatMap {
                val resourceAsStream = FileInputStream(tempFile)
                val inputFactory = XMLInputFactory.newInstance()
                val xmlStreamReader = inputFactory.createXMLStreamReader(resourceAsStream)
                val mapper = XmlMapper()
                val checklist: Checklist = mapper.readValue(xmlStreamReader, Checklist::class.java)
                createChecklistTemplateService.uploadChecklistToDatabase(registryType, checklist, templateId, checklistName)
            }.then()
    }

    /**
     *
     * Get all the available templateIds for the provided registry type
     *
     * @param guestId guest id
     * @param channel channel
     * @param subChannel sub channel
     * @param locationId location id
     * @param registryType registry type
     * @return Registry checklist template response transfer object
     *
     */
    @Get("/checklist_templates")
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

    /**
     *
     * Get all the checklist information for the provided registry id
     *
     * @param guestId guest id
     * @param registryId registry id
     * @param channel channel
     * @param subChannel sub channel
     * @param locationId location id
     * @return Checklist response transfer object
     *
     */
    @Get("/{registry_id}/checklist_templates")
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

    /**
     *
     * Update the templateId for the provided registry id
     *
     * @param guestId guest id
     * @param registryId registry id
     * @param channel channel
     * @param subChannel sub channel
     * @param locationId location id
     * @param registryChecklistRequest Update template request body
     * @return Checklist response transfer object
     *
     */
    @Put("/{registry_id}/checklist_templates/{template_id}")
    @Status(HttpStatus.OK)
    @ApiResponse(
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ChecklistResponseTO::class))]
    )
    fun updateDefaultTemplate(
        @Header("profile_id") guestId: String,
        @PathVariable("registry_id") registryId: UUID,
        @PathVariable("template_id") templateId: Int,
        @QueryValue("location_id") locationId: Long?,
        @QueryValue("channel") channel: RegistryChannel,
        @QueryValue("sub_channel") subChannel: RegistrySubChannel
    ): Mono<ChecklistResponseTO> {
        return updateDefaultTemplateService.updateDefaultTemplateId(guestId, registryId, templateId, channel, subChannel)
    }

    /**
     *
     * Mark the checklist for the provided registryId and checklistId
     *
     * @param guestId guest id
     * @param registryId registry id
     * @param checklistId checklist id
     * @param channel channel
     * @param subChannel sub channel
     * @param locationId location id
     * @param registryChecklistRequest Mark checklist request body
     * @return Registry checklist response transfer object
     *
     */
    @Post("/{registry_id}/checklist_templates/{checklist_id}")
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
        return markChecklistService.markChecklistId(registryId, checklistId, registryChecklistRequest.templateId, subChannel)
    }

    /**
     *
     * Unmark the checklist for the provided registryId and checklistId
     *
     * @param guestId guest id
     * @param registryId registry id
     * @param checklistId checklist id
     * @param templateId template id
     * @param channel channel
     * @param subChannel sub channel
     * @param locationId location id
     * @return Registry checklist response transfer object
     *
     */
    @Delete("/{registry_id}/checklist_templates/{checklist_id}/{template_id}")
    @Status(HttpStatus.OK)
    @ApiResponse(
        content = [Content(mediaType = "application/json", schema = Schema(implementation = RegistryChecklistResponseTO::class))]
    )
    fun unmarkChecklist(
        @Header("profile_id") guestId: String,
        @PathVariable("registry_id") registryId: UUID,
        @PathVariable("checklist_id") checklistId: Int,
        @PathVariable("template_id") templateId: Int,
        @QueryValue("location_id") locationId: Long?,
        @QueryValue("channel") channel: RegistryChannel,
        @QueryValue("sub_channel") subChannel: RegistrySubChannel
    ): Mono<RegistryChecklistResponseTO> {
        return unmarkChecklistService.unmarkChecklistId(registryId, checklistId, templateId)
    }

    /**
     *
     * Delete all the checklist information for the provided templateId in database
     *
     * @param guestId guest id
     * @param channel channel
     * @param subChannel sub channel
     * @param locationId location id
     * @param templateId template id
     *
     */
    @Delete(value = "/checklist_templates")
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
