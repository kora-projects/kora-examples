import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("application")
    id("jacoco")
    kotlin("jvm") version ("1.9.25")
    id("com.google.devtools.ksp") version ("1.9.25-1.0.20")
    id("com.github.bjornvester.wsdl2java") version ("2.0.2")
}

val koraBom: Configuration by configurations.creating
configurations {
    ksp.get().extendsFrom(koraBom); compileOnly.get().extendsFrom(koraBom)
    api.get().extendsFrom(koraBom); implementation.get().extendsFrom(koraBom)
    testImplementation.get().extendsFrom(koraBom); kspTest.get().extendsFrom(koraBom)
}

dependencies {
    koraBom(platform("io.koraframework:kora-parent:${property("koraVersion")}"))
    ksp("io.koraframework:symbol-processors")
    kspTest("io.koraframework:symbol-processors")

    implementation("io.koraframework:json-module")
    implementation("io.koraframework:http-client-jdk")
    implementation("io.koraframework:soap-client") {
        exclude(group = "jakarta.xml")
        exclude(group = "jakarta.jws")
        exclude(group = "jakarta.xml.ws")
        exclude(group = "jakarta.xml.bind")
        exclude(group = "org.glassfish.jaxb")
        exclude(group = "com.sun.activation")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.8.1")
    implementation("io.koraframework:logging-logback")
    implementation("io.koraframework:config-hocon")

    testImplementation("io.koraframework:test-junit5")
    testImplementation("io.goodforgod:testcontainers-extensions-mockserver:0.13.1")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

wsdl2java {
    cxfVersion.set("4.0.2")
    wsdlDir.set(layout.projectDirectory.dir("src/main/resources/wsdl"))
    useJakarta.set(true)
    markGenerated.set(true)
    verbose.set(false)
    packageName.set("ru.tinkoff.kora.example.generated.soap")
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
    mainClass.set("ru.tinkoff.kora.kotlin.example.soap.client.ApplicationKt")
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
