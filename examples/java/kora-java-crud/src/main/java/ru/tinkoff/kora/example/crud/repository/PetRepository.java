package ru.tinkoff.kora.example.crud.repository;

import java.util.Optional;
import io.koraframework.database.common.UpdateCount;
import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.JdbcRepository;
import ru.tinkoff.kora.example.crud.model.dao.Pet;
import ru.tinkoff.kora.example.crud.model.dao.PetWithCategory;

@Repository
public interface PetRepository extends JdbcRepository {

    @Query("""
            SELECT p.id, p.name, p.status, p.category_id, c.name as category_name
            FROM pets p
            JOIN categories c on c.id = p.category_id
            WHERE p.id = :id
            """)
    Optional<PetWithCategory> findById(long id);

    @Id
    @Query("INSERT INTO %{entity#inserts -= id}")
    long insert(Pet entity);

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    void update(Pet entity);

    @Query("DELETE FROM pets WHERE id = :id")
    UpdateCount deleteById(long id);
}
