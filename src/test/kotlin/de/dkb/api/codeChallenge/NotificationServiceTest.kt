package de.dkb.api.codeChallenge.notification

import de.dkb.api.codeChallenge.notification.exception.*
import de.dkb.api.codeChallenge.notification.model.*
import de.dkb.api.codeChallenge.notification.repository.CategoryNotificationRepository
import de.dkb.api.codeChallenge.notification.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.*
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.assertThrows

@ExtendWith(MockitoExtension::class)
class NotificationServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var categoryNotificationRepository: CategoryNotificationRepository
    private lateinit var notificationService: NotificationService

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        categoryNotificationRepository = mock()
        notificationService = NotificationService(userRepository, categoryNotificationRepository)
    }

    @Test
    fun `addNotificationType should save new category notification`() {
        val request = AddNotificationTypeRequest("A", "type1")

        notificationService.addNotificationType(request)

        val captor = argumentCaptor<CategoryNotification>()
        verify(categoryNotificationRepository).save(captor.capture())
        assertEquals("A", captor.firstValue.id.category)
        assertEquals("type1", captor.firstValue.id.notificationType)
    }

    @Test
    fun `addNotificationType should throw NotificationException when notificationType is blank`() {
        val request = AddNotificationTypeRequest("A", " ")

        val exception = assertThrows<NotificationException> {
            notificationService.addNotificationType(request)
        }

        assertEquals("notificationType must not be blank", exception.message)
        verify(categoryNotificationRepository, never()).existsById(any())
        verify(categoryNotificationRepository, never()).save(any())
    }

    @Test
    fun `addNotificationType should throw NotificationTypeAlreadyExistsException when mapping type to new category`() {
        val existingType = "type1"
        val existingCategory = "A"
        val newCategory = "B"

        val request = AddNotificationTypeRequest(newCategory, existingType)

        val existingMapping = CategoryNotification(CategoryNotificationId(existingCategory, existingType))
        whenever(categoryNotificationRepository.findByIdNotificationType(existingType)).thenReturn(existingMapping)

        val exception = assertThrows(NotificationTypeAlreadyExistsException::class.java) {
            notificationService.addNotificationType(request)
        }

        assertTrue(exception.message!!.contains(existingType))
        assertTrue(exception.message!!.contains(existingCategory))
        assertTrue(exception.message!!.contains(newCategory.uppercase()))

        verify(categoryNotificationRepository, never()).save(any())
    }
}
