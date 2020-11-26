package com.tgt.backpackregistrychecklists.api.auth

import com.tgt.lists.common.components.filters.auth.permissions.DefaultListPermissionManager
import com.tgt.lists.common.components.filters.auth.permissions.ListPermissionManager
import com.tgt.listspermissions.api.client.ListPermissionClientManager
import com.tgt.listspermissions.api.client.ListPermissionsClient
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory

@Factory
class RegistryChecklistPermissionManagerFactory(private val permissionsClient: ListPermissionsClient) {
    val listFallbackPermissionManager = ListPermissionClientManager(permissionsClient)

    @Bean
    fun newListPermissionManager(): ListPermissionManager {
        // There is no fall back for permissions in checklist MS
        return DefaultListPermissionManager(listFallbackPermissionManager, null, false)
    }
}
