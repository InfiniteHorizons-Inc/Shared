import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


plugins {
    id("maven-publish")
    id("java")
}

val versionPrefix = "SNAPSHOT"

val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.M.d"))!!
val versionNumber = "${currentDateTime}-${versionPrefix}"

group = "com.infinitehorizons"
version = versionNumber

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

publishing {

    repositories {
        maven {
            name = "InfiniteHorizons Shared"
            url = uri("https://maven.pkg.github.com/InfiniteHorizons-Inc/Shared")
            credentials {
                username = project.findProperty("gpr.user") as String?
                password = project.findProperty("gpr.key") as String?
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}