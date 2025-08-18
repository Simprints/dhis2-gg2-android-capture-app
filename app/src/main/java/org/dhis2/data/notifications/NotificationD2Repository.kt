package org.dhis2.data.notifications

import NotificationsApi
import UserGroupsApi
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.prefs.Preference
import org.dhis2.usescases.notifications.domain.Notification
import org.dhis2.usescases.notifications.domain.NotificationRepository
import org.dhis2.usescases.notifications.domain.ReadBy
import org.dhis2.usescases.notifications.domain.Recipients
import org.dhis2.usescases.notifications.domain.Ref
import org.dhis2.usescases.notifications.domain.UserGroups
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.BaseIdentifiableObject
import timber.log.Timber

class NotificationD2Repository(
    private val d2: D2,
    private val preferenceProvider: BasicPreferenceProvider,
    private val notificationsApi: NotificationsApi,
    private val userGroupsApi: UserGroupsApi
) : NotificationRepository {

    override fun sync(): Flow<Unit> = flow {
        try {
            val allNotifications = getAllNotificationsFromRemote()

            val userGroups = getUserGroups()

            val userNotifications =
                getNotificationsForCurrentUser(allNotifications, userGroups.userGroups)

            preferenceProvider.saveAsJson(Preference.NOTIFICATIONS, userNotifications)

            emit(Unit)

            Timber.d("Notifications synced")
            Timber.d("Notifications: $userNotifications")

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun get(): Flow<List<Notification>> = flow {
        val listStringType = object : TypeToken<List<Notification>>() {}

        val notifications = preferenceProvider.getObjectFromJson(
            Preference.NOTIFICATIONS,
            listStringType,
            listOf()
        )

        emit(notifications)
    }

    override fun getById(id: String): Flow<Notification?> {
        return sync().flatMapConcat {
            get()
        }.map { notifications ->
            notifications.find { it.id == id }
        }
    }

    override fun save(notification: Notification): Flow<Unit> = flow {
        try {
            val notifications = getAllNotificationsFromRemote().map {
                if (it.id == notification.id) {
                    notification
                } else {
                    it
                }
            }

            val notificationsDTO = notifications.map { mapNotificationDTOs(it) }

            notificationsApi.postData(notificationsDTO)

            emit(Unit)

        } catch (e: Exception) {
            Timber.e("Error updating notifications: $e")
        }
    }.flatMapConcat { sync() }

    private suspend fun getAllNotificationsFromRemote(): List<Notification> {
        try {
            val notificationsDTO = notificationsApi.getData()

            val notifications = notificationsDTO.map { mapNotification(it) }

            return notifications
        } catch (e: Exception) {
            Timber.e("Error getting notifications: $e")
            return emptyList()
        }
    }

    private suspend fun getUserGroups(): UserGroups {
        try {
            val userGroupsDTO =
                userGroupsApi.getData(d2.userModule().user().blockingGet()!!.uid())

            val userGroups = mapUserGroups(userGroupsDTO)

            return userGroups
        } catch (e: Exception) {
            Timber.e("Error getting userGroups: $e")
            return UserGroups(listOf())
        }
    }

    private fun getNotificationsForCurrentUser(
        allNotifications: List<Notification>,
        userGroups: List<Ref>
    ): List<Notification> {
        val userGroupIds = userGroups.map { it.id }

        val nonReadByUserNotifications = allNotifications.filter { notification ->
            notification.readBy.none { readBy ->
                readBy.id == d2.userModule().user().blockingGet()!!.uid()
            }
        }

        val notificationsByAll = nonReadByUserNotifications.filter { notification ->
            notification.recipients.wildcard == "ALL"
        }

        val notificationsByUserGroup = nonReadByUserNotifications.filter { notification ->
            notification.recipients.userGroups.any { userGroupIds.contains(it.id) }
        }

        val notificationsByUser = nonReadByUserNotifications.filter { notification ->
            notification.recipients.users.any {
                it.id == d2.userModule().user().blockingGet()!!.uid()
            }
        }

        return notificationsByAll + notificationsByUserGroup + notificationsByUser
    }

    private fun mapNotification(notificationDTO: NotificationDTO): Notification {
        return Notification(
            content = notificationDTO.content,
            createdAt = BaseIdentifiableObject.parseDate(notificationDTO.createdAt),
            id = notificationDTO.id,
            readBy = notificationDTO.readBy.map {
                ReadBy(
                    BaseIdentifiableObject.parseDate(it.date),
                    it.id,
                    it.name
                )
            },
            recipients = Recipients(
                userGroups = notificationDTO.recipients.userGroups.map { Ref(it.id, it.name) },
                users = notificationDTO.recipients.users.map { Ref(it.id, it.name) },
                wildcard = notificationDTO.recipients.wildcard
            )
        )
    }

    private fun mapNotificationDTOs(notification: Notification): NotificationDTO {
        return NotificationDTO(
            id = notification.id,
            content = notification.content,
            createdAt = BaseIdentifiableObject.dateToDateStr(notification.createdAt),
            readBy = notification.readBy.map {
                ReadByDTO(
                    BaseIdentifiableObject.dateToDateStr(it.date),
                    it.id,
                    it.name
                )
            },
            recipients = RecipientsDTO(
                userGroups = notification.recipients.userGroups.map { RefDTO(it.id, it.name) },
                users = notification.recipients.users.map { RefDTO(it.id, it.name) },
                wildcard = notification.recipients.wildcard
            )
        )
    }

    private fun mapUserGroups(userGroupsDTO: UserGroupsDTO): UserGroups {
        return UserGroups(
            userGroups = userGroupsDTO.userGroups.map { Ref(it.id, it.name) }
        )
    }
}


