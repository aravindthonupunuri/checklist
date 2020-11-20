package com.tgt.backpackregistrychecklists.api

import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplate
import com.tgt.backpackregistrychecklists.domain.model.ChecklistTemplatePK
import com.tgt.backpackregistrychecklists.persistence.ChecklistTemplateRepository
import com.tgt.backpackregistrychecklists.test.BasePersistenceFunctionalTest
import com.tgt.backpackregistrychecklists.test.DataProvider
import com.tgt.backpackregistryclient.util.RegistryType
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.client.multipart.MultipartBody
import io.micronaut.test.annotation.MicronautTest
import org.slf4j.Logger

import javax.inject.Inject
import java.time.LocalDateTime

@MicronautTest
class CreateChecklistTemplateFunctionalTest extends BasePersistenceFunctionalTest {

    @Inject
    ChecklistTemplateRepository checklistTemplateRepository

    static File file
    static String appDir
    def setupSpec() {
        appDir = System.getProperty("user.dir")
        file = new File("${appDir}/src/test/functional/resources/file.xml")
        file.write("<checklist>\n")
        file.append("<categories>\n")
        file.append("<category>\n")
        file.append("<checklist_id>201</checklist_id>\n")
        file.append("<l1_taxonomy_id>963002</l1_taxonomy_id>\n")
        file.append("<l1_alias_name>strollers &amp; car seats</l1_alias_name>\n")
        file.append("<l1_display_order>1</l1_display_order>\n")
        file.append("<l2_taxonomy_id>5</l2_taxonomy_id>\n")
        file.append("<l2_child_ids>5xtjw</l2_child_ids>\n")
        file.append("<plp_param>reg_type=baby</plp_param>\n")
        file.append("<l2_alias_name>travel system</l2_alias_name>\n")
        file.append("<l2_display_order>1</l2_display_order>\n")
        file.append("<suggested_item_count>0</suggested_item_count>\n")
        file.append("<default_image>GUEST_8a9644c6-5d35-45df-94cd-482bc9965a86</default_image>\n")
        file.append("<image_url>https://target.scene7.com/is/image/Target/GUEST_8a9644c6-5d35-45df-94cd-482bc9965a86</image_url>\n")
        file.append("</category>\n")
        file.append("<category>\n")
        file.append("<checklist_id>202</checklist_id>\n")
        file.append("<l1_taxonomy_id>963003</l1_taxonomy_id>\n")
        file.append("<l1_alias_name>strollers &amp; car seats</l1_alias_name>\n")
        file.append("<l1_display_order>2</l1_display_order>\n")
        file.append("<l2_taxonomy_id>5</l2_taxonomy_id>\n")
        file.append("<l2_child_ids>5xtjw</l2_child_ids>\n")
        file.append("<plp_param>reg_type=baby</plp_param>\n")
        file.append("<l2_alias_name>travel system</l2_alias_name>\n")
        file.append("<l2_display_order>1</l2_display_order>\n")
        file.append("<suggested_item_count>0</suggested_item_count>\n")
        file.append("<default_image>GUEST_8a9644c6-5d35-45df-94cd-482bc9965a86</default_image>\n")
        file.append("<image_url>https://target.scene7.com/is/image/Target/GUEST_8a9644c6-5d35-45df-94cd-482bc9965a86</image_url>\n")
        file.append("</category>\n")
        file.append("<category>\n")
        file.append("<checklist_id>203</checklist_id>\n")
        file.append("<l1_taxonomy_id>963004</l1_taxonomy_id>\n")
        file.append("<l1_alias_name>strollers &amp; car seats</l1_alias_name>\n")
        file.append("<l1_display_order>3</l1_display_order>\n")
        file.append("<l2_taxonomy_id>5</l2_taxonomy_id>\n")
        file.append("<l2_child_ids>5xtjw</l2_child_ids>\n")
        file.append("<plp_param>reg_type=baby</plp_param>\n")
        file.append("<l2_alias_name>travel system</l2_alias_name>\n")
        file.append("<l2_display_order>1</l2_display_order>\n")
        file.append("<suggested_item_count>0</suggested_item_count>\n")
        file.append("<default_image>GUEST_8a9644c6-5d35-45df-94cd-482bc9965a86</default_image>\n")
        file.append("<image_url>https://target.scene7.com/is/image/Target/GUEST_8a9644c6-5d35-45df-94cd-482bc9965a86</image_url>\n")
        file.append("</category>\n")
        file.append("</categories>\n")
        file.append("</checklist>\n")
    }

    def "test create checklist integrity"() {
        given:
        String guestId = "1236"
        String uri = "registry_checklists/v1/checklists?registry_type=BABY&template_id=1&channel=web&sub_channel=kiosk&checklist_name=checklistname1"

        MultipartBody multipartBody = MultipartBody
            .builder()
            .addPart("file", "file.xml", MediaType.APPLICATION_XML_TYPE, file)
            .build()

        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK(RegistryType.BABY, 1, 2)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, "name", true, 1, "categoryId", "categoryName",
        "categoryImageUrl", "subCategoryId", "subCategoryName", 1, "subCategoryUrl", "plpParam", LocalDateTime.now(), LocalDateTime.now())

