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
    koraBom(platform("ru.tinkoff.kora:kora-parent:${property("koraVersion")}"))

    api(project(":guides:kotlin:kora-kotlin-guide-dependency-injection:kora-kotlin-guide-dependency-injection-common"))

    implementation("ru.tinkoff.kora:config-common")

    testImplementation(platform("org.junit:junit-bom:${property("junitVersion")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("ru.tinkoff.kora:test-junit5")
}
