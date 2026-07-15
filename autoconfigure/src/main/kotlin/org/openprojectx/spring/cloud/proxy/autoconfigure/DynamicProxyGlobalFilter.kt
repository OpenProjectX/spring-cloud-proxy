package org.openprojectx.spring.cloud.proxy.autoconfigure

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.net.URI

internal class DynamicProxyGlobalFilter(
    private val properties: DynamicProxyProperties,
) : GlobalFilter, Ordered {

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val path = exchange.request.path.pathWithinApplication().value()
        val prefix = properties.normalizedPrefix()
        if (path != prefix && !path.startsWith("$prefix/")) return chain.filter(exchange)

        val destination = try {
            destination(path, exchange.request.uri.rawQuery)
        } catch (exception: IllegalArgumentException) {
            return badRequest(exchange, exception.message ?: "Invalid dynamic proxy destination")
        }

        exchange.attributes[GATEWAY_REQUEST_URL_ATTR] = destination
        return chain.filter(exchange)
    }

    private fun destination(path: String, rawQuery: String?): URI {
        val parts = path.removePrefix(properties.normalizedPrefix()).removePrefix("/").split("/", limit = 4)
        require(parts.size == 4 && parts[3].isNotEmpty()) {
            "Expected /{scheme}/{host}/{port}/{target-path}"
        }

        val (scheme, host, portText, target) = parts
        require(scheme.lowercase() in properties.allowedSchemes.map(String::lowercase)) {
            "Unsupported proxy scheme: $scheme"
        }
        require(host.isNotBlank()) { "Proxy host must not be blank" }
        val port = portText.toIntOrNull()
        require(port != null && port in 1..65535) { "Proxy port must be between 1 and 65535" }

        val authority = URI(scheme.lowercase(), null, host, port, null, null, null).toASCIIString()
        // target and rawQuery came from the already parsed request URI. Reusing their raw forms avoids
        // turning an encoded value such as %20 into %2520 on the downstream request.
        return URI.create(buildString {
            append(authority).append('/').append(target)
            if (rawQuery != null) append('?').append(rawQuery)
        })
    }

    private fun badRequest(exchange: ServerWebExchange, message: String): Mono<Void> {
        exchange.response.statusCode = HttpStatus.BAD_REQUEST
        val body = """{"error":"${message.jsonEscape()}"}""".toByteArray()
        exchange.response.headers.contentType = org.springframework.http.MediaType.APPLICATION_JSON
        return exchange.response.writeWith(Mono.just(exchange.response.bufferFactory().wrap(body)))
    }

    // RouteToRequestUrlFilter first installs the route's placeholder URL; this filter then replaces it.
    override fun getOrder(): Int = RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 1
}

internal fun DynamicProxyProperties.normalizedPrefix(): String =
    "/" + pathPrefix.trim().trim('/').also { require(it.isNotEmpty()) { "pathPrefix must not be blank" } }

private fun String.jsonEscape(): String = buildString {
    this@jsonEscape.forEach { character ->
        when (character) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(character)
        }
    }
}
