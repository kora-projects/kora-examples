package ru.tinkoff.kora.guide.json.service

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.json.dto.UserRequest
import ru.tinkoff.kora.guide.json.dto.UserResponse
import ru.tinkoff.kora.guide.json.dto.UserResult
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Component
class UserService {

    private val users = ConcurrentHashMap<String, UserResponse>()
    private val idGenerator = AtomicLong(1)

    fun createUser(request: UserRequest): UserResponse {
        val id = idGenerator.getAndIncrement().toString()
        val user = UserResponse(
            id = id,
            name = request.name,
            email = request.email,
            createdAt = LocalDateTime.now()
        )
        users[id] = user
        return user
    }

    fun getAllUsers(): List<UserResponse> = users.values.toList()

    fun getUser(id: String): UserResult {
        val user = users[id]
        return if (user != null) {
            UserResult.UserSuccess(UserResult.Status.OK, user)
        } else {
            UserResult.UserError(UserResult.Status.ERROR, "User not found with id: $id")
        }
    }
}
