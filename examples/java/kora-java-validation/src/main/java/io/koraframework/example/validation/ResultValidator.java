package io.koraframework.example.validation;

import org.jspecify.annotations.Nullable;
import java.util.UUID;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Root;
import io.koraframework.validation.common.annotation.NotBlank;
import io.koraframework.validation.common.annotation.Size;
import io.koraframework.validation.common.annotation.Valid;
import io.koraframework.validation.common.annotation.Validate;

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
