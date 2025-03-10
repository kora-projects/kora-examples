import com.google.devtools.ksp.gradle.KspTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

buildscript {
    dependencies {
        classpath("ru.tinkoff.kora:openapi-generator:${property("koraVersion")}")
    }
}

plugins {
    id("org.openapi.generator") version ("7.4.0")
    id("application")
    id("jacoco")
    id("java")
    kotlin("kapt") version ("1.9.10")
    kotlin("jvm") version ("1.9.10")
    id("com.google.devtools.ksp") version ("1.9.10-1.0.13")
    id("org.flywaydb.flyway") version ("8.4.2")
}

application {
    applicationName = "application"
    mainClass.set("ru.tinkoff.kora.kotlin.example.crud.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

kotlin {
    jvmToolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
    sourceSets.main { kotlin.srcDir("build/generated/ksp/main/kotlin") }
    sourceSets.test { kotlin.srcDir("build/generated/ksp/test/kotlin") }
    sourceSets.main { kotlin.srcDir("build/generated/openapi") }
    sourceSets.main { kotlin.srcDir("build/generated/source/kapt/main") }
}

val koraBom: Configuration by configurations.creating
configurations {
    ksp.get().extendsFrom(koraBom)
    compileOnly.get().extendsFrom(koraBom)
    api.get().extendsFrom(koraBom)
    implementation.get().extendsFrom(koraBom)
}

dependencies {
    koraBom(platform("ru.tinkoff.kora:kora-parent:${property("koraVersion")}"))

    kapt("org.mapstruct:mapstruct-processor:1.5.5.Final")
    ksp("ru.tinkoff.kora:symbol-processors")

    implementation("ru.tinkoff.kora:http-server-undertow")
    implementation("ru.tinkoff.kora:http-client-ok")
    implementation("ru.tinkoff.kora:database-jdbc")
    implementation("ru.tinkoff.kora:micrometer-module")
    implementation("ru.tinkoff.kora:json-module")
    implementation("ru.tinkoff.kora:validation-module")
    implementation("ru.tinkoff.kora:validation-common")
    implementation("ru.tinkoff.kora:cache-caffeine")
    implementation("ru.tinkoff.kora:resilient-kora")
    implementation("ru.tinkoff.kora:config-hocon")
    implementation("ru.tinkoff.kora:openapi-management")
    implementation("ru.tinkoff.kora:logging-logback")

    implementation("org.postgresql:postgresql:42.7.2")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.7.3")

    kspTest("ru.tinkoff.kora:symbol-processors")
    testImplementation("org.json:json:20231013")
    testImplementation("org.skyscreamer:jsonassert:1.5.1")

    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("ru.tinkoff.kora:test-junit5")
    testImplementation("io.goodforgod:testcontainers-extensions-postgres:0.12.2")
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
}

tasks.register("openApiGenerateHttpServer", GenerateTask::class) {
    generatorName = "kora"
    group = "openapi tools"
    inputSpec = "$projectDir/src/main/resources/openapi/http-server.yaml"
    outputDir = "$buildDir/generated/openapi"
    apiPackage = "ru.tinkoff.kora.example.crud.openapi.http.server.api"
    modelPackage = "ru.tinkoff.kora.example.crud.openapi.http.server.model"
    invokerPackage = "ru.tinkoff.kora.example.crud.openapi.http.server.invoker"
    configOptions = mapOf(
        "mode" to "kotlin-server",
        "enableServerValidation" to "true",
    )
}

ksp {
    allowSourcesFromOtherPlugins = true
    arg("kora.app.submodule.enabled", "true") // Only for integration tests
}
tasks.withType<KspTask> {
    dependsOn(tasks.named("kaptKotlin").get())
    tasks.named("kaptGenerateStubsKotlin").get()
}
tasks.withType<KotlinCompile>().configureEach {
    dependsOn(tasks.named("openApiGenerateHttpServer"))
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

tasks.test {
    dependsOn("distTar")

    jvmArgs(
        "-XX:+TieredCompilation",
        "-XX:TieredStopAtLevel=1",
    )

    exclude(listOf("**/\$*"))

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

flyway {
    url = "jdbc:postgresql://$postgresHost:$postgresPort/$postgresDatabase"
    user = postgresUser
    password = postgresPassword
    locations = arrayOf("classpath:db/migration")
}

tasks.distTar {
    archiveFileName.set("application.tar")
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}
