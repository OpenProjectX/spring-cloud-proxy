plugins {
    id("buildsrc.convention.kotlin-jvm")
    id("buildsrc.convention.spring-kotlin")
}


dependencies {

    implementation(project(":spring-cloud-proxy-spring-boot-starter"))

}