package ru.tinkoff.kora.guide.grpcserver.advanced.service

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.grpcserver.advanced.dto.UserRequest
import ru.tinkoff.kora.guide.grpcserver.advanced.dto.UserResponse

@Component
class UserStreamingService(
    private val userService: UserService
) {

    fun getAllUsers(): List<UserResponse> = userService.getUsers(0, Int.MAX_VALUE, "name")

    fun createUsers(requests: List<UserRequest>): List<UserResponse> = requests.map(userService::createUser)

    fun tryUpdateUser(id: String, request: UserRequest): UserResponse? {
        return try {
            userService.updateUser(id, request)
        } catch (e: UserNotFoundException) {
            null
        }
    }
}
