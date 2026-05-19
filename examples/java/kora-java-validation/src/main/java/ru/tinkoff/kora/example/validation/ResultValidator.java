package ru.tinkoff.kora.example.validation;

import jakarta.annotation.Nullable;
import java.util.UUID;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.annotation.Root;
import ru.tinkoff.kora.validation.common.annotation.NotBlank;
import ru.tinkoff.kora.validation.common.annotation.Size;
import ru.tinkoff.kora.validation.common.annotation.Valid;
import ru.tinkoff.kora.validation.common.annotation.Validate;

@Root
@Component
public class ResultValidator {

    @Valid
    public record User(@NotBlank String id,
                       @Size(min = 3, max = 6) String name,
                       @Nullable @Size(min = 1, max = 10) String status) {}

    @Valid
    @Validate
    public User create(String name, String status) {
        return new User(UUID.randomUUID().toString(), name, status);
    }
}
