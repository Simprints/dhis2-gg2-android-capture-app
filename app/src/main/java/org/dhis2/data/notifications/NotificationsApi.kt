package org.dhis2.data.notifications
import org.dhis2.data.notifications.NotificationDTO
import org.dhis2.data.notifications.UserGroupsDTO
import org.hisp.dhis.android.core.arch.api.HttpServiceClient
import org.hisp.dhis.android.core.user.User

class NotificationsApi (private val client: HttpServiceClient) {
    suspend fun getData(): List<NotificationDTO>{
        return client.get {
            url("dataStore/notifications/notifications")
        }
    }

    suspend fun postData( notifications:List<NotificationDTO>): User {
        return client.put {
            url("dataStore/notifications/notifications")
            body(notifications)
        }
    }
}

class UserGroupsApi (private val client: HttpServiceClient) {
    suspend fun getData( userId:String): UserGroupsDTO {
        return client.get {
            url("users/$userId?fields=userGroups")
        }
    }
}

