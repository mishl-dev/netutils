plugins {
    id("architectury-plugin") version "3.4.161"
    id("dev.architectury.loom") version "1.13.467" apply false
    kotlin("jvm") version "2.3.0" apply false
}

architectury {
    minecraft = "1.21.11"
}

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.terraformersmc.com/releases/")
    }
}

allprojects {
    group = "org.netutils"
    version = "1.0.0"
}

tasks.register("collectJars", Copy::class) {
    dependsOn(subprojects.map { it.tasks.named("build") })
    
    from(project(":fabric").tasks.named("remapJar").map { (it as org.gradle.jvm.tasks.Jar).archiveFile })
    from(project(":forge").tasks.named("remapJar").map { (it as org.gradle.jvm.tasks.Jar).archiveFile })
    
    into(layout.buildDirectory.dir("libs"))
    
    rename { "netutils-${it}" }
    
    doLast {
        println("Universal compilation complete. Jars collected in: ${layout.buildDirectory.dir("libs").get()}")
    }
}
