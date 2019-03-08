package de.debuglevel.r.rest

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.jsonBody
import de.debuglevel.r.domain.r.PdfCompilerTests
import de.debuglevel.microservices.utils.spark.SparkTestUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
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

    private fun getJsonWithOneFile(testData: PdfCompilerTests.LatexTestData): String {
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
        @Test
        fun `sends UUID on POST`() {
            // Arrange
            val json = getJsonWithOneFile(PdfCompilerTests.LatexTestData("foo", "bar"))

            // Act
            val (postRequest, postResponse, postResult) =
                Fuel.post("http://localhost:4567/analyses/")
                    .jsonBody(json)
                    .responseString()
            val (uuid, postError) = postResult

            // Assert
            assertThat(postResponse.statusCode).isEqualTo(201)
            assertThat(postResponse.headers["Content-Type"]).contains("text/plain")
            assertThatCode { UUID.fromString(uuid) }.doesNotThrowAnyException()
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class `invalid requests` {
        // TODO: test fails sometimes
//        @Test
//        fun `fails POST with non-JSON Content-Type`() {
//            // Arrange
//            val json = getJsonWithOneFile(PdfCompilerTests.LatexTestData("foo", "bar"))
//
//            // Act
//            val (postRequest, postResponse, postResult) =
//                Fuel.post("http://localhost:4567/documents/")
//                    .body(json)
//                    .header(Headers.CONTENT_TYPE, "application/binary")
//                    .responseString()
//            val (_, fuelError) = postResult
//
//            // Assert
//            assertThat(postResponse.statusCode).isEqualTo(415)
//            assertThat(postResponse.headers["Content-Type"]).contains("application/json")
//
//            // accessing result does somehow not work with a 415 status code (works well with e.g. 400)
//            val body = fuelError?.errorData?.toString(Charset.defaultCharset())
//            assertThat(body).contains("\"message\"")
//            assertThat(JsonUtils.isJSONValid(body)).isTrue()
//        }

        @Test
        fun `status code 404 on invalid UUID`() {
            // Arrange
            val uuid = UUID.randomUUID()

            // Act
            Fuel.get("http://localhost:4567/analyses/$uuid")
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