package org.openprojectx.spring.cloud.proxy.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("spring.cloud.proxy.dynamic")
data class DynamicProxyProperties(
    /** Enables the dynamic proxy route. */
    var enabled: Boolean = true,
    /** Public path under which dynamic destinations are accepted. */
    var pathPrefix: String = "/dynamic-proxy",
    /** URI schemes that callers may select. */
    var allowedSchemes: Set<String> = setOf("http", "https"),
)
