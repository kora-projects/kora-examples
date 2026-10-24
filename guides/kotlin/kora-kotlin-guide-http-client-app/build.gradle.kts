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
    implementation("io.koraframework:config-hocon")
    implementation("io.koraframework:http-client-common")
    implementation("io.koraframework:http-client-ok")
    implementation("io.koraframework:http-server-undertow")
    implementation("io.koraframework:json-common")
    implementation("io.koraframework:logging-logback")
    testImplementation(platform("org.junit:junit-bom:${property("junitVersion")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.koraframework:test-junit5")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
    testImplementation("org.testcontainers:testcontainers:1.21.4")
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


application {
    applicationName = "application"
    mainClass.set("io.koraframework.guide.httpclient.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.distTar {
    archiveFileName.set("application.tar")
}

tasks.test {
    dependsOn(":guides:kotlin:kora-kotlin-guide-http-server-app:distTar")
    inputs.file("../kora-kotlin-guide-http-server-app/Dockerfile")
    inputs.file("../kora-kotlin-guide-http-server-app/build/distributions/application.tar")

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
