package de.debuglevel.r.rest

import de.debuglevel.microservices.utils.spark.SparkTestUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import spark.Spark


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MainKtTest {

    @Test
    fun `standalone startup`() {
        // Arrange
        main(arrayOf())
        Spark.awaitInitialization()

        // Act
        val response = ApiTestUtils.request("GET", "/", null)

        // Assert
        // HTTP Codes begin from "100". So something from 100 and above was probably a response to a HTTP request
        Assertions.assertThat(response?.status).isGreaterThanOrEqualTo(100)
    }

    @AfterAll
    fun stopServer() {
        SparkTestUtils.awaitShutdown()
    }
}