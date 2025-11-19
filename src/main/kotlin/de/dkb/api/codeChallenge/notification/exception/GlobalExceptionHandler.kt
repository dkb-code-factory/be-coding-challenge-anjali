package de.dkb.api.codeChallenge.notification.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    private fun buildResponseEntity(status: HttpStatus, message: String): ResponseEntity<ErrorResponse> =
        ResponseEntity(
            ErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = message
            ),
            status
        )

    @ExceptionHandler(NotificationTypeAlreadyExistsException::class)
    fun handleNotificationTypeAlreadyExists(ex: NotificationTypeAlreadyExistsException) =
        buildResponseEntity(HttpStatus.CONFLICT, ex.message ?: "Notification type already exists")

    @ExceptionHandler(NotificationException::class)
    fun handleGenericNotificationException(ex: NotificationException) =
        buildResponseEntity(HttpStatus.BAD_REQUEST, ex.message ?: "Notification error")

    @ExceptionHandler(CategoryNotFoundException::class)
    fun handleCategoryNotFound(ex: CategoryNotFoundException) =
        buildResponseEntity(HttpStatus.BAD_REQUEST, ex.message ?: "No categories found")

    @ExceptionHandler(NotificationTypeNotFoundException::class)
    fun handleNotificationTypeNotFound(ex: NotificationTypeNotFoundException) =
        buildResponseEntity(HttpStatus.BAD_REQUEST, ex.message ?: "Notification type not found")

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException) =
        buildResponseEntity(HttpStatus.NOT_FOUND, ex.message ?: "User not found")

    @ExceptionHandler(UserNotSubscribedException::class)
    fun handleUserNotSubscribed(ex: UserNotSubscribedException) =
        buildResponseEntity(HttpStatus.BAD_REQUEST, ex.message ?: "User not subscribed to category")

}