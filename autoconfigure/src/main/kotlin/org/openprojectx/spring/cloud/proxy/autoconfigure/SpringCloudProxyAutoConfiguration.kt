package org.openprojectx.spring.cloud.proxy.autoconfigure

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(RouteLocator::class, GlobalFilter::class)
@ConditionalOnProperty(
    prefix = "spring.cloud.proxy.dynamic",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
@EnableConfigurationProperties(DynamicProxyProperties::class)
class SpringCloudProxyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = ["dynamicProxyRouteLocator"])
    fun dynamicProxyRouteLocator(
        builder: RouteLocatorBuilder,
        properties: DynamicProxyProperties,
    ): RouteLocator = builder.routes()
        .route("spring-cloud-proxy-dynamic") { route ->
            route.path("${properties.normalizedPrefix()}/**")
                // Replaced by DynamicProxyGlobalFilter after RouteToRequestUrlFilter runs.
                .uri("http://dynamic-proxy.invalid")
        }
        .build()

    @Bean
    @ConditionalOnMissingBean(name = ["dynamicProxyGlobalFilter"])
    internal fun dynamicProxyGlobalFilter(properties: DynamicProxyProperties): DynamicProxyGlobalFilter =
        DynamicProxyGlobalFilter(properties)
}
