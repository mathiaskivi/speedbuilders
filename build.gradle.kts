import xyz.jpenilla.runpaper.task.RunServer

group = "ee.mathiaskivi"
version = "1.1.4"

plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "2.3.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    implementation("ee.mathiaskivi:speedbuilders-api:1.0.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

tasks.named<RunServer>("runServer") {
    minecraftVersion("1.21")
}

tasks.named<Jar>("jar") {
    doFirst {
        copy {
            from("src/main/resources/plugin.yml")
            into(layout.buildDirectory.dir("resources/main"))

            filter { line ->
                line.replace("%VERSION%", version.toString())
            }
        }
    }
}
