val koraBom: Configuration by configurations.creating
configurations {
    ksp.get().extendsFrom(koraBom)
    compileOnly.get().extendsFrom(koraBom)
    api.get().extendsFrom(koraBom)
    implementation.get().extendsFrom(koraBom)
    testImplementation.get().extendsFrom(koraBom)
}

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
    id("java-library")
}

dependencies {
    koraBom(platform("io.koraframework:kora-parent:${property("koraVersion")}"))

    ksp("io.koraframework:symbol-processors")

    api(project(":guides:kotlin:kora-kotlin-guide-dependency-injection:kora-kotlin-guide-dependency-injection-common"))

    implementation("io.koraframework:config-common")

    testImplementation(platform("org.junit:junit-bom:${property("junitVersion")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.koraframework:test-junit5")
}

kotlin {
    sourceSets.main { kotlin.srcDir("build/generated/ksp/main/kotlin") }
}
