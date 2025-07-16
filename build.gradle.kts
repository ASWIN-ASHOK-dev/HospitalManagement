plugins {
    id("java")
    id("application")
}

group = "org.hospitalmanagement"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("mysql:mysql-connector-java:8.0.33")
}

tasks.test {
    useJUnitPlatform()
}
tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.hospitalmanagement.Main"
    }
}
application {
    mainClass.set("org.hospitalmanagement.Main")
}
tasks.register<Jar>("compileall") {
    group = "build"
    manifest {
        attributes["Main-Class"] = "org.hospitalmanagement.Main"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })

    archiveClassifier.set("all")
}