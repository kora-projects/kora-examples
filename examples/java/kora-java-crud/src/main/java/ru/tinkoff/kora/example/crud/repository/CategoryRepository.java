package ru.tinkoff.kora.example.crud.repository;

import java.util.Optional;
import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.JdbcRepository;
import ru.tinkoff.kora.example.crud.model.dao.PetCategory;

@Repository
public interface CategoryRepository extends JdbcRepository {

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE name = :name")
    Optional<PetCategory> findByName(String name);

    @Id
    @Query("INSERT INTO categories(name) VALUES (:categoryName)")
    long insert(String categoryName);

    @Query("DELETE FROM categories WHERE id = :id")
    void deleteById(long id);
}
