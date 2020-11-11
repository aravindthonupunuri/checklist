package com.tgt.backpackregistrychecklists.api.auth

import com.tgt.lists.common.components.exception.BaseErrorCodes
import com.tgt.lists.common.components.exception.ForbiddenException
import com.tgt.lists.common.components.filters.auth.permissions.ListPermissionManager
import com.tgt.listspermissions.api.client.ListPermissionsClient
import com.tgt.listspermissions.domain.model.PermissionType
import io.micronaut.http.HttpMethod
import reactor.core.publisher.Mono
import java.util.*
import javax.inject.Singleton

@Singleton
class RegistryChecklistPermissionManager(private val permissionsClient: ListPermissionsClient) : ListPermissionManager {

    override fun authorize(userId: String, listId: UUID, requestMethod: HttpMethod): Mono<Boolean> {
        val permissionType: PermissionType = when (requestMethod) {
            HttpMethod.GET -> PermissionType.READ
            HttpMethod.POST -> PermissionType.CREATE
            HttpMethod.PUT -> PermissionType.UPDATE
            HttpMethod.DELETE -> PermissionType.DELETE
            else -> throw ForbiddenException(BaseErrorCodes.FORBIDDEN_ERROR_CODE(listOf("User is not allowed to access Registry $listId")))
        }
        return permissionsClient.checkResourceSubResourcePermission(resourceId = listId, memberId = userId, permission = permissionType).map { true }
    }
}
