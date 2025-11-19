package de.dkb.api.codeChallenge.notification

import de.dkb.api.codeChallenge.notification.exception.NotificationException
import de.dkb.api.codeChallenge.notification.exception.NotificationTypeAlreadyExistsException
import de.dkb.api.codeChallenge.notification.model.*
import de.dkb.api.codeChallenge.notification.repository.CategoryNotificationRepository
import de.dkb.api.codeChallenge.notification.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val userRepository: UserRepository,
    private val categoryNotificationRepository: CategoryNotificationRepository,) {

    fun registerUser(user: User) = userRepository.save(user)

    fun sendNotification(notificationDto: NotificationDto) =
        userRepository.findById(notificationDto.userId)
            .filter { it.notifications.contains(notificationDto.notificationType) }
            .ifPresent { // Logic to send notification to user
                println(
                    "Sending notification of type ${notificationDto.notificationType}" +
                            " to user ``````${it.id}: ${notificationDto.message}"
                )
            }

    @Transactional
    fun addNotificationType(request: AddNotificationTypeRequest) {
        val normalizedCategory = request.category.trim().uppercase()
        val normalizedType = request.notificationType.trim().lowercase()

        if (normalizedType.isBlank()) {
            throw NotificationException("notificationType must not be blank")
        }

        val existingMapping = categoryNotificationRepository.findByIdNotificationType(normalizedType)

        if (existingMapping != null) {
            throw NotificationTypeAlreadyExistsException(
                notificationType = normalizedType,
                existingCategory = existingMapping.id.category,
                newCategory = normalizedCategory
            )
        }

        val id = CategoryNotificationId(
            category = normalizedCategory,
            notificationType = normalizedType,
        )

        categoryNotificationRepository.save(
            CategoryNotification(
                id = id,
            )
        )
    }
}