package de.dkb.api.codeChallenge.notification.exception

import java.util.UUID

class CategoryNotFoundException (
    userId: UUID,
    notificationTypes: Set<String>
    ) : NotificationException(
    "No categories found for user $userId with provided notification types: $notificationTypes"
)