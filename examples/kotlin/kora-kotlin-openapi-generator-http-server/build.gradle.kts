import com.google.devtools.ksp.gradle.KspTask
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
    kotlin("jvm") version ("1.9.25")
    id("com.google.devtools.ksp") version ("1.9.25-1.0.20")
    id("org.openapi.generator") version ("7.23.0")
}

val koraBom: Configuration by configurations.creating
configurations {
    ksp.get().extendsFrom(koraBom); compileOnly.get().extendsFrom(koraBom)
    api.get().extendsFrom(koraBom); implementation.get().extendsFrom(koraBom)
    testImplementation.get().extendsFrom(koraBom); kspTest.get().extendsFrom(koraBom)
}

dependencies {
    koraBom(platform("io.koraframework:kora-parent:${property("koraVersion")}"))
    ksp("io.koraframework:symbol-processors")
    kspTest("io.koraframework:symbol-processors")

    implementation("io.koraframework:validation-module")
    implementation("io.koraframework:http-server-undertow")
    implementation("io.koraframework:json-module")
    implementation("io.koraframework:logging-logback")
    implementation("io.koraframework:config-hocon")

    testImplementation("org.json:json:20231013")
    testImplementation("org.skyscreamer:jsonassert:1.5.1")
    testImplementation("io.koraframework:test-junit5")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

val openApiGeneratePetV2 = tasks.register<GenerateTask>("openApiGeneratePetV2") {
    generatorName.set("kora")
    group = "openapi tools"
    inputSpec.set("$projectDir/src/main/resources/openapi/petstoreV2.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi/petV2").get().asFile.absolutePath)
    val corePackage = "ru.tinkoff.kora.kotlin.example.openapi.petV2"
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

val openApiGeneratePetV3 = tasks.register<GenerateTask>("openApiGeneratePetV3") {
    generatorName.set("kora")
    group = "openapi tools"
    inputSpec.set("$projectDir/src/main/resources/openapi/petstoreV3.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi/petV3").get().asFile.absolutePath)
    val corePackage = "ru.tinkoff.kora.kotlin.example.openapi.petV3"
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

kotlin.sourceSets.main {
    kotlin.srcDir(openApiGeneratePetV2.get().outputDir)
    kotlin.srcDir(openApiGeneratePetV3.get().outputDir)
}

tasks.withType<KspTask>().configureEach {
    dependsOn(openApiGeneratePetV2, openApiGeneratePetV3)
}
tasks.compileKotlin {
    dependsOn(openApiGeneratePetV2, openApiGeneratePetV3)
}

application {
    applicationName = "application"
    mainClass.set("ru.tinkoff.kora.kotlin.example.openapi.http.server.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
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
