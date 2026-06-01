package ru.tinkoff.kora.example.openapi.http.server;

import java.util.Collection;
import java.util.List;
import ru.tinkoff.kora.http.common.auth.PrincipalWithScopes;

public record UserPrincipal(String name) implements PrincipalWithScopes {

    @Override
    public Collection<String> scopes() {
        return List.of("read", "write");
    }
}
