package ru.tinkoff.kora.example.submodule.vet.repository;

import java.util.List;
import java.util.Optional;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.database.common.annotation.Id;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;
import ru.tinkoff.kora.example.submodule.vet.model.dao.Vet;

@Repository
public interface VetRepository extends JdbcRepository {

    @Query("SELECT %{return#selects} FROM %{return#table}")
    List<Vet> findAll();

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    Optional<Vet> findById(long id);

    @Id
    @Query("INSERT INTO %{entity#inserts -= id}")
    long insert(Vet entity);

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    void update(Vet entity);

    @Query("DELETE FROM vets WHERE id = :id")
    UpdateCount deleteById(long id);
}
