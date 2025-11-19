package de.dkb.api.codeChallenge.notification.model

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table( name = "category_notifications")
data class CategoryNotification(

    @EmbeddedId
    val id: CategoryNotificationId
) {
    constructor() : this(
        id = CategoryNotificationId("", "")
    )
}
