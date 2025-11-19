package de.dkb.api.codeChallenge.notification.exception

class NotificationTypeNotFoundException(
    notificationType: String
) : NotificationException(
    "Unknown notification type: $notificationType"
)