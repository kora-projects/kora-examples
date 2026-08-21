import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("application")
    id("jacoco")
    kotlin("jvm") version ("2.4.10")
    id("com.google.devtools.ksp") version ("2.3.11")
    id("com.github.bjornvester.wsdl2java") version ("2.0.2")
}

dependencies {
    implementation(platform("io.koraframework:kora-bom:${property("koraVersion")}"))
    ksp("io.koraframework:symbol-processors:${property("koraVersion")}")

    implementation("io.koraframework:json-common")
    implementation("io.koraframework:http-client-ok")
    implementation("io.koraframework:soap-client")
    implementation("io.koraframework:logging-logback")
    implementation("io.koraframework:config-hocon")

    testImplementation("io.koraframework:test-junit5")
    testImplementation("io.goodforgod:testcontainers-extensions-mockserver:0.15.0")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

wsdl2java {
    cxfVersion.set("4.0.2")
    wsdlDir.set(layout.projectDirectory.dir("src/main/resources/wsdl"))
    useJakarta.set(true)
    markGenerated.set(true)
    verbose.set(false)
    packageName.set("io.koraframework.example.generated.soap")
    generatedSourceDir.set(layout.buildDirectory.dir("generated/sources/wsdl2java/java"))
    includesWithOptions.set(
        mapOf("**/simple-service.wsdl" to listOf("-wsdlLocation", "https://kora.tinkoff.ru/simple/service?wsdl"))
    )
}

sourceSets.main {
    java.srcDir(layout.buildDirectory.dir("generated/sources/wsdl2java/java"))
}

application {
    applicationName = "application"
    mainClass.set("io.koraframework.kotlin.example.soap.client.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.withType<JavaExec> {
    environment("SOAP_CLIENT_URL" to (findProperty("httpClientUrl") ?: "http://localhost:8080"))
}

tasks.distTar { archiveFileName.set("application.tar") }
tasks.test {
    dependsOn("distTar")
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
    }
}
