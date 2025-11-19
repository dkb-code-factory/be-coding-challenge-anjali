package de.dkb.api.codeChallenge.notification.repository

import de.dkb.api.codeChallenge.notification.model.CategoryNotification
import de.dkb.api.codeChallenge.notification.model.CategoryNotificationId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CategoryNotificationRepository: JpaRepository<CategoryNotification, CategoryNotificationId> {

    fun findByIdNotificationType(notificationType: String): CategoryNotification?

    fun findByIdNotificationTypeIn(types: Set<String>): List<CategoryNotification>

    @Query("SELECT c.id.category FROM CategoryNotification c WHERE c.id.notificationType = :type")
    fun findCategoryByType(type: String): String?
}