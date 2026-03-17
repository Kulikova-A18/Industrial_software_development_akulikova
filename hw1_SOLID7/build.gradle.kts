plugins {
    id("java")
    id("jacoco")
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.7.0")
    
    // AspectJ
    implementation("org.aspectj:aspectjrt:1.9.19")
    implementation("org.aspectj:aspectjweaver:1.9.19")
    
    // Spring Context для @Configuration и @EnableAspectJAutoProxy
    implementation("org.springframework:spring-context:5.3.30")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.required.set(true)
    }
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
    }
    test {
        java {
            srcDirs("src/test")
        }
    }
}