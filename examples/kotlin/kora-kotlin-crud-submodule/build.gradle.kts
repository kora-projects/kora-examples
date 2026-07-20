import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm") version "1.9.25" apply false
    id("com.google.devtools.ksp") version "1.9.25-1.0.20" apply false
    id("jacoco")
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.google.devtools.ksp")
    apply(plugin = "jacoco")

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension> {
            jvmToolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
                vendor.set(JvmVendorSpec.ADOPTIUM)
            }
            sourceSets.named("main") { kotlin.srcDir("build/generated/ksp/main/kotlin") }
            sourceSets.named("test") { kotlin.srcDir("build/generated/ksp/test/kotlin") }
        }
    }

    val koraBom: Configuration by configurations.creating
    configurations {
        listOf("ksp", "kspTest", "compileOnly", "api", "implementation", "testImplementation").forEach { name ->
            named(name) { extendsFrom(koraBom) }
        }
    }

    dependencies {
        koraBom(platform("io.koraframework:kora-parent:${property("koraVersion")}"))
        add("ksp", "io.koraframework:symbol-processors")

        add("testImplementation", "org.json:json:20231013")
        add("testImplementation", "org.skyscreamer:jsonassert:1.5.1")
        add("testImplementation", "io.koraframework:test-junit5")
        add("testImplementation", "org.mockito.kotlin:mockito-kotlin:5.4.0")
    }

    val jacocoExcludeSet = setOf("**/generated/**", "**/Application*", "**/\$*")
    tasks.withType(Test::class) {
        jvmArgs("-XX:+TieredCompilation", "-XX:TieredStopAtLevel=1")
        useJUnitPlatform()
        failOnNoDiscoveredTests = false
        testLogging {
            showStandardStreams = true
            events("passed", "skipped", "failed")
            exceptionFormat = TestExceptionFormat.FULL
        }
        reports {
            html.required = false
            junitXml.required = false
        }
        exclude("**/\$*")
        jacoco { jacocoExcludeSet.forEach { exclude(it) } }
    }

    tasks.named("check") {
        dependsOn(tasks.named("jacocoTestReport"))
    }

    tasks.withType<JacocoReport> {
        reports {
            xml.required = true
            html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
        }
        classDirectories.setFrom(
            files(
                classDirectories.files.map { dir ->
                    fileTree(dir) { exclude(jacocoExcludeSet) }
                }
            )
        )
    }
}
