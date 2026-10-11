import io.github.lijinhong11.nexusmcpublisher.VersionTag
import java.nio.charset.StandardCharsets

import com.vanniktech.maven.publish.DeploymentValidation
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    java
    signing
    id("com.diffplug.spotless") version "8.0.0"
    id("io.github.lijinhong11.nexusmcpublisher") version "1.0.3"
    id("io.freefair.lombok") version "9.5.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "io.github.lijinhong11"
version = findProperty("version")!! as String

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.faststats.dev/releases")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    compileOnly("io.github.lijinhong11:MittelLib:1.4.8")

    compileOnly("org.bstats:bstats-bukkit:3.2.1")
    compileOnly("dev.faststats.metrics:bukkit:0.30.2")

    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:3.2.0")
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

mavenPublishing {
    publishToMavenCentral(automaticRelease = true, validateDeployment = DeploymentValidation.PUBLISHED)

    signAllPublications()

    coordinates("io.github.lijinhong11", "MittelRandomEvents", project.version.toString())

    pom {
        name.set(project.name)
        description.set("A random event engine")
        url.set("https://github.com/LinsMinecraftStudio/MittelRandomEvents")

        licenses {
            license {
                name.set("GPL 3.0 License")
                url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
            }
        }

        developers {
            developer {
                id.set("lijinhong11")
                name.set("Jinhong Li")
                email.set("tygfhk@outlook.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/LinsMinecraftStudio/MittelRandomEvents.git")
            developerConnection.set("scm:git:ssh://github.com:LinsMinecraftStudio/MittelRandomEvents.git")
            url.set("https://github.com/LinsMinecraftStudio/MittelRandomEvents")
        }
    }

    configureBasedOnAppliedPlugins(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Javadoc(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        sourcesJar = SourcesJar.Sources()
    )
}

signing {
    useGpgCmd()
}