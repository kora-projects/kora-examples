package io.koraframework.guide.validation.repository;

import java.util.List;
import java.util.Optional;
import io.koraframework.guide.validation.dto.UserResponse;

public interface UserRepository {

    List<UserResponse> findAll();

    Optional<UserResponse> findById(String id);

    String save(String name, String email);

    boolean update(String id, String name, String email);

    boolean deleteById(String id);
}
