package de.debuglevel.markdown.domain.markdown

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.ByteArrayOutputStream
import java.util.stream.Stream


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HtmlConverterTests {
    @ParameterizedTest
    @MethodSource("markdownToHtmlProvider")
    fun `convert to HTML`(testData: MarkdownTestData) {
        // Arrange
        val outputStream = ByteArrayOutputStream()

        // Act
        HtmlConverter.convert(testData.value, outputStream)
        val html = String(outputStream.toByteArray())

        //Assert
        assertThat(html).isEqualTo(testData.expected)
    }

    fun markdownToHtmlProvider() = Stream.of(
        MarkdownTestData(value = "This is *Sparta*", expected = "<p>This is <em>Sparta</em></p>\n")
    )

    @ParameterizedTest
    @MethodSource("markdownToPlaintextProvider")
    fun `convert to Plaintext`(testData: MarkdownTestData) {
        // Arrange
        val outputStream = ByteArrayOutputStream()

        // Act
        PlaintextConverter.convert(testData.value, outputStream)
        val plaintext = String(outputStream.toByteArray())

        //Assert
        assertThat(plaintext).isEqualTo(testData.expected)
    }

    fun markdownToPlaintextProvider() = Stream.of(
        MarkdownTestData(value = "This is *Sparta*", expected = "This is Sparta")
    )

    data class MarkdownTestData(
        val value: String,
        val expected: String
    )
}