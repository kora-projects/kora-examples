package ru.tinkoff.kora.example.crud.model.dao;

import ru.tinkoff.kora.database.common.annotation.Column;
import ru.tinkoff.kora.database.common.annotation.Id;
import ru.tinkoff.kora.database.common.annotation.Table;
import ru.tinkoff.kora.database.jdbc.EntityJdbc;

@EntityJdbc
@Table("pets")
public record Pet(@Id @Column("id") long id,
                  @Column("name") String name,
                  @Column("status") Status status,
                  @Column("category_id") long categoryId) {

    public enum Status {

        AVAILABLE(0),
        PENDING(10),
        SOLD(20);

        public final int code;

        Status(int code) {
            this.code = code;
        }
    }
}
