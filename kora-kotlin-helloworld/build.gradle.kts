import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("application")
    id("jacoco")
    kotlin("jvm") version ("1.9.25")
    id("com.google.devtools.ksp") version ("1.9.25-1.0.20")
}

kotlin {
    jvmToolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
    sourceSets.main { kotlin.srcDir("build/generated/ksp/main/kotlin") }
    sourceSets.test { kotlin.srcDir("build/generated/ksp/test/kotlin") }
}

val koraBom: Configuration by configurations.creating
configurations {
    ksp.get().extendsFrom(koraBom)
    compileOnly.get().extendsFrom(koraBom)
    api.get().extendsFrom(koraBom)
    implementation.get().extendsFrom(koraBom)
}

val koraVersion: String by project
dependencies {
    koraBom(platform("ru.tinkoff.kora:kora-parent:$koraVersion"))

    ksp("ru.tinkoff.kora:symbol-processors")

    implementation("ru.tinkoff.kora:http-server-undertow")
    implementation("ru.tinkoff.kora:json-module")
    implementation("ru.tinkoff.kora:config-hocon")
    implementation("ru.tinkoff.kora:logging-logback")

    testImplementation("ru.tinkoff.kora:test-junit5")
    testImplementation("org.skyscreamer:jsonassert:1.5.1")
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
}

application {
    applicationName = "application"
    mainClass.set("ru.tinkoff.kora.kotlin.example.helloworld.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

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

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
    classDirectories.setFrom(sourceSets.main.get().output.asFileTree.matching {
        jacocoExcludeSet.forEach { exclude(it) }
    })
}
