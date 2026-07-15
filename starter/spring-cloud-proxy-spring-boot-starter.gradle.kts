plugins {
    kotlin("jvm")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:${libs.versions.springBoot.get()}"))
    api(platform(libs.springCloudDependencies))

    api(project(":spring-cloud-proxy-spring-boot-autoconfigure"))
    api("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
}
