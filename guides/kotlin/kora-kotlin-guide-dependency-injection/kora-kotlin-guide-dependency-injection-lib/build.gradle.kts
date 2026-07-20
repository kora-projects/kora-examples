val koraBom: Configuration by configurations.creating
configurations {
    compileOnly.get().extendsFrom(koraBom)
    api.get().extendsFrom(koraBom)
    implementation.get().extendsFrom(koraBom)
    testImplementation.get().extendsFrom(koraBom)
}

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("java-library")
}

dependencies {
    koraBom(platform("io.koraframework:kora-parent:${property("koraVersion")}"))

    api(project(":guides:kotlin:kora-kotlin-guide-dependency-injection:kora-kotlin-guide-dependency-injection-common"))

    implementation("io.koraframework:config-common")

    testImplementation(platform("org.junit:junit-bom:${property("junitVersion")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.koraframework:test-junit5")
}