        when:
        HttpResponse<Void> response = client.toBlocking()
            .exchange(HttpRequest.POST(uri, multipartBody).headers (DataProvider.getHeaders(guestId)).contentType(MediaType.MULTIPART_FORM_DATA_TYPE), Void)

        def actualStatus = response.status()

        then:
        actualStatus == HttpStatus.CREATED
    }

    def "test create checklist checklisttemplate table already have that templateid"() {
        given:
        String guestId = "1236"
        String uri = "registry_checklists/v1/checklists?registry_type=WEDDING&template_id=91&channel=web&sub_channel=kiosk&checklist_name=checklistname12"

        MultipartBody multipartBody = MultipartBody
            .builder()
            .addPart("file", "file.xml", MediaType.APPLICATION_XML_TYPE, file)
            .build()

        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK(RegistryType.WEDDING, 91, 4)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, "name", true, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "subCategoryName", 1, "subCategoryUrl", "plpParam", LocalDateTime.now(), LocalDateTime.now())
        checklistTemplateRepository.save(checklistTemplate).block()

        when:
        HttpResponse<Void> response = client.toBlocking()
            .exchange(HttpRequest.POST(uri, multipartBody).headers (DataProvider.getHeaders(guestId)).contentType(MediaType.MULTIPART_FORM_DATA_TYPE), Void)

        def actualStatus = response.status()

        then:
        actualStatus == HttpStatus.CREATED
    }

    def "test create checklist checklisttemplate table  already have that registryType"() {
        given:
        String guestId = "1236"
        String uri = "registry_checklists/v1/checklists?registry_type=BABY&template_id=2&channel=web&sub_channel=kiosk&checklist_name=checklistname13"

        MultipartBody multipartBody = MultipartBody
            .builder()
            .addPart("file", "file.xml", MediaType.APPLICATION_XML_TYPE, file)
            .build()

        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK(RegistryType.BABY, 1, 4)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, "name", false, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "subCategoryName", 1, "subCategoryUrl", "plpParam", LocalDateTime.now(), LocalDateTime.now())
        checklistTemplateRepository.save(checklistTemplate).block()

        when:
        HttpResponse<Void> response = client.toBlocking()
            .exchange(HttpRequest.POST(uri, multipartBody).headers (DataProvider.getHeaders(guestId)).contentType(MediaType.MULTIPART_FORM_DATA_TYPE), Void)

        def actualStatus = response.status()

        then:
        actualStatus == HttpStatus.CREATED
    }

    def "test create checklist checklisttemplate table  already have that checklist name for the templateId which we are going to update"() {
        given:
        String guestId = "1236"
        String uri = "registry_checklists/v1/checklists?registry_type=BABY&template_id=7&channel=web&sub_channel=kiosk&checklist_name=checklistName"

        MultipartBody multipartBody = MultipartBody
            .builder()
            .addPart("file", "file.xml", MediaType.APPLICATION_XML_TYPE, file)
            .build()

        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK(RegistryType.BABY, 7, 4)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, "checklistName", false, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "subCategoryName", 1, "subCategoryUrl", "plpParam", LocalDateTime.now(), LocalDateTime.now())
        checklistTemplateRepository.save(checklistTemplate).block()

        when:
        HttpResponse<Void> response = client.toBlocking()
            .exchange(HttpRequest.POST(uri, multipartBody).headers (DataProvider.getHeaders(guestId)).contentType(MediaType.MULTIPART_FORM_DATA_TYPE), Void)

        def actualStatus = response.status()

        then:
        actualStatus == HttpStatus.CREATED
    }

    def "test create checklist checklisttemplate table  already have that checklist name for a different"() {
        given:
        String guestId = "1236"
        String uri = "registry_checklists/v1/checklists?registry_type=BABY&template_id=6&channel=web&sub_channel=kiosk&checklist_name=checklistName"

        MultipartBody multipartBody = MultipartBody
            .builder()
            .addPart("file", "file.xml", MediaType.APPLICATION_XML_TYPE, file)
            .build()

        ChecklistTemplatePK checklistTemplatePK = new ChecklistTemplatePK(RegistryType.BABY, 11, 3)
        ChecklistTemplate checklistTemplate = new ChecklistTemplate(checklistTemplatePK, "checklistName", false, 1, "categoryId", "categoryName",
            "categoryImageUrl", "subCategoryId", "subCategoryName", 1, "subCategoryUrl", "plpParam", LocalDateTime.now(), LocalDateTime.now())
        checklistTemplateRepository.save(checklistTemplate).block()

        when:
        HttpResponse<Void> response = client.toBlocking()
            .exchange(HttpRequest.POST(uri, multipartBody).headers (DataProvider.getHeaders(guestId)).contentType(MediaType.MULTIPART_FORM_DATA_TYPE), Void)

        def actualStatus = response.status()

        then:
        def error = thrown(HttpClientResponseException)
        error.status == HttpStatus.BAD_REQUEST
    }

    @Override
    Logger getLogger() {
        return null
    }
}
