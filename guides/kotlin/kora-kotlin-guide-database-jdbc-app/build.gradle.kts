import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
    id("application")
}

dependencies {
    implementation(platform("io.koraframework:kora-bom:${property("koraVersion")}"))

    ksp("io.koraframework:symbol-processors:${property("koraVersion")}")
    runtimeOnly("org.postgresql:postgresql:42.7.3")
    implementation("io.koraframework:config-hocon")
    implementation("io.koraframework:database-flyway")
    // с Flyway 10 поддержка конкретных СУБД вынесена в отдельные артефакты;
    // без этого приложение падает на старте: "Unsupported Database: PostgreSQL"
    implementation("org.flywaydb:flyway-database-postgresql:13.1.0")
    implementation("io.koraframework:database-jdbc")
    implementation("io.koraframework:http-server-undertow")
    implementation("io.koraframework:json-common")
    implementation("io.koraframework:logging-logback")
    testImplementation(platform("org.junit:junit-bom:${property("junitVersion")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.koraframework:test-junit5")
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

ksp {
    arg("kora.app.submodule.enabled", "true")
}


application {
    applicationName = "application"
    mainClass.set("io.koraframework.guide.databasejdbc.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.distTar {
    archiveFileName.set("application.tar")
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
