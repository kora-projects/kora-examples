package io.koraframework.kotlin.example.openapi.http.server

import io.koraframework.http.common.auth.PrincipalWithScopes

data class UserPrincipal(val name: String) : PrincipalWithScopes {
    override fun scopes(): Collection<String> = listOf("read", "write", "read_pets", "write_pets")
}
