package de.dkb.api.codeChallenge.notification.exception

class NotificationTypeAlreadyExistsException (
    notificationType: String,
    existingCategory: String,
    newCategory: String
) : NotificationException(
    "Notification type '$notificationType' is already mapped to category '$existingCategory' " +
            "and cannot be added to '$newCategory'."
)