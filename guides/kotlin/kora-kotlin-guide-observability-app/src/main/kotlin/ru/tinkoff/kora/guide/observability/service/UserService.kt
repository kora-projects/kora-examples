package ru.tinkoff.kora.guide.observability.service

import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.observability.dto.UserRequest
import ru.tinkoff.kora.guide.observability.dto.UserResponse
import ru.tinkoff.kora.guide.observability.repository.UserRepository
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import java.time.LocalDateTime

@Component
class UserService(
    private val userRepository: UserRepository,
    private val metricsService: MetricsService
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun createUser(request: UserRequest): UserResponse {
        logger.info("Creating user with name={} and email={}", request.name, request.email)
        return metricsService.recordUserCreation {
            val id = userRepository.save(request.name, request.email)
            val user = UserResponse(id, request.name, request.email, LocalDateTime.now())
            logger.info("Created user with id={}", id)
            user
        }
    }

    fun getUser(id: String): UserResponse? {
        logger.debug("Retrieving user with id={}", id)
        return userRepository.findById(id)
    }

    fun getUsers(page: Int, size: Int, sort: String): List<UserResponse> {
        logger.debug("Listing users page={}, size={}, sort={}", page, size, sort)
        return userRepository.findAll()
            .sortedWith(getComparator(sort))
            .drop(page * size)
            .take(size)
    }

    fun updateUser(id: String, request: UserRequest): UserResponse {
        if (!userRepository.update(id, request.name, request.email)) {
            logger.warn("User with id={} was not found for update", id)
            throw HttpServerResponseException.of(404, "User not found")
        }
        logger.info("Updated user with id={}", id)
        return UserResponse(id, request.name, request.email, LocalDateTime.now())
    }

    fun deleteUser(id: String) {
        if (!userRepository.deleteById(id)) {
            logger.warn("User with id={} was not found for deletion", id)
            throw HttpServerResponseException.of(404, "User not found")
        }
        logger.info("Deleted user with id={}", id)
    }

    private fun getComparator(sort: String): Comparator<UserResponse> = when (sort.lowercase()) {
        "name" -> compareBy { it.name }
        "email" -> compareBy { it.email }
        "createdat" -> compareBy { it.createdAt }
        else -> compareBy { it.name }
    }
}
