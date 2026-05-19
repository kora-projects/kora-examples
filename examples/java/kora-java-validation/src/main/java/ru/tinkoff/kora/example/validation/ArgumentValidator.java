package ru.tinkoff.kora.example.validation;

import jakarta.annotation.Nullable;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.annotation.Root;
import ru.tinkoff.kora.validation.common.annotation.*;

@Root
@Component
public class ArgumentValidator {

    @Valid
    public record User(@NotBlank String id,
                       @Size(min = 3, max = 6) String name,
                       @Nullable String status) {}

    @Validate
    public int calculate(@Valid User user,
                         @Range(from = 1, to = 900) int weight,
                         @Pattern("ME\\d+") String code) {
        return Integer.parseInt(code.substring(2));
    }
}
