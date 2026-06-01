import com.google.protobuf.gradle.id
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
    id("application")
    id("com.google.protobuf") version "0.9.4"
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

    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    ksp("ru.tinkoff.kora:symbol-processors")
    implementation("ru.tinkoff.kora:config-hocon")
    implementation("ru.tinkoff.kora:grpc-server")
    implementation("ru.tinkoff.kora:logging-logback")
    implementation("io.grpc:grpc-protobuf:1.74.0")
    implementation("io.grpc:grpc-services:1.74.0")
    testCompileOnly("javax.annotation:javax.annotation-api:1.3.2")
    kspTest("ru.tinkoff.kora:symbol-processors")
    testImplementation(platform("org.junit:junit-bom:${property("junitVersion")}"))
    testImplementation("io.grpc:grpc-netty:1.74.0")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("ru.tinkoff.kora:test-junit5")
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

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:3.25.3" }
    plugins {
        id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:1.74.0" }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins { id("grpc") }
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc", "build/generated/source/proto/main/java")
        }
    }
}


application {
    applicationName = "application"
    mainClass.set("ru.tinkoff.kora.guide.grpcserver.advanced.ApplicationKt")
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
