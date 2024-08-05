import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    `maven-publish`
    id("java")
}

val versionPrefix = "SNAPSHOT"

val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.M.d.HHmm"))!!
val versionNumber = "${currentDateTime}-${versionPrefix}"

group = "com.infinitehorizons"
version = versionNumber

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {

    // Lombok: Java boilerplate code reduction
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    // SLF4J: Logging
    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")

    // Logback: Logging implementation
    testImplementation("ch.qos.logback:logback-classic:1.5.6")

    // JJWT: JSON Web Token
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Junit 5: Testing

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0-RC1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.0-RC1")
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("org.junit.vintage:junit-vintage-engine:5.11.0-RC1")

    testImplementation("org.junit.platform:junit-platform-launcher:1.11.0-RC1")
    testImplementation("org.junit.platform:junit-platform-runner:1.11.0-RC1")

    testImplementation(platform("org.junit:junit-bom:5.10.3"))

    // Mockito: Mocking

    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
    testImplementation("org.mockito:mockito-core:5.12.0")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/InfiniteHorizons-Inc/Shared")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}

tasks.test {
    useJUnitPlatform()
}