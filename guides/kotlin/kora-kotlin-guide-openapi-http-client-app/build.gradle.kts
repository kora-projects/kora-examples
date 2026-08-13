import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec

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
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
    id("application")
    id("org.openapi.generator") version "7.24.0"
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
    implementation("io.koraframework:validation-module")
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

val openApiGenerateUsersHttpClient = tasks.register<GenerateTask>("openApiGenerateUsersHttpClient") {
    generatorName = "kora"
    group = "openapi tools"
    inputSpec.set(layout.projectDirectory.file("../kora-kotlin-guide-openapi-http-server-app/src/main/resources/openapi/user-http-server.yaml"))
    outputDir.set(layout.buildDirectory.dir("generated/user-http-server"))
    val corePackage = "io.koraframework.guide.openapi.httpclient.user"
    apiPackage = "${corePackage}.api"
    modelPackage = "${corePackage}.model"
    invokerPackage = "${corePackage}.invoker"
    configOptions = mapOf(
        "mode" to "kotlin-client",
        "clientConfigPrefix" to "httpClient",
    )
}

kotlin.sourceSets.main { kotlin.srcDir(openApiGenerateUsersHttpClient.get().outputDir) }
tasks.matching { it.name.startsWith("ksp") }.configureEach {
    dependsOn(openApiGenerateUsersHttpClient)
}


application {
    applicationName = "application"
    mainClass.set("io.koraframework.guide.openapi.httpclient.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.distTar {
    archiveFileName.set("application.tar")
}

tasks.test {
    dependsOn(":guides:kotlin:kora-kotlin-guide-openapi-http-server-app:distTar")
    inputs.file("../kora-kotlin-guide-openapi-http-server-app/Dockerfile")
    inputs.file("../kora-kotlin-guide-openapi-http-server-app/build/distributions/application.tar")

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
