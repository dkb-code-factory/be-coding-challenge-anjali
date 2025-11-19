package de.dkb.api.codeChallenge.notification.exception

import java.util.UUID

class UserNotFoundException(userId: UUID) :
    NotificationException("User not found: $userId")



