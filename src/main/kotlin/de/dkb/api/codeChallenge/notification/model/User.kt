package de.dkb.api.codeChallenge.notification.model

import jakarta.persistence.*
import java.io.Serializable
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @EmbeddedId
    val id: UserSubscriptionKey,
) {
    constructor() : this(UserSubscriptionKey(UUID.randomUUID(), ""))
}

