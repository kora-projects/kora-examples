plugins {
    id("java-library")
}

dependencies {
    api(project(":examples:kotlin:kora-kotlin-crud-submodule:kora-kotlin-crud-submodule-common"))
    api("io.koraframework:database-jdbc")
    api("io.koraframework:cache-caffeine")
    api("io.koraframework:resilient-kora")

    testImplementation("io.koraframework:config-hocon")
    testImplementation("io.goodforgod:testcontainers-extensions-postgres:0.13.1")
}
