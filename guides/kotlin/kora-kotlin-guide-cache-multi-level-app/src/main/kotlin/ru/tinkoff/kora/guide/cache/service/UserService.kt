package ru.tinkoff.kora.guide.cache.service

import ru.tinkoff.kora.cache.annotation.CacheInvalidate
import ru.tinkoff.kora.cache.annotation.CachePut
import ru.tinkoff.kora.cache.annotation.Cacheable
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.cache.dto.UserRequest
import ru.tinkoff.kora.guide.cache.dto.UserResponse
import ru.tinkoff.kora.guide.cache.repository.UserRepository
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import java.time.LocalDateTime

@Component
open class UserService(
    private val userRepository: UserRepository,
    private val userCaffeineCache: UserCaffeineCache,
    private val userRedisCache: UserRedisCache
) {
    fun createUser(request: UserRequest): UserResponse {
        val id = userRepository.save(request.name, request.email)
        val user = UserResponse(id, request.name, request.email, LocalDateTime.now())
        userCaffeineCache.put(user.id, user)
        userRedisCache.put(user.id, user)
        return user
    }

    @Cacheable(UserCaffeineCache::class)
    @Cacheable(UserRedisCache::class)
    open fun getUser(id: String): UserResponse? = userRepository.findById(id)

    fun getUsers(page: Int, size: Int, sort: String): List<UserResponse> = userRepository.findAll()
        .sortedWith(getComparator(sort))
        .drop(page * size)
        .take(size)

    @CachePut(value = UserCaffeineCache::class, parameters = ["id"])
    @CachePut(value = UserRedisCache::class, parameters = ["id"])
    open fun updateUser(id: String, request: UserRequest): UserResponse {
        if (!userRepository.update(id, request.name, request.email)) {
            throw HttpServerResponseException.of(404, "User not found")
        }
        return UserResponse(id, request.name, request.email, LocalDateTime.now())
    }

    @CacheInvalidate(UserCaffeineCache::class)
    @CacheInvalidate(UserRedisCache::class)
    open fun deleteUser(id: String) {
        if (!userRepository.deleteById(id)) {
            throw HttpServerResponseException.of(404, "User not found")
        }
    }

    private fun getComparator(sort: String): Comparator<UserResponse> = when (sort.lowercase()) {
        "name" -> compareBy { it.name }
        "email" -> compareBy { it.email }
        "createdat" -> compareBy { it.createdAt }
        else -> compareBy { it.name }
    }
}
