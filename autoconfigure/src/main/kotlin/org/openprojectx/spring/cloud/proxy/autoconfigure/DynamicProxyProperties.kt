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
    /** Request headers that must never be forwarded to the dynamic destination. */
    var removedRequestHeaders: Set<String> = setOf("Authorization", "Cookie"),
    /** Headers that carry destination-specific values under a different public name. */
    var requestHeaderMappings: List<RequestHeaderMapping> = listOf(
        RequestHeaderMapping("X-Target-Authorization", "Authorization"),
    ),
)

data class RequestHeaderMapping(
    var source: String = "",
    var target: String = "",
)
