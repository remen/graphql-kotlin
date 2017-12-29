import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.include
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
}
kotlin {
    experimental.coroutines = Coroutines.ENABLE
}

apply {
    plugin("org.junit.platform.gradle.plugin")
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
