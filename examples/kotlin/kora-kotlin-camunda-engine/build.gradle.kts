import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("application")
    id("jacoco")
    kotlin("jvm") version ("1.9.25")
    id("com.google.devtools.ksp") version ("1.9.25-1.0.20")
}

val koraBom: Configuration by configurations.creating
configurations {
    ksp.get().extendsFrom(koraBom); compileOnly.get().extendsFrom(koraBom)
    api.get().extendsFrom(koraBom); implementation.get().extendsFrom(koraBom)
    testImplementation.get().extendsFrom(koraBom); kspTest.get().extendsFrom(koraBom)
}

dependencies {
    koraBom(platform("ru.tinkoff.kora:kora-parent:${property("koraVersion")}"))
    ksp("ru.tinkoff.kora:symbol-processors")
    kspTest("ru.tinkoff.kora:symbol-processors")

    implementation("ru.tinkoff.kora:http-server-undertow")
    implementation("ru.tinkoff.kora.experimental:camunda-engine-bpmn")
    implementation("ru.tinkoff.kora.experimental:camunda-rest-undertow")
    implementation("ru.tinkoff.kora:json-module")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("ru.tinkoff.kora:database-jdbc")
    implementation("ru.tinkoff.kora:logging-logback")
    implementation("ru.tinkoff.kora:config-hocon")

    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("ru.tinkoff.kora:test-junit5")
    testImplementation("io.goodforgod:testcontainers-extensions-postgres:0.13.1")
    testRuntimeOnly("com.h2database:h2:2.2.224")
    testImplementation("org.camunda.bpm:camunda-bpm-assert:7.21.0")
    testImplementation("org.assertj:assertj-core:3.26.0")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

application {
    applicationName = "application"
    mainClass.set("ru.tinkoff.kora.kotlin.example.camunda.engine.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.withType<JavaExec> {
    environment(
        "POSTGRES_JDBC_URL" to "jdbc:postgresql://${findProperty("postgresHost") ?: "localhost"}:${findProperty("postgresPort") ?: "5432"}/${
            findProperty(
                "postgresDatabase"
            ) ?: "postgres"
        }",
        "POSTGRES_USER" to (findProperty("postgresUser") ?: "postgres"),
        "POSTGRES_PASS" to (findProperty("postgresPassword") ?: "postgres"),
    )
}

tasks.distTar { archiveFileName.set("application.tar") }

val jacocoExcludeSet = setOf("**/generated/**", "**/Application*", "**/\$*")
tasks.test {
    dependsOn("distTar")
    jvmArgs("-XX:+TieredCompilation", "-XX:TieredStopAtLevel=1")
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
    exclude("**/\$*")
    jacoco { jacocoExcludeSet.forEach { exclude(it) } }
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
    classDirectories.setFrom(sourceSets.main.get().output.asFileTree.matching {
        jacocoExcludeSet.forEach { exclude(it) }
    })
}
