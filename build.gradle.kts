plugins {
    java
}

group = "com.person98"
version = "1.0.1"

repositories {
    mavenCentral()
    maven("https://repo.purpurmc.org/snapshots")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    // The target server API. Purpur 26.2 requires Java 25 bytecode.
    compileOnly("org.purpurmc.purpur:purpur-api:26.2.build.2620-stable")

    // Required server plugins declared in plugin.yml.
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("fr.minuskube.inv:smart-invs:1.2.7") {
        // SmartInvs still declares Spigot 1.8.8; Purpur already supplies that API.
        exclude(group = "org.spigotmc", module = "spigot-api")
    }

    // The original JAR embeds HikariCP (and its SLF4J API dependency).
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.slf4j:slf4j-api:1.8.0-beta4")

    testImplementation("org.purpurmc.purpur:purpur-api:26.2.build.2620-stable")
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 25
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
}

tasks.processResources {
    filteringCharset = "UTF-8"
}

tasks.jar {
    archiveFileName = "Quests-${project.version}.jar"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Reproduce the original Maven Shade output without adding another plugin.
    from({
        configurations.runtimeClasspath.get().map { dependency ->
            if (dependency.isDirectory) dependency else zipTree(dependency)
        }
    })

    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")

    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
}

tasks.build {
    dependsOn(tasks.jar)
}

tasks.test {
    useJUnitPlatform()
}
