plugins {
    id("java")
}

allprojects {
    group = "io.github.dracosomething"
    version = "1.0-SNAPSHOT"

    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation(platform("org.junit:junit-bom:5.10.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        implementation("org.apache.commons:commons-compress:1.26.0")
        implementation("org.apache.commons:commons-lang3:3.19.0")
        implementation("commons-io:commons-io:2.20.0")
        implementation("commons-codec:commons-codec:1.19.0")
        implementation("org.seleniumhq.selenium:selenium-java:4.36.0")
        implementation("org.seleniumhq.selenium:htmlunit-driver:4.13.0")
    }

    tasks.test {
        useJUnitPlatform()
    }
}

subprojects {
    apply(plugin = "java")

    dependencies {
        implementation(project(":"))
    }
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "io.github.dracosomething.Main",
        )
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.compileClasspath.get().map { if (it.isDirectory()) it else zipTree(it) })
}
