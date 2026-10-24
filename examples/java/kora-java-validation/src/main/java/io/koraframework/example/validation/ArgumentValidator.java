package io.koraframework.example.validation;

import org.jspecify.annotations.Nullable;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Root;
import io.koraframework.validation.common.annotation.*;

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
