package org.dhis2.usescases.notifications.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.dhis2.usescases.notifications.domain.GetNotifications
import org.dhis2.usescases.notifications.domain.MarkNotificationAsRead
import org.dhis2.usescases.notifications.domain.Notification

class NotificationsPresenter(
    private val getNotifications: GetNotifications,
    private val markNotificationAsRead: MarkNotificationAsRead
) {
    fun refresh(notificationsView: NotificationsView,) {
        if (ShowNotifications.isPending) {
            ShowNotifications.isPending = false
            CoroutineScope(Dispatchers.Main).launch {
                getNotifications().collect {
                    notificationsView.renderNotifications(it)
                }
            }
        }
    }

    fun markShowNotificationsAsPending() {
        ShowNotifications.isPending = true
    }

    fun markNotificationAsRead(notification: Notification) {
        CoroutineScope(Dispatchers.IO).launch {
            markNotificationAsRead.invoke(notification.id).collect {}
        }
    }
}

object ShowNotifications {
    var isPending = false
}

interface NotificationsView {
    fun renderNotifications(notifications: List<Notification>)
}
