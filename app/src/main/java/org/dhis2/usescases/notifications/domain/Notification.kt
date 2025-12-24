package org.dhis2.usescases.notifications.domain

import java.util.Date

data class Notification(
    val content: String,
    val createdAt: Date,
    val id: String,
    val readBy: List<ReadBy>,
    val recipients: Recipients,
    val permissions: Permissions?,
    val translations: Map<String, String>?
)

data class ReadBy(
    val date: Date,
    val id: String,
    val name: String
)

data class Recipients(
    val userGroups: List<Ref>,
    val users: List<Ref>,
    val wildcard: String
)

data class Ref(
    val id: String,
    val name: String?
)

data class UserGroups(
    val userGroups: List<Ref>,
)

data class Permissions(
    val publicAccess: String,
    val userAccesses: List<UserAccesses>,
    val userGroupAccesses: List<UserGroupAccesses>,
)

data class UserAccesses(
    val access: String,
    val id: String,
    val name: String,
)

data class UserGroupAccesses(
    val access: String,
    val id: String,
    val name: String,
)