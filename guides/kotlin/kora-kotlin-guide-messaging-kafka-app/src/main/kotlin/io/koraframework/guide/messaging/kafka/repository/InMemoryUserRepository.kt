package io.koraframework.guide.messaging.kafka.repository

import io.koraframework.common.annotation.Component
import io.koraframework.guide.messaging.kafka.dto.UserResponse
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryUserRepository : UserRepository {

    private val users = ConcurrentHashMap<String, UserResponse>()

    override fun findAll(): List<UserResponse> = users.values.toList()

    override fun findById(id: String): UserResponse? = users[id]

    override fun save(user: UserResponse) {
        users[user.id] = user
    }

    override fun update(id: String, name: String, email: String): Boolean {
        return users.computeIfPresent(id) { key, current -> UserResponse(key, name, email, current.createdAt) } != null
    }

    override fun deleteById(id: String): Boolean = users.remove(id) != null
}
