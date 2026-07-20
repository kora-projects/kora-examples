import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec

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

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
    id("application")
}

dependencies {
    koraBom(platform("io.koraframework:kora-parent:${property("koraVersion")}"))

    ksp("io.koraframework:symbol-processors")
    implementation(project(":guides:kotlin:kora-kotlin-guide-dependency-injection:kora-kotlin-guide-dependency-injection-common"))
    implementation(project(":guides:kotlin:kora-kotlin-guide-dependency-injection:kora-kotlin-guide-dependency-injection-lib"))
    implementation(project(":guides:kotlin:kora-kotlin-guide-dependency-injection:kora-kotlin-guide-dependency-injection-submodule"))
    implementation("io.koraframework:config-hocon")
    implementation("io.koraframework:logging-logback")
    kspTest("io.koraframework:symbol-processors")
    testImplementation(platform("org.junit:junit-bom:${property("junitVersion")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.koraframework:test-junit5")
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

application {
    applicationName = "application"
    mainClass.set("ru.tinkoff.kora.guide.dependencyinjection.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.distTar {
    archiveFileName.set("application.tar")
}
