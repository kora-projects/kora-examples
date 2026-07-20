plugins {
    id("org.jetbrains.kotlin.jvm")
    id("java-library")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:${property("junitVersion")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.koraframework:test-junit5")
}
