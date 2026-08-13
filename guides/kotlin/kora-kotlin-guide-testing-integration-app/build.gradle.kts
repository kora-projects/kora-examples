import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(platform("io.koraframework:kora-bom:${property("koraVersion")}"))

    ksp("io.koraframework:symbol-processors:${property("koraVersion")}")
    kspTest("io.koraframework:symbol-processors:${property("koraVersion")}")
    testRuntimeOnly("org.postgresql:postgresql:42.7.3")
    testImplementation(platform("org.junit:junit-bom:${property("junitVersion")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(project(":guides:kotlin:kora-kotlin-guide-database-jdbc-app"))
    testImplementation("io.koraframework:config-hocon")
    testImplementation("io.koraframework:database-flyway")
    // flyway-core 13 не содержит поддержки конкретных СУБД, иначе Flyway падает
    // с "Unsupported Database: PostgreSQL"
    testImplementation("org.flywaydb:flyway-database-postgresql:13.1.0")
    testImplementation("io.koraframework:database-jdbc")
    testImplementation("io.koraframework:http-client-common")
    testImplementation("io.koraframework:http-server-undertow")
    testImplementation("io.koraframework:json-common")
    testImplementation("io.koraframework:logging-logback")
    testImplementation("io.koraframework:test-junit5")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
    testImplementation("org.testcontainers:postgresql:1.21.4")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
    sourceSets.main { kotlin.srcDir("build/generated/ksp/main/kotlin") }
    sourceSets.test { kotlin.srcDir("build/generated/ksp/test/kotlin") }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

tasks.test {
    filter {
        excludeTestsMatching("*${'$'}*")
        excludeTestsMatching("*TestApplication")
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
