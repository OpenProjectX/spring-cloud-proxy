package org.openprojectx.spring.cloud.proxy.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [DynamicProxyTestApplication::class],
)
class DynamicProxyIntegrationTest {

    @LocalServerPort
    private var serverPort: Int = 0

    @BeforeEach
    fun configureWireMock() {
        val mapping = """
            {
              "request": {
                "method": "POST",
                "urlPath": "/target%20host-api/nested",
                "queryParameters": { "q": { "equalTo": "spring cloud" } },
                "headers": {
                  "X-Proxy-Test": { "equalTo": "forwarded" },
                  "Authorization": { "equalTo": "Bearer target-credential" },
                  "Cookie": { "absent": true },
                  "X-Target-Authorization": { "absent": true }
                },
                "bodyPatterns": [{ "equalToJson": { "hello": "gateway" } }]
              },
              "response": {
                "status": 201,
                "headers": { "Content-Type": "application/json" },
                "jsonBody": { "proxied": true }
              }
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder(wireMockUri("/__admin/mappings"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(mapping))
            .build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        assertThat(response.statusCode()).isEqualTo(201)
    }

    @Test
    fun `proxies method path query headers body and response`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$serverPort").build()

        client.post()
            .uri { uri ->
                uri.path("/dynamic-proxy/http/${wireMock.host}/${wireMock.getMappedPort(8080)}/target host-api/nested")
                    .queryParam("q", "spring cloud")
                    .build()
            }
            .header("X-Proxy-Test", "forwarded")
            .header(HttpHeaders.AUTHORIZATION, "Bearer proxy-credential")
            .header(HttpHeaders.COOKIE, "proxy-session=secret")
            .header("X-Target-Authorization", "Bearer target-credential")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"hello":"gateway"}""")
            .exchange()
            .expectStatus().isCreated
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody().json("""{"proxied":true}""")
    }

    @Test
    fun `rejects unsupported schemes before routing`() {
        WebTestClient.bindToServer().baseUrl("http://localhost:$serverPort").build()
            .get()
            .uri("/dynamic-proxy/ftp/example.com/21/file")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.error").isEqualTo("Unsupported proxy scheme: ftp")
    }

    private fun wireMockUri(path: String): URI =
        URI.create("http://${wireMock.host}:${wireMock.getMappedPort(8080)}$path")

    companion object {
        @Container
        @JvmStatic
        val wireMock = KGenericContainer(DockerImageName.parse("wiremock/wiremock:3.13.2"))
            .withExposedPorts(8080)
    }
}

class KGenericContainer(image: DockerImageName) : GenericContainer<KGenericContainer>(image)

@SpringBootConfiguration
@EnableAutoConfiguration
private class DynamicProxyTestApplication
