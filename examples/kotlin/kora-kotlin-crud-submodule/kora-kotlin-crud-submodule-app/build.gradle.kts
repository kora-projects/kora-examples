import com.google.devtools.ksp.gradle.KspTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

buildscript {
    dependencies {
        classpath("ru.tinkoff.kora:openapi-generator:${property("koraVersion")}")
    }
}

plugins {
    id("application")
    id("org.openapi.generator") version ("7.14.0")
    id("org.flywaydb.flyway") version ("8.4.2")
}

dependencies {
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

    kspTest("ru.tinkoff.kora:symbol-processors")

    testRuntimeOnly(project(":examples:kotlin:kora-kotlin-crud-submodule:kora-kotlin-crud-submodule-common"))
    testImplementation("io.goodforgod:testcontainers-extensions-postgres:0.13.1")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
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

tasks.test {
    dependsOn("distTar")
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

flyway {
    url = "jdbc:postgresql://$postgresHost:$postgresPort/$postgresDatabase"
    user = postgresUser
    password = postgresPassword
    locations = arrayOf("classpath:db/migration")
}
