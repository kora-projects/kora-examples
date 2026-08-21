import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("io.koraframework:openapi-generator:${property("koraVersion")}")
    }
}

plugins {
    id("application")
    id("jacoco")
    kotlin("jvm") version ("2.4.10")
    id("com.google.devtools.ksp") version ("2.3.11")
    id("org.openapi.generator") version ("7.24.0")
    id("org.flywaydb.flyway") version ("8.4.2")
}

dependencies {
    implementation(platform("io.koraframework:kora-bom:${property("koraVersion")}"))

    ksp("io.koraframework:symbol-processors:${property("koraVersion")}")
    ksp("io.mcarle:konvert:4.5.1")

    implementation("io.koraframework:http-server-undertow")
    implementation("io.koraframework:http-client-ok")
    implementation("io.koraframework:database-jdbc")
    implementation("io.koraframework:micrometer-module")
    implementation("io.koraframework:json-common")
    implementation("io.koraframework:validation-module")
    implementation("io.koraframework:cache-caffeine")
    implementation("io.koraframework:resilient-kora")
    implementation("io.koraframework:config-hocon")
    implementation("io.koraframework:openapi-management")
    implementation("io.koraframework:logging-logback")
    implementation("io.mcarle:konvert-api:4.5.1")

    implementation("org.postgresql:postgresql:42.7.7")

    kspTest("io.koraframework:symbol-processors:${property("koraVersion")}")
    testImplementation("org.json:json:20231013")
    testImplementation("org.skyscreamer:jsonassert:1.5.1")

    testImplementation("io.mockk:mockk:1.14.11")
    testImplementation("io.koraframework:test-junit5")
    testImplementation("io.goodforgod:testcontainers-extensions-postgres:0.15.0")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}


application {
    applicationName = "application"
    mainClass.set("io.koraframework.kotlin.example.crud.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

val postgresHost: String by project
val postgresPort: String by project
val postgresDatabase: String by project
val postgresUser: String by project
val postgresPassword: String by project
tasks.withType<JavaExec> {
    environment(
        "POSTGRES_JDBC_URL" to "jdbc:postgresql://${postgresHost}:${postgresPort}/${postgresDatabase}",
        "POSTGRES_USER" to postgresUser,
        "POSTGRES_PASS" to postgresPassword,
    )
}

val openApiGenerateHttpServer = tasks.register<GenerateTask>("openApiGenerateHttpServer") {
    generatorName = "kora"
    group = "openapi tools"
    inputSpec.set(layout.projectDirectory.file("src/main/resources/openapi/http-server.yaml"))
    outputDir.set(layout.buildDirectory.dir("generated/openapi"))
    val corePackage = "io.koraframework.example.crud.openapi.http.server"
    apiPackage = "${corePackage}.api"
    modelPackage = "${corePackage}.model"
    invokerPackage = "${corePackage}.invoker"
    configOptions = mapOf(
        "mode" to "kotlin-server",
        "enableServerValidation" to "true",
    )
}
kotlin.sourceSets.main { kotlin.srcDir(layout.buildDirectory.dir("generated/openapi")) }
tasks.matching { it.name.startsWith("ksp") }.configureEach {
    dependsOn(openApiGenerateHttpServer)
}

ksp {
    arg("kora.app.submodule.enabled", "true") // Only for integration tests
}

//tasks.withType<KspTask> {
//}

tasks.distTar {
    archiveFileName.set("application.tar")
}

val jacocoExcludeSet = setOf("**/generated/**", "**/Application*", "**/\$*")
tasks.test {
    dependsOn("distTar")

    jvmArgs(
        "-XX:+TieredCompilation",
        "-XX:TieredStopAtLevel=1",
    )

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

    jacoco {
        jacocoExcludeSet.forEach { exclude(it) }
    }
}

flyway {
    url = "jdbc:postgresql://$postgresHost:$postgresPort/$postgresDatabase"
    user = postgresUser
    password = postgresPassword
    locations = arrayOf("classpath:db/migration")
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
