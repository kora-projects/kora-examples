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
}

dependencies {
    implementation(platform("io.koraframework:kora-bom:${property("koraVersion")}"))
    ksp("io.koraframework:symbol-processors:${property("koraVersion")}")

    implementation("io.koraframework:validation-module")
    implementation("io.koraframework:http-client-ok")
    implementation("io.koraframework:json-common")
    implementation("io.koraframework:logging-logback")
    implementation("io.koraframework:config-hocon")

    testImplementation("org.json:json:20231013")
    testImplementation("org.skyscreamer:jsonassert:1.5.1")
    testImplementation("io.koraframework:test-junit5")
    testImplementation("io.goodforgod:testcontainers-extensions-mockserver:0.15.0")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

val openApiGeneratePetV2 = tasks.register<GenerateTask>("openApiGeneratePetV2") {
    generatorName.set("kora")
    group = "openapi tools"
    inputSpec.set("$projectDir/src/main/resources/openapi/petstoreV2.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi/petV2").get().asFile.absolutePath)
    val corePackage = "io.koraframework.kotlin.example.openapi.petV2"
    apiPackage.set("$corePackage.api")
    modelPackage.set("$corePackage.model")
    invokerPackage.set("$corePackage.invoker")
    configOptions.set(
        mapOf(
            "mode" to "kotlin-client",
            "clientConfigPrefix" to "httpClient.petV2",
        )
    )
}

val openApiGeneratePetV3 = tasks.register<GenerateTask>("openApiGeneratePetV3") {
    generatorName.set("kora")
    group = "openapi tools"
    inputSpec.set("$projectDir/src/main/resources/openapi/petstoreV3.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi/petV3").get().asFile.absolutePath)
    val corePackage = "io.koraframework.kotlin.example.openapi.petV3"
    apiPackage.set("$corePackage.api")
    modelPackage.set("$corePackage.model")
    invokerPackage.set("$corePackage.invoker")
    configOptions.set(
        mapOf(
            "mode" to "kotlin-client",
            "clientConfigPrefix" to "httpClient.petV3",
            "securityConfigPrefix" to "openapiAuth",
            "primaryAuth" to "apiKeyAuth",
        )
    )
}

kotlin.sourceSets.main {
    kotlin.srcDir(openApiGeneratePetV2.get().outputDir)
    kotlin.srcDir(openApiGeneratePetV3.get().outputDir)
}

tasks.matching { it.name.startsWith("ksp") }.configureEach {
    dependsOn(openApiGeneratePetV2, openApiGeneratePetV3)
}
tasks.compileKotlin {
    dependsOn(openApiGeneratePetV2, openApiGeneratePetV3)
}

application {
    applicationName = "application"
    mainClass.set("io.koraframework.kotlin.example.openapi.http.client.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.withType<JavaExec> {
    environment(
        "HTTP_CLIENT_PET_V2_URL" to (findProperty("httpClientUrlPetV2") ?: "http://localhost:8080"),
        "HTTP_CLIENT_PET_V3_URL" to (findProperty("httpClientUrlPetV3") ?: "http://localhost:8080"),
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
