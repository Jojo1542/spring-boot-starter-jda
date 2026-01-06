plugins {
    `java-library`
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get().toInt())
    }
}

repositories {
    mavenCentral()
    maven("https://repo.triumphteam.dev/snapshots/")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}")
    }
}

dependencies {
    implementation(rootProject)
    implementation(libs.spring.dotenv)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Don't publish example module
tasks.matching { it.name.contains("publish", ignoreCase = true) }.configureEach {
    enabled = false
}
