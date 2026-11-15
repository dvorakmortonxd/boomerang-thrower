plugins {
    id("java")
}

group = "dev.dvorakmortonxd"
version = "0.0.1a"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// tells java which class has main(), so the jar can be started with "java -jar" or a double click
tasks.jar {
    manifest {
        attributes["Main-Class"] = "dev.dvorakmortonxd.Game"
    }
}
