plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
    id("java-library")
}

dependencies {
    implementation(platform("io.koraframework:kora-bom:${property("koraVersion")}"))

    ksp("io.koraframework:symbol-processors:${property("koraVersion")}")
    api(project(":guides:kotlin:kora-kotlin-guide-dependency-injection:kora-kotlin-guide-dependency-injection-common"))
    implementation("io.koraframework:common")
    testImplementation(platform("org.junit:junit-bom:${property("junitVersion")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.koraframework:test-junit5")
}

kotlin {
    sourceSets.main { kotlin.srcDir("build/generated/ksp/main/kotlin") }
    sourceSets.test { kotlin.srcDir("build/generated/ksp/test/kotlin") }
}
