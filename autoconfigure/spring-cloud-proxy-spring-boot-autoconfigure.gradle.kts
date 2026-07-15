plugins {
    id("buildsrc.convention.kotlin-jvm")
    `kotlin-kapt`
}


dependencies {

    api(project(":core"))

    val bootBom = platform("org.springframework.boot:spring-boot-dependencies:${libs.versions.springBoot.get()}")
    val cloudBom = platform(libs.springCloudDependencies)

    implementation(bootBom)
    implementation(cloudBom)
    kapt(bootBom)

    implementation("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.boot:spring-boot-starter")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.3")
    testImplementation("org.testcontainers:testcontainers:2.0.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")



}
