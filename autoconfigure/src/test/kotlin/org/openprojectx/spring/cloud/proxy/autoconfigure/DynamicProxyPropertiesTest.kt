package org.openprojectx.spring.cloud.proxy.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource

class DynamicProxyPropertiesTest {

    @Test
    fun `binds custom removed headers and header mappings`() {
        val source = MapConfigurationPropertySource(
            mapOf(
                "spring.cloud.proxy.dynamic.removed-request-headers[0]" to "X-Internal-Token",
                "spring.cloud.proxy.dynamic.removed-request-headers[1]" to "X-User-Session",
                "spring.cloud.proxy.dynamic.request-header-mappings[0].source" to "X-Target-Token",
                "spring.cloud.proxy.dynamic.request-header-mappings[0].target" to "X-Api-Token",
            ),
        )

        val properties = Binder(source)
            .bind("spring.cloud.proxy.dynamic", Bindable.of(DynamicProxyProperties::class.java))
            .get()

        assertThat(properties.removedRequestHeaders)
            .containsExactly("X-Internal-Token", "X-User-Session")
        assertThat(properties.requestHeaderMappings)
            .containsExactly(RequestHeaderMapping("X-Target-Token", "X-Api-Token"))
    }
}
