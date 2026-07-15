package org.openprojectx.spring.cloud.proxy.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class SpringCloudProxyApplication

fun main(args: Array<String>) {

    runApplication<SpringCloudProxyApplication>(*args)
}