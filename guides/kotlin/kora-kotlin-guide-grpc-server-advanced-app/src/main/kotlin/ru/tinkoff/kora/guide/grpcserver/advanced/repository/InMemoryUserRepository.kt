package ru.tinkoff.kora.guide.grpcserver.advanced.repository

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.grpcserver.advanced.dto.UserResponse
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

    override fun update(id: String, name: String, email: String): Boolean {
        return users.computeIfPresent(id) { key, current -> UserResponse(key, name, email, current.createdAt) } != null
    }

    override fun deleteById(id: String): Boolean = users.remove(id) != null
}
