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

    implementation("ru.tinkoff.kora.experimental:camunda-zeebe-worker")
    implementation("ru.tinkoff.kora:scheduling-jdk")
    implementation("ru.tinkoff.kora:logging-logback")
    implementation("ru.tinkoff.kora:config-hocon")
    implementation("ru.tinkoff.kora:json-module")

    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("ru.tinkoff.kora:test-junit5")
    testImplementation("io.camunda:zeebe-process-test-extension-testcontainer:8.8.24")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

application {
    applicationName = "application"
    mainClass.set("ru.tinkoff.kora.kotlin.example.camunda.zeebe.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.withType<JavaExec> {
    environment(
        "ZEEBE_GRPC_URL",
        "http://${findProperty("zeebeHost") ?: "localhost"}:${findProperty("zeebePort") ?: "26500"}"
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
