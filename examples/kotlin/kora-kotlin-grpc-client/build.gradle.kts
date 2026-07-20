import com.google.protobuf.gradle.id
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("application")
    id("jacoco")
    kotlin("jvm") version ("1.9.25")
    id("com.google.devtools.ksp") version ("1.9.25-1.0.20")
    id("com.google.protobuf") version ("0.10.0")
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

    implementation("io.koraframework:grpc-client")
    implementation("io.grpc:grpc-protobuf:1.74.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("io.koraframework:logging-logback")
    implementation("io.koraframework:config-hocon")

    testImplementation("io.koraframework:test-junit5")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:3.25.3" }
    plugins {
        id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:1.74.0" }
    }
    generateProtoTasks {
        all().forEach { task -> task.plugins { id("grpc") } }
    }
}

sourceSets.main {
    java.srcDir(layout.buildDirectory.dir("generated/source/proto/main/grpc"))
    java.srcDir(layout.buildDirectory.dir("generated/source/proto/main/java"))
}

application {
    applicationName = "application"
    mainClass.set("ru.tinkoff.kora.kotlin.example.grpc.client.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.distTar { archiveFileName.set("application.tar") }
tasks.test {
    dependsOn("distTar", "generateProto")
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
    }
}
