package de.dkb.api.codeChallenge.notification.exception

import java.util.UUID

class UserNotSubscribedException(
    userId: UUID,
    category: String?
) : NotificationException(
    "User $userId is not subscribed to category ${category ?: "unknown"}"
)



