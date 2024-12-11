plugins {
    kotlin("jvm") version "2.1.0"
    id("io.github.goooler.shadow") version "8.1.8"
}


group = "me.chancy"
version = "0.1-ALPHA"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.nexomc.com/releases")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("com.nexomc:nexo:0.4.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.jeff-media:custom-block-data:2.2.3")

}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        relocate("com.jeff_media.customblockdata", "me.chancy.xRpg.customblockdata")
    }

    val moveJarTask = task("moveJar") {
        doLast {
            val sourceJar = file("${buildDir}/libs/${project.name}-${project.version}-all.jar")
            val destinationDir = file("${System.getProperty("user.home")}/Documents/cutekitten03/plugins/")
            val destinationJar = file("${destinationDir}/${sourceJar.name}")
            if (sourceJar.exists()) {
                destinationDir.mkdirs()
                sourceJar.copyTo(destinationJar, overwrite = true)
            }
        }
    }

    build {
        finalizedBy(moveJarTask)
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}