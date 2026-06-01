package ru.tinkoff.kora.kotlin.example.openapi.http.server

import ru.tinkoff.kora.http.common.auth.PrincipalWithScopes

data class UserPrincipal(val name: String) : PrincipalWithScopes {
    override fun scopes(): Collection<String> = listOf("read", "write", "read_pets", "write_pets")
}
