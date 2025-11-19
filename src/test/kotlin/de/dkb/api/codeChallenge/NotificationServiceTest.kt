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
import java.util.*

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

    @Test
    fun `registerUser should save user subscriptions for new categories`() {
        val userId = UUID.randomUUID()
        val request = RegisterRequest(userId, setOf("type1", "type2"))

        val catType1 = CategoryNotification(CategoryNotificationId("A", "type1"))
        val catType2 = CategoryNotification(CategoryNotificationId("B", "type2"))
        whenever(categoryNotificationRepository.findByIdNotificationType("type1")).thenReturn(catType1)
        whenever(categoryNotificationRepository.findByIdNotificationType("type2")).thenReturn(catType2)
        whenever(categoryNotificationRepository.findByIdNotificationTypeIn(setOf("type1", "type2")))
            .thenReturn(listOf(catType1, catType2))

        whenever(userRepository.existsByUserIdAndCategory(userId, "A")).thenReturn(false)
        whenever(userRepository.existsByUserIdAndCategory(userId, "B")).thenReturn(false)

        notificationService.registerUser(request)

        val captor = argumentCaptor<User>()
        verify(userRepository, times(2)).save(captor.capture())
        val savedCategories = captor.allValues.map { it.id.category }
        assertTrue(savedCategories.containsAll(listOf("A", "B")))
    }

    @Test
    fun `registerUser should throw NotificationTypeNotFoundException when notification type is invalid`() {
        val userId = UUID.randomUUID()
        val request = RegisterRequest(userId, setOf("unknown"))

        // Mock repository to return null for invalid type
        whenever(categoryNotificationRepository.findByIdNotificationType("unknown")).thenReturn(null)

        assertThrows(NotificationTypeNotFoundException::class.java) {
            notificationService.registerUser(request)
        }
    }

    @Test
    fun `registerUser should save only new categories for an existing user`() {
        val userId = UUID.randomUUID()
        val request = RegisterRequest(userId, setOf("type1", "type2"))

        val catType1 = CategoryNotification(CategoryNotificationId("A", "type1"))
        val catType2 = CategoryNotification(CategoryNotificationId("B", "type2"))
        whenever(categoryNotificationRepository.findByIdNotificationType("type1")).thenReturn(catType1)
        whenever(categoryNotificationRepository.findByIdNotificationType("type2")).thenReturn(catType2)
        whenever(categoryNotificationRepository.findByIdNotificationTypeIn(setOf("type1", "type2")))
            .thenReturn(listOf(catType1, catType2))

        whenever(userRepository.existsByUserIdAndCategory(userId, "A")).thenReturn(true)
        whenever(userRepository.existsByUserIdAndCategory(userId, "B")).thenReturn(false)

        notificationService.registerUser(request)

        val captor = argumentCaptor<User>()
        verify(userRepository, times(1)).save(captor.capture())
        assertEquals("B", captor.firstValue.id.category)
    }

    @Test
    fun `registerUser should not save if user is already subscribed to all categories`() {
        val userId = UUID.randomUUID()
        val request = RegisterRequest(userId, setOf("type1", "type2"))

        val catType1 = CategoryNotification(CategoryNotificationId("A", "type1"))
        val catType2 = CategoryNotification(CategoryNotificationId("B", "type2"))
        whenever(categoryNotificationRepository.findByIdNotificationType("type1")).thenReturn(catType1)
        whenever(categoryNotificationRepository.findByIdNotificationType("type2")).thenReturn(catType2)
        whenever(categoryNotificationRepository.findByIdNotificationTypeIn(setOf("type1", "type2")))
            .thenReturn(listOf(catType1, catType2))

        whenever(userRepository.existsByUserIdAndCategory(userId, "A")).thenReturn(true)
        whenever(userRepository.existsByUserIdAndCategory(userId, "B")).thenReturn(true)

        notificationService.registerUser(request)

        verify(userRepository, never()).save(any())
    }

    @Test
    fun `registerUser should only save distinct categories when multiple types map to the same category`() {
        val userId = UUID.randomUUID()
        val request = RegisterRequest(userId, setOf("type1", "type1_alias"))

        val catType1 = CategoryNotification(CategoryNotificationId("A", "type1"))
        val catType1Alias = CategoryNotification(CategoryNotificationId("A", "type1_alias"))
        whenever(categoryNotificationRepository.findByIdNotificationType("type1")).thenReturn(catType1)
        whenever(categoryNotificationRepository.findByIdNotificationType("type1_alias")).thenReturn(catType1Alias)
        whenever(categoryNotificationRepository.findByIdNotificationTypeIn(setOf("type1", "type1_alias")))
            .thenReturn(listOf(catType1, catType1Alias))

        whenever(userRepository.existsByUserIdAndCategory(userId, "A")).thenReturn(false)

        notificationService.registerUser(request)

        verify(userRepository, times(1)).save(any())
        val captor = argumentCaptor<User>()
        verify(userRepository).save(captor.capture())
        assertEquals("A", captor.firstValue.id.category)
    }

    @Test
    fun `sendNotification should successfully send notification for a subscribed user`() {
        val userId = UUID.randomUUID()
        val notificationType = "type1"
        val category = "A"
        val message = "Testing send notification happy flow"
        val notificationDto = NotificationDto(userId, notificationType, message)

        whenever(userRepository.existsByUserId(userId)).thenReturn(true)
        whenever(categoryNotificationRepository.findCategoryByType(notificationType)).thenReturn(category)
        // Ensure the category returned is UPPERCASE
        whenever(userRepository.existsByUserIdAndCategory(userId, category.uppercase())).thenReturn(true)

        notificationService.sendNotification(notificationDto)

        // Verify all required checks were performed exactly once
        verify(userRepository, times(1)).existsByUserId(userId)
        verify(categoryNotificationRepository, times(1)).findCategoryByType(notificationType)
        verify(userRepository, times(1)).existsByUserIdAndCategory(userId, category.uppercase())

    }

    @Test
    fun `sendNotification should throw UserNotFoundException when user does not exist`() {
        val userId = UUID.randomUUID()
        val notificationDto = NotificationDto(userId, "type1", "msg")

        whenever(userRepository.existsByUserId(userId)).thenReturn(false)

        assertThrows(UserNotFoundException::class.java) {
            notificationService.sendNotification(notificationDto)
        }
    }

    @Test
    fun `sendNotification should throw NotificationTypeNotFoundException when type does not exist`() {
        val userId = UUID.randomUUID()
        val notificationDto = NotificationDto(userId, "typeX", "msg")

        whenever(userRepository.existsByUserId(userId)).thenReturn(true)
        whenever(categoryNotificationRepository.findCategoryByType("typex")).thenReturn(null)

        assertThrows(NotificationTypeNotFoundException::class.java) {
            notificationService.sendNotification(notificationDto)
        }
    }

    @Test
    fun `sendNotification should throw UserNotSubscribedException when user not subscribed to category`() {
        val userId = UUID.randomUUID()
        val notificationDto = NotificationDto(userId, "type1", "msg")

        whenever(userRepository.existsByUserId(userId)).thenReturn(true)
        whenever(categoryNotificationRepository.findCategoryByType("type1")).thenReturn("A")
        whenever(userRepository.existsByUserIdAndCategory(userId, "A")).thenReturn(false)

        assertThrows(UserNotSubscribedException::class.java) {
            notificationService.sendNotification(notificationDto)
        }
    }
}
