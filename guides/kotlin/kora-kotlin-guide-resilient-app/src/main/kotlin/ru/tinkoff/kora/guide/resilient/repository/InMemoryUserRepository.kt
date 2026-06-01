package ru.tinkoff.kora.guide.resilient.repository

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.resilient.dto.UserResponse
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@Component
class InMemoryUserRepository : UserRepository {
    private val users = ConcurrentHashMap<String, UserResponse>()
    private val idGenerator = AtomicLong(1)
    private val getUserTransientFailures = ConcurrentHashMap<String, AtomicInteger>()
    private val updateInvocations = ConcurrentHashMap<String, AtomicInteger>()
    private val findAllInvocations = AtomicInteger()

    override fun findAll(): List<UserResponse> {
        findAllInvocations.incrementAndGet()
        if (users.values.any { it.name.contains("slow-list") }) {
            sleep(200)
            throw IllegalStateException("Synthetic list failure for resilience tests")
        }
        return users.values.toList()
    }

    override fun findById(id: String): UserResponse? {
        val user = users[id]
        if (user != null && user.name.contains("retry-user")) {
            val counter = getUserTransientFailures.computeIfAbsent(id) { AtomicInteger(2) }
            if (counter.getAndUpdate { current -> if (current > 0) current - 1 else 0 } > 0) {
                throw IllegalStateException("Synthetic transient read failure for resilience tests")
            }
        }
        return user
    }

    override fun save(name: String, email: String): String {
        if (name.contains("fallback-create")) {
            throw IllegalStateException("Synthetic create failure for resilience tests")
        }
        val id = idGenerator.getAndIncrement().toString()
        users[id] = UserResponse(id, name, email, LocalDateTime.now())
        return id
    }

    override fun update(id: String, name: String, email: String): Boolean {
        updateInvocations.computeIfAbsent(id) { AtomicInteger() }.incrementAndGet()
        if (name.contains("breaker-update")) {
            throw IllegalStateException("Synthetic update failure for resilience tests")
        }
        return users.computeIfPresent(id) { key, value -> UserResponse(key, name, email, value.createdAt) } != null
    }

    override fun deleteById(id: String): Boolean {
        val user = users[id]
        if (user != null && user.name.contains("slow-delete")) {
            sleep(200)
        }
        return users.remove(id) != null
    }

    fun updateInvocations(id: String): Int = updateInvocations.getOrDefault(id, AtomicInteger()).get()

    fun findAllInvocations(): Int = findAllInvocations.get()

    private fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("Operation interrupted", e)
        }
    }
}
