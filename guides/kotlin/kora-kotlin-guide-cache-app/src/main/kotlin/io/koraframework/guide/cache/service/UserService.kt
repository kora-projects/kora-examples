package io.koraframework.guide.cache.service

import io.koraframework.cache.annotation.CacheInvalidate
import io.koraframework.cache.annotation.CachePut
import io.koraframework.cache.annotation.Cacheable
import io.koraframework.common.annotation.Component
import io.koraframework.guide.cache.dto.UserRequest
import io.koraframework.guide.cache.dto.UserResponse
import io.koraframework.guide.cache.repository.UserRepository
import io.koraframework.http.server.common.response.HttpServerResponseException
import java.time.LocalDateTime

@Component
open class UserService(
    private val userRepository: UserRepository,
    private val userCache: UserCaffeineCache
) {
    fun createUser(request: UserRequest): UserResponse {
        val id = userRepository.save(request.name, request.email)
        val user = UserResponse(id, request.name, request.email, LocalDateTime.now())
        userCache.put(user.id, user)
        return user
    }

    @Cacheable(UserCaffeineCache::class)
    open fun getUser(id: String): UserResponse? = userRepository.findById(id)

    fun getUsers(page: Int, size: Int, sort: String): List<UserResponse> = userRepository.findAll()
        .sortedWith(getComparator(sort))
        .drop(page * size)
        .take(size)

    @CachePut(value = UserCaffeineCache::class, args = ["id"])
    open fun updateUser(id: String, request: UserRequest): UserResponse {
        if (!userRepository.update(id, request.name, request.email)) {
            throw HttpServerResponseException.of(404, "User not found")
        }
        return UserResponse(id, request.name, request.email, LocalDateTime.now())
    }

    @CacheInvalidate(UserCaffeineCache::class)
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
