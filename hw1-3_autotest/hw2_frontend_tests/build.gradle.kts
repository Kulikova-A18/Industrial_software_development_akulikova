plugins {
    id("java")
    id("io.qameta.allure") version "4.0.2"
}

group = "autotest"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    testImplementation("net.java.dev.jna:jna:5.15.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.16")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("com.codeborne:selenide:7.7.1")

    testImplementation(platform("io.qameta.allure:allure-bom:2.29.1"))
    testImplementation("io.qameta.allure:allure-junit5")
    testImplementation("io.qameta.allure:allure-selenide")
    testImplementation("io.qameta.allure:allure-cucumber7-jvm:2.29.1")

    testImplementation("io.cucumber:cucumber-java:7.20.1")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.20.1")

    testImplementation("org.junit.platform:junit-platform-suite:1.11.4")
}


allure {
    version.set("2.35.1")
    adapter {
        aspectjWeaver.set(true)
        frameworks {
            junit5 {
                adapterVersion.set("2.35.1")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
}
