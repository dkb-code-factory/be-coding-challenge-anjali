package de.dkb.api.codeChallenge.notification.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.*

@Embeddable
data class UserSubscriptionKey(
    @Column(name = "id", columnDefinition = "uuid")
    val userId: UUID,

    @Column(name = "category", nullable = false)
    val category: String
) : Serializable {
    constructor() : this(UUID.randomUUID(), "")
}
