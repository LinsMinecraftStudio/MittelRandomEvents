import io.github.lijinhong11.nexusmcpublisher.VersionTag
import java.nio.charset.StandardCharsets

plugins {
    id("java")
    id("com.diffplug.spotless") version "8.0.0"
    id("io.github.lijinhong11.nexusmcpublisher") version "1.0.3"
    id("io.freefair.lombok") version "9.5.0"
}

group = "io.github.lijinhong11"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    compileOnly("io.github.lijinhong11:MittelLib:1.3.3")
    compileOnly("org.bstats:bstats-bukkit:3.2.1")
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.3.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(mapOf("version" to project.version))
    }
}

spotless {
    java {
        importOrder()
        removeUnusedImports()
        palantirJavaFormat()
        formatAnnotations()
    }
}

nexusMCPublisher {
    resourceId.set("")
    versionTag.set(VersionTag.RELEASE)
    versionTitle = project.property("version") as String
    changelog.set(file("changelog.txt").readLines(StandardCharsets.UTF_8).joinToString("\n"))
    mcVersions.set(listOf("26.1", "26.1.1", "26.1.2", "26.2"))
    token = System.getenv("NEXUSMC_API_TOKEN")
}
