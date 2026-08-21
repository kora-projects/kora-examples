import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("application")
    id("jacoco")
    kotlin("jvm") version "2.4.10"
    id("com.google.devtools.ksp") version "2.3.11"
}

dependencies {
    implementation(platform("io.koraframework:kora-bom:${property("koraVersion")}"))
    ksp("io.koraframework:symbol-processors:${property("koraVersion")}")

    implementation("io.koraframework:http-server-undertow")
    implementation("io.koraframework:database-jdbc")
    implementation("io.koraframework:database-flyway")
    implementation("io.koraframework:json-common")
    implementation("io.koraframework:validation-module")
    implementation("io.koraframework:config-hocon")
    implementation("io.koraframework:logging-logback")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("org.flywaydb:flyway-database-postgresql:13.1.0")

    testImplementation(platform("org.junit:junit-bom:${property("junitVersion")}"))
    testImplementation("io.koraframework:test-junit5")
    testImplementation("io.goodforgod:testcontainers-extensions-postgres:0.15.0")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

application {
    applicationName = "application"
    mainClass.set("io.koraframework.kotlin.example.petclinic.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.withType<JavaExec> {
    environment(
        "POSTGRES_JDBC_URL" to "jdbc:postgresql://${findProperty("postgresHost") ?: "localhost"}:${findProperty("postgresPort") ?: "5432"}/${findProperty("postgresDatabase") ?: "postgres"}",
        "POSTGRES_USER" to (findProperty("postgresUser") ?: "postgres"),
        "POSTGRES_PASS" to (findProperty("postgresPassword") ?: "postgres"),
    )
}

ksp {
    arg("kora.app.submodule.enabled", "true")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
    }
    reports {
        html.required = false
        junitXml.required = false
    }
}
