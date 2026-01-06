plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management)
}

group = property("group") as String
version = property("version") as String

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get().toInt())
    }
    withSourcesJar()
    withJavadocJar()
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
    // Spring Boot
    api(libs.spring.boot.starter)
    annotationProcessor(libs.spring.boot.autoconfigure.processor)
    annotationProcessor(libs.spring.boot.configuration.processor)

    // JDA - Transitiva
    api(libs.jda)

    // Triumph CMDs - Transitiva para slash commands
    api(libs.triumph.cmds.jda.slash)

    // Testing
    testImplementation(libs.spring.boot.starter.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<Javadoc> {
    options {
        (this as StandardJavadocDocletOptions).apply {
            addStringOption("Xdoclint:none", "-quiet")
            encoding = "UTF-8"
        }
    }
}

// JitPack publishing configuration
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = "spring-boot-starter-jda"
            version = project.version.toString()

            pom {
                name = "Spring Boot Starter JDA"
                description = "Spring Boot Starter for JDA (Java Discord API) with Triumph CMDs support"
                url = "https://github.com/jojo1542/spring-boot-starter-jda"

                licenses {
                    license {
                        name = "Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                    }
                }

                developers {
                    developer {
                        id = "jojo1542"
                        name = "José Antonio Ponce Piñero"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/jojo1542/spring-boot-starter-jda.git"
                    developerConnection = "scm:git:ssh://github.com/jojo1542/spring-boot-starter-jda.git"
                    url = "https://github.com/jojo1542/spring-boot-starter-jda"
                }
            }
        }
    }
}
