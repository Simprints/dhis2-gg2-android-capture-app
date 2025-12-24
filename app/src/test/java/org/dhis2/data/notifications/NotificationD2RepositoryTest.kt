package org.dhis2.data.notifications


import NotificationsApi
import UserGroupsApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.prefs.Preference.Companion.NOTIFICATIONS
import org.dhis2.usescases.notifications.domain.Notification
import org.dhis2.usescases.notifications.domain.ReadBy
import org.dhis2.usescases.notifications.domain.Recipients
import org.dhis2.usescases.notifications.domain.Ref
import org.dhis2.usescases.notifications.domain.UserGroups
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class NotificationD2RepositoryTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Mock
    lateinit var basicPreferenceProvider: BasicPreferenceProvider

    @Mock
    lateinit var notificationsApi: NotificationsApi

    @Mock
    lateinit var userGroupsApi: UserGroupsApi

    val user = givenAnUser()

    @Test
    fun `Should sync empty notifications if it's empty in remote`()  = runBlocking {
        val repository = givenTestData(user, listOf(), UserGroups(userGroups = listOf()))

        repository.sync().first()

        verify(basicPreferenceProvider).saveAsJson(
            NOTIFICATIONS,
            listOf<Notification>(),
        )
    }

    @Test
    fun `Should sync notifications with all receivers`() = runBlocking {
        val notifications = listOf(givenANotification(wildcard = "ALL"))

        val repository = givenTestData(user, notifications, UserGroups(userGroups = listOf()))

        repository.sync().first()

        verify(basicPreferenceProvider).saveAsJson(
            NOTIFICATIONS,
            notifications,
        )
    }

    @Test
    fun `Should sync notifications with android receivers and for specific user`()  = runBlocking{
        val notifications = listOf(
            givenANotification(
                wildcard = "Android",
                users = listOf(Ref(id = user.uid(), name = null))
            )
        )

        val repository = givenTestData(user, notifications, UserGroups(userGroups = listOf()))

        repository.sync().first()

        verify(basicPreferenceProvider).saveAsJson(
            NOTIFICATIONS,
            notifications,
        )
    }

    @Test
    fun `Should sync notifications with android receivers and for specific userGroup`()  = runBlocking{
        val notifications = listOf(
            givenANotification(
                wildcard = "Android",
                userGroups = arrayListOf(Ref(id = "userGroup1", name = null))
            )
        )

        val repository = givenTestData(
            user,
            notifications,
            UserGroups(userGroups = listOf(Ref(id = "userGroup1", name = null)))
        )

        repository.sync().first()

        verify(basicPreferenceProvider).saveAsJson(
            NOTIFICATIONS,
            notifications,
        )
    }

    @Test
    fun `Should sync notifications with both receivers and for specific user`()  = runBlocking{
        val notifications = listOf(
            givenANotification(
                wildcard = "Both",
                users = listOf(Ref(id = user.uid(), name = null))
            )
        )

        val repository = givenTestData(user, notifications, UserGroups(userGroups = listOf()))

        repository.sync().first()

        verify(basicPreferenceProvider).saveAsJson(
            NOTIFICATIONS,
            notifications,
        )
    }

    @Test
    fun `Should sync notifications with both receivers and for specific userGroup`()  = runBlocking{
        val notifications = listOf(
            givenANotification(
                wildcard = "both",
                userGroups = arrayListOf(Ref(id = "userGroup1", name = null))
            )
        )

        val repository = givenTestData(
            user,
            notifications,
            UserGroups(userGroups = listOf(Ref(id = "userGroup1", name = null)))
        )

        repository.sync().first()

        verify(basicPreferenceProvider).saveAsJson(
            NOTIFICATIONS,
            notifications,
        )
    }

    @Test
    fun `Should not sync notifications with web receivers and for specific user`() = runBlocking {
        val notifications = listOf(
            givenANotification(
                wildcard = "Web",
                users = listOf(Ref(id = user.uid(), name = null))
            )
        )

        val repository = givenTestData(user, notifications, UserGroups(userGroups = listOf()))

        repository.sync().first()

        verify(basicPreferenceProvider).saveAsJson(
            NOTIFICATIONS,
            listOf<Notification>(),
        )
    }

    @Test
    fun `Should not sync notifications with web receivers and for specific userGroup`()  = runBlocking{
        val notifications = listOf(
            givenANotification(
                wildcard = "web",
                userGroups = arrayListOf(Ref(id = "userGroup1", name = null))
            )
        )

        val repository = givenTestData(
            user,
            notifications,
            UserGroups(userGroups = listOf(Ref(id = "userGroup1", name = null)))
        )

        repository.sync().first()

        verify(basicPreferenceProvider).saveAsJson(
            NOTIFICATIONS,
            listOf<Notification>(),
        )
    }

    @Test
    fun `Should only sync expected notifications`() = runBlocking {
        val forWebByUserGroup = givenANotification(
            wildcard = "web",
            userGroups = arrayListOf(Ref(id = "userGroup1", name = null))
        )
        val forWebByUser = givenANotification(
            wildcard = "WEB",
            users = listOf(Ref(id = user.uid(), name = null))
        )
        val forBothByUserGroup = givenANotification(
            wildcard = "Both",
            userGroups = arrayListOf(Ref(id = "userGroup1", name = null))
        )
        val forBothByUser = givenANotification(
            wildcard = "both",
            users = listOf(Ref(id = user.uid(), name = null))
        )
        val forAndroidByUserGroup = givenANotification(
            wildcard = "Android",
            userGroups = arrayListOf(Ref(id = "userGroup1", name = null))
        )
        val forAndroidByUser = givenANotification(
            wildcard = "android",
            users = listOf(Ref(id = user.uid(), name = null))
        )
        val forAll = givenANotification(
            wildcard = "All",
        )

        val notifications = listOf(
            forWebByUserGroup,
            forWebByUser,
            forBothByUserGroup,
            forBothByUser,
            forAndroidByUserGroup,
            forAndroidByUser,
            forAll
        )

        val repository = givenTestData(
            user,
            notifications,
            UserGroups(userGroups = listOf(Ref(id = "userGroup1", name = null)))
        )

        repository.sync().first()

        verifySyncedNotifications(
            forBothByUserGroup,
            forBothByUser,
            forAndroidByUserGroup,
            forAndroidByUser,
            forAll
        )
    }

    private fun givenTestData(
        user: User,
        notifications: List<Notification>,
        userGroups: UserGroups
    ): NotificationD2Repository {
        whenever(
            d2.userModule().user()
                .blockingGet(),
        ) doReturn user

        runBlocking {
            whenever(notificationsApi.getData()) doReturn notifications
        }

        runBlocking {
            whenever(userGroupsApi.getData(user.uid())) doReturn userGroups
        }

        return NotificationD2Repository(
            d2,
            basicPreferenceProvider,
            notificationsApi,
            userGroupsApi
        )
    }

    private fun givenAnUser(): User {
        val user = User.builder().uid("user1").build()

        whenever(
            d2.userModule().user()
                .blockingGet(),
        ) doReturn User.builder().uid("user1").build()

        return user
    }

    private fun givenANotification(
        readBy: List<ReadBy> = listOf(),
        userGroups: ArrayList<Ref> = ArrayList(),
        users: List<Ref> = listOf(),
        wildcard: String = ""
    ): Notification {
        return Notification(
            content = "test",
            id = "1",
            createdAt = Date(),
            readBy = readBy,
            recipients = Recipients(
                userGroups = userGroups,
                users = users,
                wildcard = wildcard
            ),
            permissions = null
        )
    }

    private fun verifySyncedNotifications(
        forBothByUserGroup: Notification,
        forBothByUser: Notification,
        forAndroidByUserGroup: Notification,
        forAndroidByUser: Notification,
        forAll: Notification
    ) {
        val captor = argumentCaptor<List<Notification>>()
        verify(basicPreferenceProvider).saveAsJson(eq(NOTIFICATIONS), captor.capture())

        val expectedNotifications = listOf(
            forBothByUserGroup,
            forBothByUser,
            forAndroidByUserGroup,
            forAndroidByUser,
            forAll
        )

        assertEquals(expectedNotifications.size, captor.firstValue.size)
        assertTrue(captor.firstValue.containsAll(expectedNotifications))
    }
}