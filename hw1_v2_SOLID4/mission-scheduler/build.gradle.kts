// mission-scheduler/build.gradle.kts
dependencies {
    // gRPC client for telemetry
    implementation("io.grpc:grpc-stub:1.62.2")
    implementation("io.grpc:grpc-protobuf:1.62.2")
    implementation("net.devh:grpc-client-spring-boot-starter:3.0.0.RELEASE")
    compileOnly("org.apache.tomcat:annotations-api:6.0.53")
}

// Add protobuf configuration same as telemetry-service
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.62.2"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
            }
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs(
                "build/generated/source/proto/main/java",
                "build/generated/source/proto/main/grpc"
            )
        }
    }
}