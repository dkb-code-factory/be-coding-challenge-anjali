package de.dkb.api.codeChallenge.notification.repository

import de.dkb.api.codeChallenge.notification.model.User
import de.dkb.api.codeChallenge.notification.model.UserSubscriptionKey
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, UserSubscriptionKey> {

    @Query(
        value = "SELECT EXISTS(SELECT 1 FROM users WHERE id = :userId AND category = :category)",
        nativeQuery = true
    )
    fun existsByUserIdAndCategory(userId: UUID, category: String): Boolean


    fun findAllByIdUserId(userId: UUID): List<User>



}