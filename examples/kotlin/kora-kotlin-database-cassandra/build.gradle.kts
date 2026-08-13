import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("application")
    id("jacoco")
    kotlin("jvm") version ("2.4.10")
    id("com.google.devtools.ksp") version ("2.3.11")
}

dependencies {
    implementation(platform("io.koraframework:kora-bom:${property("koraVersion")}"))
    ksp("io.koraframework:symbol-processors:${property("koraVersion")}")

    implementation("io.koraframework:database-cassandra")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("io.koraframework:config-hocon")
    implementation("io.koraframework:logging-logback")

    testImplementation("io.koraframework:test-junit5")
    testImplementation("io.goodforgod:testcontainers-extensions-scylla:0.13.1")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

application {
    applicationName = "application"
    mainClass.set("io.koraframework.kotlin.example.cassandra.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.withType<JavaExec> {
    environment(
        "CASSANDRA_CONTACT_POINTS" to "${findProperty("cassandraHost") ?: "localhost"}:${findProperty("cassandraPort") ?: "9042"}",
        "CASSANDRA_USER" to (findProperty("cassandraUser") ?: "cassandra"),
        "CASSANDRA_PASS" to (findProperty("cassandraPassword") ?: "cassandra"),
        "CASSANDRA_DC" to (findProperty("cassandraDatacenter") ?: "datacenter1"),
        "CASSANDRA_KEYSPACE" to (findProperty("cassandraKeyspace") ?: "kora"),
    )
}

tasks.distTar { archiveFileName.set("application.tar") }
tasks.test {
    dependsOn("distTar")
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
    }
}
