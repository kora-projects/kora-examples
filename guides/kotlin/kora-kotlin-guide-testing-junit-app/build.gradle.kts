import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
}

val koraBom: Configuration by configurations.creating
configurations {
    ksp.get().extendsFrom(koraBom)
    compileOnly.get().extendsFrom(koraBom)
    runtimeOnly.get().extendsFrom(koraBom)
    implementation.get().extendsFrom(koraBom)
    testCompileOnly.get().extendsFrom(koraBom)
    kspTest.get().extendsFrom(koraBom)
    testRuntimeOnly.get().extendsFrom(koraBom)
    testImplementation.get().extendsFrom(koraBom)
}

dependencies {
    koraBom(platform("ru.tinkoff.kora:kora-parent:${property("koraVersion")}"))

    ksp("ru.tinkoff.kora:symbol-processors")
    kspTest("ru.tinkoff.kora:symbol-processors")
    testImplementation(platform("org.junit:junit-bom:${property("junitVersion")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(project(":guides:kotlin:kora-kotlin-guide-http-server-app"))
    testImplementation("ru.tinkoff.kora:config-hocon")
    testImplementation("ru.tinkoff.kora:http-server-undertow")
    testImplementation("ru.tinkoff.kora:json-module")
    testImplementation("ru.tinkoff.kora:logging-logback")
    testImplementation("ru.tinkoff.kora:test-junit5")
    testImplementation("org.mockito:mockito-core:5.12.0")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
    sourceSets.main { kotlin.srcDir("build/generated/ksp/main/kotlin") }
    sourceSets.test { kotlin.srcDir("build/generated/ksp/test/kotlin") }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

tasks.test {
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
