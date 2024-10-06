import xyz.jpenilla.runpaper.task.RunServer

group = "ee.mathiaskivi"
version = "1.1.4"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "2.3.0"
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
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
