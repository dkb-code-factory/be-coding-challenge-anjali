package de.dkb.api.codeChallenge.notification.model

import java.util.UUID
import jakarta.validation.constraints.NotNull

data class RegisterRequest(
    val id: UUID,
    @field:NotNull
    val notifications: Set<String>
)

