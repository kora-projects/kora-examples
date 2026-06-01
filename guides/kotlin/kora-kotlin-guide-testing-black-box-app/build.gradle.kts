import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    id("org.jetbrains.kotlin.jvm")
}

val koraBom: Configuration by configurations.creating
configurations {
    compileOnly.get().extendsFrom(koraBom)
    runtimeOnly.get().extendsFrom(koraBom)
    implementation.get().extendsFrom(koraBom)
    testCompileOnly.get().extendsFrom(koraBom)
    testRuntimeOnly.get().extendsFrom(koraBom)
    testImplementation.get().extendsFrom(koraBom)
}

dependencies {
    koraBom(platform("ru.tinkoff.kora:kora-parent:${property("koraVersion")}"))

    testImplementation(platform("org.junit:junit-bom:${property("junitVersion")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(project(":guides:kotlin:kora-kotlin-guide-database-jdbc-app"))
    testImplementation("ru.tinkoff.kora:test-junit5")
    testImplementation("org.json:json:20231013")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
    testImplementation("org.testcontainers:postgresql:1.21.4")
    testImplementation("org.testcontainers:testcontainers:1.21.4")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
    sourceSets.main { kotlin.srcDir("build/generated/ksp/main/kotlin") }
    sourceSets.test { kotlin.srcDir("build/generated/ksp/test/kotlin") }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

tasks.test {
    dependsOn(":guides:kotlin:kora-kotlin-guide-database-jdbc-app:distTar")
    inputs.file("../kora-kotlin-guide-database-jdbc-app/Dockerfile")
    inputs.file("../kora-kotlin-guide-database-jdbc-app/build/distributions/application.tar")

    jvmArgs(
        "-XX:+TieredCompilation",
        "-XX:TieredStopAtLevel=1",
    )

    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
