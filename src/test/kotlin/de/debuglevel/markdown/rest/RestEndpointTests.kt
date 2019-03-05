package de.debuglevel.markdown.rest

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.jsonBody
import de.debuglevel.markdown.domain.markdown.HtmlConverterTests
import de.debuglevel.microservices.utils.spark.SparkTestUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import spark.Spark.awaitInitialization
import java.nio.charset.Charset
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RestEndpointTests {

    init {
        val restEndpoint = RestEndpoint()
        restEndpoint.start(arrayOf())

        awaitInitialization()
    }

    @AfterAll
    fun stopServer() {
        SparkTestUtils.awaitShutdown()
    }

    @Test
    fun `server listens on default port`() {
        // Arrange

        // Act
        //val response = khttp.get("http://localhost:4567/")
        val (request, response, result) =
            Fuel.get("http://localhost:4567/")
                .responseString()

        // Assert
        // HTTP Codes begin from "100". So something from 100 and above was probably a response to a HTTP request
        assertThat(response.statusCode).isGreaterThanOrEqualTo(100)
    }

    private fun String.toBase64(): String {
        return Base64.getEncoder().encode(this.toByteArray()).toString(Charset.defaultCharset())
    }

    private fun getJsonWithOneFile(testData: HtmlConverterTests.MarkdownTestData): String {
        val base64 = testData.value.toBase64()
        val json = """
                        {
                            "files":
                            [
                                {
                                    "name": "test.md",
                                    "base64data": "$base64"
                                }
                            ]
                        }
                    """.trimIndent()
        return json
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class `valid requests` {
        @ParameterizedTest
        @MethodSource("markdownToPlaintextProvider")
        fun `sends UUID on POST`(testData: HtmlConverterTests.MarkdownTestData) {
            // Arrange
            val json = getJsonWithOneFile(testData)

            // Act
            val (postRequest, postResponse, postResult) =
                Fuel.post("http://localhost:4567/documents/")
                    .jsonBody(json)
                    .responseString()
            val (uuid, postError) = postResult

            // Assert
            assertThat(postResponse.statusCode).isEqualTo(201)
            assertThat(postResponse.headers["Content-Type"]).contains("text/plain")
            assertThatCode { UUID.fromString(uuid) }.doesNotThrowAnyException()
        }

        @ParameterizedTest
        @MethodSource("markdownToPlaintextProvider")
        fun `converts to Plaintext`(testData: HtmlConverterTests.MarkdownTestData) {
            // Arrange
            val json = getJsonWithOneFile(testData)
            val (postRequest, postResponse, postResult) =
                Fuel.post("http://localhost:4567/documents/")
                    .jsonBody(json)
                    .responseString()
            val (uuid, postError) = postResult

            // Act
            val (getRequest, getResponse, getResult) =
                Fuel.get("http://localhost:4567/documents/$uuid")
                    .header(Headers.ACCEPT, "text/plain")
                    .responseString()

            // Assert
            assertThat(getResponse.statusCode).isEqualTo(200)
            assertThat(getResponse.headers["Content-Type"]).contains("text/plain")
            assertThat(getResult.get()).isEqualTo(testData.expected)
        }

        @ParameterizedTest
        @MethodSource("markdownToHtmlProvider")
        fun `converts to HTML`(testData: HtmlConverterTests.MarkdownTestData) {
            // Arrange
            val json = getJsonWithOneFile(testData)
            val (postRequest, postResponse, postResult) =
                Fuel.post("http://localhost:4567/documents/")
                    .jsonBody(json)
                    .responseString()
            val (uuid, postError) = postResult

            // Act
            val (getRequest, getResponse, getResult) =
                Fuel.get("http://localhost:4567/documents/$uuid")
                    .header(Headers.ACCEPT, "text/html")
                    .responseString()

            // Assert
            assertThat(getResponse.statusCode).isEqualTo(200)
            assertThat(getResponse.headers["Content-Type"]).contains("text/html")
            assertThat(getResult.get()).isEqualTo(testData.expected)
        }

        @ParameterizedTest
        @MethodSource("markdownToHtmlProvider", "markdownToPlaintextProvider")
        fun `converts to JSON`(testData: HtmlConverterTests.MarkdownTestData) {
            // Arrange
            val json = getJsonWithOneFile(testData)
            val (postRequest, postResponse, postResult) =
                Fuel.post("http://localhost:4567/documents/")
                    .jsonBody(json)
                    .responseString()
            val (uuid, postError) = postResult

            // Act
            val (getRequest, getResponse, getResult) =
                Fuel.get("http://localhost:4567/documents/$uuid")
                    .header(Headers.ACCEPT, "application/json")
                    .responseString()

            // Assert
            assertThat(getResponse.statusCode).isEqualTo(200)
            assertThat(getResponse.headers["Content-Type"]).contains("application/json")
            assertThat(JsonUtils.isJSONValid(getResult.get())).isTrue()
            assertThat(getResult.get()).contains(testData.expected.toBase64())
            assertThat(getResult.get()).contains(".html")
            assertThat(getResult.get()).contains(".txt")
        }

        fun markdownToHtmlProvider() = HtmlConverterTests().markdownToHtmlProvider()
        fun markdownToPlaintextProvider() = HtmlConverterTests().markdownToPlaintextProvider()
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class `invalid requests` {
        @Test
        fun `fails POST with non-JSON Content-Type`() {
            // Arrange
            val json = getJsonWithOneFile(HtmlConverterTests.MarkdownTestData("foo", "bar"))

            // Act
            val (postRequest, postResponse, postResult) =
                Fuel.post("http://localhost:4567/documents/")
                    .body(json)
                    .header(Headers.CONTENT_TYPE, "application/binary")
                    .responseString()
            val (_, fuelError) = postResult

            // Assert
            assertThat(postResponse.statusCode).isEqualTo(415)
            assertThat(postResponse.headers["Content-Type"]).contains("application/json")

            // accessing result does somehow not work with a 415 status code (works well with e.g. 400)
            val body = fuelError?.errorData?.toString(Charset.defaultCharset())
            assertThat(body).contains("\"message\"")
            assertThat(JsonUtils.isJSONValid(body)).isTrue()
        }

        @Test
        fun `status code 404 on invalid UUID`() {
            // Arrange
            val uuid = UUID.randomUUID()

            // Act
            Fuel.get("http://localhost:4567/documents/$uuid")
                .header(Headers.ACCEPT, "text/plain")
                .responseString { request, response, result ->

                    // Assert
                    assertThat(response.statusCode).isEqualTo(404)
                    assertThat(response.headers["Content-Type"]).contains("application/json")
                    assertThat(result.get()).contains("\"message\"")
                    assertThat(JsonUtils.isJSONValid(result.get())).isTrue()
                }
        }
    }
}