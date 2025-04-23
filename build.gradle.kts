plugins {
    kotlin("jvm") version "2.1.10"
    id("com.gradleup.shadow") version "8.3.2"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    `maven-publish`
    java
}

group = "me.honkling"
version = "0.1.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://jitpack.io/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("com.github.honkling:commonlib:9742e51e7d")
    implementation("cc.ekblad:4koma:1.2.0")
}

kotlin {
    jvmToolchain(21)
}

tasks.build {
    dependsOn("shadowJar", "publishToMavenLocal")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.runServer {
    minecraftVersion("1.21.1")
}

tasks.jar {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
}

tasks.shadowJar {
    relocate("kotlin", "me.honkling.ruby.dependencies.kotlin")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "me.honkling"
            artifactId = "ruby"
            version = project.version.toString()

            from(components["java"])
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}