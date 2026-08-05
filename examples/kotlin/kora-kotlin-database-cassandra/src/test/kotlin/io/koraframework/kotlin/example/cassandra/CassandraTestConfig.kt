package io.koraframework.kotlin.example.cassandra

import io.goodforgod.testcontainers.extensions.scylla.ScyllaConnection
import io.koraframework.test.extension.junit5.KoraConfigModification

fun cassandraConfig(connection: ScyllaConnection): KoraConfigModification {
    val params = connection.params()
    return KoraConfigModification.ofSystemProperty("CASSANDRA_CONTACT_POINTS", params.contactPoint())
        .withSystemProperty("CASSANDRA_USER", params.username())
        .withSystemProperty("CASSANDRA_PASS", params.password())
        .withSystemProperty("CASSANDRA_DC", params.datacenter())
        .withSystemProperty("CASSANDRA_KEYSPACE", params.keyspace())
}
