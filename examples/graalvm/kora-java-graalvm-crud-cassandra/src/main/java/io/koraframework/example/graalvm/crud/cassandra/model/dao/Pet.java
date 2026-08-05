package io.koraframework.example.graalvm.crud.cassandra.model.dao;

import io.koraframework.database.cassandra.annotation.EntityCassandra;
import io.koraframework.database.common.annotation.Column;
import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Table;
import io.koraframework.json.common.annotation.Json;

@Json
@EntityCassandra
@Table("pets")
public record Pet(@Id @Column("id") long id,
                  @Column("name") String name,
                  @Column("status") Status status,
                  @Column("category") String category) {

    @Json
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
