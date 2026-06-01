package ru.tinkoff.kora.guide.validation.repository

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.validation.dto.UserResponse
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Component
class InMemoryUserRepository : UserRepository {
    private val users = ConcurrentHashMap<String, UserResponse>()
    private val idGenerator = AtomicLong(1)

    override fun findAll(): List<UserResponse> = users.values.toList()

    override fun findById(id: String): UserResponse? = users[id]

    override fun save(name: String, email: String): String {
        val id = idGenerator.getAndIncrement().toString()
        users[id] = UserResponse(id, name, email, LocalDateTime.now())
        return id
    }

    override fun update(id: String, name: String, email: String): Boolean =
        users.computeIfPresent(id) { key, value -> UserResponse(key, name, email, value.createdAt) } != null

    override fun deleteById(id: String): Boolean = users.remove(id) != null
}
