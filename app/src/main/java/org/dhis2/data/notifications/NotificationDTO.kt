package org.dhis2.data.notifications

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDTO(
    val content: String,
    @Contextual
    val createdAt: String?,
    val id: String,
    val readBy: List<ReadByDTO>,
    val recipients: RecipientsDTO,
    val permissions: PermissionsDTO?,
    val translations: Map<String, String>?
)

@Serializable
data class ReadByDTO(
    @Contextual
    val date: String?,
    val id: String,
    val name: String
)

@Serializable
data class RecipientsDTO(
    val userGroups: List<RefDTO>,
    val users: List<RefDTO>,
    val wildcard: String
)

@Serializable
data class RefDTO(
    val id: String,
    val name: String?
)

@Serializable
data class UserGroupsDTO(
    val userGroups: List<RefDTO>,
)

@Serializable
data class PermissionsDTO(
    val publicAccess: String,
    val userAccesses: List<UserAccessesDTO>,
    val userGroupAccesses: List<UserGroupAccessesDTO>,
)

@Serializable
data class UserAccessesDTO(
    val access: String,
    val id: String,
    val name: String,
)

@Serializable
data class UserGroupAccessesDTO(
    val access: String,
    val id: String,
    val name: String,
)