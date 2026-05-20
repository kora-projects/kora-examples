import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    id("application")
    id("jacoco")
    kotlin("jvm") version ("1.9.25")
    id("com.google.devtools.ksp") version ("1.9.25-1.0.20")
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

    implementation("ru.tinkoff.kora.experimental:camunda-zeebe-worker")
    implementation("ru.tinkoff.kora:scheduling-jdk")
    implementation("ru.tinkoff.kora:logging-logback")
    implementation("ru.tinkoff.kora:config-hocon")
    implementation("ru.tinkoff.kora:json-module")

    testImplementation("org.mockito:mockito-core:5.18.0")
    testImplementation("ru.tinkoff.kora:test-junit5")
    testImplementation("io.camunda:zeebe-process-test-extension-testcontainer:8.6.7")
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
    mainClass.set("ru.tinkoff.kora.kotlin.example.camunda.zeebe.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.withType<JavaExec> {
    environment("ZEEBE_GRPC_URL", "http://${findProperty("zeebeHost") ?: "localhost"}:${findProperty("zeebePort") ?: "26500"}")
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
