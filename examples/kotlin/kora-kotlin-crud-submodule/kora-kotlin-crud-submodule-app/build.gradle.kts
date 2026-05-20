import com.google.devtools.ksp.gradle.KspTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

buildscript {
    dependencies {
        classpath("ru.tinkoff.kora:openapi-generator:${property("koraVersion")}")
    }
}

plugins {
    id("application")
    id("jacoco")
    kotlin("jvm") version ("1.9.25")
    id("com.google.devtools.ksp") version ("1.9.25-1.0.20")
    id("org.openapi.generator") version ("7.14.0")
    id("org.flywaydb.flyway") version ("8.4.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
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

    implementation(project(":examples:kotlin:kora-kotlin-crud-submodule:kora-kotlin-crud-submodule-pet-api"))
    implementation(project(":examples:kotlin:kora-kotlin-crud-submodule:kora-kotlin-crud-submodule-vet-api"))
    implementation("ru.tinkoff.kora:http-server-undertow")
    implementation("ru.tinkoff.kora:config-hocon")
    implementation("ru.tinkoff.kora:logging-logback")
    implementation("ru.tinkoff.kora:json-module")
    implementation("ru.tinkoff.kora:micrometer-module")
    implementation("ru.tinkoff.kora:validation-module")
    implementation("ru.tinkoff.kora:openapi-management")
    implementation("org.postgresql:postgresql:42.7.7")

    testRuntimeOnly(project(":examples:kotlin:kora-kotlin-crud-submodule:kora-kotlin-crud-submodule-common"))
    testImplementation("org.json:json:20231013")
    testImplementation("org.skyscreamer:jsonassert:1.5.1")
    testImplementation("ru.tinkoff.kora:test-junit5")
    testImplementation("io.goodforgod:testcontainers-extensions-postgres:0.13.1")
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
    sourceSets.main { kotlin.srcDir("build/generated/ksp/main/kotlin") }
    sourceSets.test { kotlin.srcDir("build/generated/ksp/test/kotlin") }
}

application {
    applicationName = "application"
    mainClass.set("ru.tinkoff.kora.kotlin.example.submodule.app.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

val postgresHost: String by project
val postgresPort: String by project
val postgresDatabase: String by project
val postgresUser: String by project
val postgresPassword: String by project

tasks.withType<JavaExec> {
    environment(
        "POSTGRES_JDBC_URL" to "jdbc:postgresql://$postgresHost:$postgresPort/$postgresDatabase",
        "POSTGRES_USER" to postgresUser,
        "POSTGRES_PASS" to postgresPassword,
    )
}

val openApiGenerateHttpServer = tasks.register<GenerateTask>("openApiGenerateHttpServer") {
    generatorName.set("kora")
    group = "openapi tools"
    inputSpec.set("$projectDir/src/main/resources/openapi/http-server.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.absolutePath)
    val corePackage = "ru.tinkoff.kora.kotlin.example.submodule.openapi.http.server"
    apiPackage.set("$corePackage.api")
    modelPackage.set("$corePackage.model")
    invokerPackage.set("$corePackage.invoker")
    configOptions.set(
        mapOf(
            "mode" to "kotlin-server",
            "enableServerValidation" to "true",
        )
    )
}

kotlin.sourceSets.main { kotlin.srcDir(openApiGenerateHttpServer.get().outputDir) }
tasks.withType<KspTask>().configureEach {
    dependsOn(openApiGenerateHttpServer)
}
tasks.compileKotlin {
    dependsOn(openApiGenerateHttpServer)
}

ksp {
    arg("kora.app.submodule.enabled", "true")
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
