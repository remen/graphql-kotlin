import org.gradle.api.plugins.ExtensionAware
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.include
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.version
import org.jetbrains.kotlin.codegen.inline.initDefaultSourceMappingIfNeeded
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.junit.platform.gradle.plugin.EnginesExtension
import org.junit.platform.gradle.plugin.FiltersExtension
import org.junit.platform.gradle.plugin.JUnitPlatformExtension

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.0")
    }
}

plugins {
    kotlin("jvm") version "1.2.0"
    id("com.jfrog.bintray") version "1.8.0"
    `maven-publish`
    id("com.palantir.git-version") version "0.10.0"
}
apply {
    plugin("org.junit.platform.gradle.plugin")
}

val gitVersion : groovy.lang.Closure<Any> by extra

group = "io.github.remen"
version = gitVersion()

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}

// extension for configuration
fun JUnitPlatformExtension.filters(setup: FiltersExtension.() -> Unit) {
    when (this) {
        is ExtensionAware -> extensions.getByType(FiltersExtension::class.java).setup()
        else -> throw Exception("${this::class} must be an instance of ExtensionAware")
    }
}
fun FiltersExtension.engines(setup: EnginesExtension.() -> Unit) {
    when (this) {
        is ExtensionAware -> extensions.getByType(EnginesExtension::class.java).setup()
        else -> throw Exception("${this::class} must be an instance of ExtensionAware")
    }
}
configure<JUnitPlatformExtension> {
    filters {
        engines {
            includeClassNamePattern("spek")
        }
    }
}

// Create sources Jar from main kotlin sources
val sourcesJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles sources JAR"
    classifier = "sources"
    from(java.sourceSets["main"].allSource)
}

publishing {
    publications {
        create("default", MavenPublication::class.java) {
            from(components["java"])
            artifact(sourcesJar)
        }
    }
}

tasks {
    "publish" {
        dependsOn("bintrayUpload")
    }
}

bintray {
    user = "remen"
    key = System.getenv("BINTRAY_API_KEY")
    setPublications("default")
    pkg = PackageConfig().apply {
        repo = "maven"
        name = project.name
        userOrg = user
        setLicenses("MIT")
        vcsUrl = "https://github.com/remen/graphql-kotlin.git"
        setLabels("graphql", "kotlin")
        version = VersionConfig().apply {
            name = project.version.toString()
        }
        publish = true
    }
}

repositories {
    jcenter()
}

val JACKSON_VERSION = "2.9.2"
val SPEK_VERSION = "1.1.5"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("com.graphql-java", "graphql-java", "6.0")


    testImplementation("com.fasterxml.jackson.core:jackson-databind:$JACKSON_VERSION")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:$JACKSON_VERSION")
    testImplementation("org.assertj:assertj-core:3.8.0")
    testImplementation("org.slf4j:slf4j-simple:1.7.7")

    testImplementation("org.jetbrains.spek:spek-api:$SPEK_VERSION")
    testRuntime("org.jetbrains.spek:spek-junit-platform-engine:$SPEK_VERSION")
}

