plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("application")
}

group = "com.github.redreaperlp.fourwinsserver"
version = ""

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        archiveFileName.set("FourWins.jar")
        manifest {
            attributes(
                "Main-Class" to "com.github.redreaperlp.fourwinsserver.Main"
            )
        }
    }
}

application {
    mainClass.set("com.github.redreaperlp.fourwinsserver.Main")
}
