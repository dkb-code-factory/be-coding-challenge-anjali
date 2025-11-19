package de.dkb.api.codeChallenge.notification.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class CategoryNotificationId (

    @Column(name = "category")
    val category: String = "",

    @Column(name = "notification_type")
    val notificationType: String = ""
) : Serializable