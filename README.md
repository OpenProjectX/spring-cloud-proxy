# Spring Cloud Proxy

Extensible proxy utilities built on Spring Cloud Gateway. The first utility is a dynamic HTTP proxy that selects the downstream scheme, host, port, and path from the incoming URL.

The project targets Java 17, Spring Boot 4, and Spring Cloud 2025.1.1.

## Installation

Add the starter to a Spring Boot application.

### Gradle

```kotlin
dependencies {
    implementation("org.openprojectx.spring.cloud.proxy:spring-cloud-proxy-spring-boot-starter:0.1.0")
}
```

### Maven

```xml
<dependency>
    <groupId>org.openprojectx.spring.cloud.proxy</groupId>
    <artifactId>spring-cloud-proxy-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

The starter auto-configures Spring Cloud Gateway and the dynamic proxy route.

## Dynamic proxy

Requests use this format:

```text
http://proxy-server/dynamic-proxy/{scheme}/{host}/{port}/{target-path}
```

For example:

```text
POST http://localhost:8080/dynamic-proxy/https/api.example.com/443/v1/orders?active=true
```

The request method, body, permitted headers, query parameters, and encoded target path are forwarded. The target's status, headers, and body are returned to the caller.

Only `http` and `https` schemes are accepted by default. Invalid schemes, ports, and route formats return `400 Bad Request`.

## Request header isolation

Proxy credentials and cookies should not normally be exposed to the target. By default, the dynamic proxy removes:

- `Authorization`
- `Cookie`

A target-specific authorization value can coexist with the proxy's authorization header:

```http
Authorization: Bearer proxy-credential
X-Target-Authorization: Bearer target-credential
```

The target receives only:

```http
Authorization: Bearer target-credential
```

`X-Target-Authorization` is removed after it is mapped.

The removal and mapping policies are configurable:

```yaml
spring:
  cloud:
    proxy:
      dynamic:
        removed-request-headers:
          - Authorization
          - Cookie
          - X-Internal-Token
        request-header-mappings:
          - source: X-Target-Authorization
            target: Authorization
          - source: X-Target-Api-Key
            target: X-Api-Key
```

Mapped values are captured before removal, so a mapping source may also be listed in `removed-request-headers`. A mapping source header is never sent downstream under its original name.

## Configuration

| Property | Default | Description |
|---|---|---|
| `spring.cloud.proxy.dynamic.enabled` | `true` | Enables the dynamic proxy route. |
| `spring.cloud.proxy.dynamic.path-prefix` | `/dynamic-proxy` | Changes the public route prefix. |
| `spring.cloud.proxy.dynamic.allowed-schemes` | `http`, `https` | Schemes callers may select. |
| `spring.cloud.proxy.dynamic.removed-request-headers` | `Authorization`, `Cookie` | Request headers removed before routing. |
| `spring.cloud.proxy.dynamic.request-header-mappings` | `X-Target-Authorization` → `Authorization` | Source-to-target header mappings. |

Setting a collection property replaces its default collection. Repeat any defaults you want to retain.

## Run locally

```bash
./gradlew :app:bootRun
```

Example request:

```bash
curl --request POST \
  'http://localhost:8080/dynamic-proxy/https/httpbin.org/443/anything?source=proxy' \
  --header 'Authorization: Bearer proxy-credential' \
  --header 'X-Target-Authorization: Bearer target-credential' \
  --header 'Content-Type: application/json' \
  --data '{"hello":"gateway"}'
```

## Security

This utility intentionally lets a caller select a network destination, which can create server-side request forgery risk. Protect the proxy with authentication and authorization, expose it only to trusted clients, and restrict its network access to destinations callers are permitted to reach.

## Modules

| Module | Purpose |
|---|---|
| `core` | Shared foundation for future proxy utilities. |
| `spring-cloud-proxy-spring-boot-autoconfigure` | Dynamic proxy implementation and Spring Boot auto-configuration. |
| `spring-cloud-proxy-spring-boot-starter` | Consumer-facing starter and dependency management. |
| `app` | Runnable example application. |

## Tests

Docker is required for the integration test, which uses a generic Testcontainers container running `wiremock/wiremock:3.13.2`.

```bash
./gradlew build --no-configuration-cache
```

## License

Licensed under the Apache License 2.0. See [LICENSE](LICENSE).
