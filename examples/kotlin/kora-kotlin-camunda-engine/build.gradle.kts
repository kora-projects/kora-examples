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

    implementation("io.koraframework:http-server-undertow")
    implementation("io.koraframework.experimental:camunda-engine-bpmn")
    implementation("io.koraframework.experimental:camunda-rest-undertow")
    implementation("io.koraframework:json-common")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("io.koraframework:database-jdbc")
    implementation("io.koraframework:logging-logback")
    implementation("io.koraframework:config-hocon")

    // mockito-kotlin 5.4.0 pins an older mockito-core whose Byte Buddy rejects Java 25 class files
    testImplementation("org.mockito:mockito-core:5.18.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("io.koraframework:test-junit5")
    testImplementation("io.goodforgod:testcontainers-extensions-postgres:0.13.1")
    testRuntimeOnly("com.h2database:h2:2.2.224")
    testImplementation("org.camunda.bpm:camunda-bpm-assert:7.21.0")
    testImplementation("org.assertj:assertj-core:3.26.0")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

application {
    applicationName = "application"
    mainClass.set("io.koraframework.kotlin.example.camunda.engine.ApplicationKt")
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
