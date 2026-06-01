package ru.tinkoff.kora.example.crud.repository;

import java.util.Optional;
import ru.tinkoff.kora.database.common.annotation.Id;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;
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
