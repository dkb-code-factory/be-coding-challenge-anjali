package de.dkb.api.codeChallenge.notification.model

import jakarta.validation.constraints.NotBlank

data class AddNotificationTypeRequest(
    @field:NotBlank
    val category: String,
    @field:NotBlank
    val notificationType: String,
)



