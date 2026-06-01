plugins {
    id("java-library")
}

dependencies {
    api(project(":examples:kotlin:kora-kotlin-crud-submodule:kora-kotlin-crud-submodule-common"))
    api("ru.tinkoff.kora:database-jdbc")
    api("ru.tinkoff.kora:cache-caffeine")
    api("ru.tinkoff.kora:resilient-kora")

    testImplementation("ru.tinkoff.kora:config-hocon")
    testImplementation("io.goodforgod:testcontainers-extensions-postgres:0.13.1")
}
