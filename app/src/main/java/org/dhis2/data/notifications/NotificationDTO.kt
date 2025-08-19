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
    val recipients: RecipientsDTO
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