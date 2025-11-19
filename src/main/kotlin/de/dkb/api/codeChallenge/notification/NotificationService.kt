package de.dkb.api.codeChallenge.notification

import de.dkb.api.codeChallenge.notification.exception.CategoryNotFoundException
import de.dkb.api.codeChallenge.notification.exception.NotificationException
import de.dkb.api.codeChallenge.notification.exception.NotificationTypeAlreadyExistsException
import de.dkb.api.codeChallenge.notification.exception.NotificationTypeNotFoundException
import de.dkb.api.codeChallenge.notification.model.*
import de.dkb.api.codeChallenge.notification.repository.CategoryNotificationRepository
import de.dkb.api.codeChallenge.notification.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val userRepository: UserRepository,
    private val categoryNotificationRepository: CategoryNotificationRepository,) {

    @Transactional
    fun registerUser(registerRequest: RegisterRequest) {

        //Check if notification types are valid
        val normalizedTypes: Set<String> = registerRequest.notifications
            .map { it.trim().lowercase() }
            .map { type ->
                if (categoryNotificationRepository.findByIdNotificationType(type) == null) {
                    throw NotificationTypeNotFoundException(type)
                }
                type
            }
            .toSet()


        //Derives and stores categories from each notification type
        val categoriesToBeAdded: Set<String> = categoryNotificationRepository
            .findByIdNotificationTypeIn(normalizedTypes)
            .map { it.id.category }
            .toSet()

        if (categoriesToBeAdded.isEmpty()) {
            throw CategoryNotFoundException(
                userId = registerRequest.id,
                notificationTypes = normalizedTypes
            )
        }

        // Insert one row per category if it does not already exist
        categoriesToBeAdded.forEach { category ->
            val userIdComposite = UserSubscriptionKey(registerRequest.id, category)
            if (!userRepository.existsByUserIdAndCategory(registerRequest.id, category)) {
                userRepository.save(User(userIdComposite))
            }
        }
    }

    fun sendNotification(notificationDto: NotificationDto) {
        val subscriptions = userRepository.findAllByIdUserId(notificationDto.userId)

        val isSubscribed = subscriptions.any { it.id.category == notificationDto.notificationType.toString() }

        if (!isSubscribed) return

        println(
            "Sending notification of type ${notificationDto.notificationType} " +
                    "to user ${notificationDto.userId}: ${notificationDto.message}"
        )
    }

//    fun sendNotification(notificationDto: NotificationDto) =
//        userRepository.findById(notificationDto.userId)
//            .filter { it.notifications.contains(notificationDto.notificationType) }
//            .ifPresent { // Logic to send notification to user
//                println(
//                    "Sending notification of type ${notificationDto.notificationType}" +
//                            " to user ``````${it.id}: ${notificationDto.message}"
//                )
//            }

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