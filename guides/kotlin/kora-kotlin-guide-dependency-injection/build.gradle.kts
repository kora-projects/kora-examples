import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots")
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>("kotlin") {
            jvmToolchain {
                languageVersion.set(JavaLanguageVersion.of(24))
                vendor.set(JvmVendorSpec.ADOPTIUM)
            }
        }
    }

    plugins.withId("java") {
        extensions.configure<JavaPluginExtension>("java") {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(24))
                vendor.set(JvmVendorSpec.ADOPTIUM)
            }
        }
    }

    tasks.withType<Test>().configureEach {
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
}
