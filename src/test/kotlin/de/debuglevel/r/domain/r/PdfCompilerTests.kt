package de.debuglevel.r.domain.r

import org.junit.jupiter.api.TestInstance
import java.util.stream.Stream


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PdfCompilerTests {
//    @ParameterizedTest
//    @MethodSource("markdownToHtmlProvider")
//    fun `convert to HTML`(testData: LatexTestData) {
//        // Arrange
//        val outputStream = ByteArrayOutputStream()
//
//        // Act
//        HtmlConverter.convert(testData.value, outputStream)
//        val html = String(outputStream.toByteArray())
//
//        //Assert
//        assertThat(html).isEqualTo(testData.expected)
//    }
//
//    fun markdownToHtmlProvider() = Stream.of(
//        LatexTestData(value = "This is *Sparta*", expected = "<p>This is <em>Sparta</em></p>\n")
//    )
//
//    @ParameterizedTest
//    @MethodSource("latexToPdfProvider")
//    fun `convert to Plaintext`(testData: LatexTestData) {
//        // Arrange
//        val outputStream = ByteArrayOutputStream()
//
//        // Act
//        PlaintextConverter.convert(testData.value, outputStream)
//        val plaintext = String(outputStream.toByteArray())
//
//        //Assert
//        assertThat(plaintext).isEqualTo(testData.expected)
//    }

    fun latexToPdfProvider() = Stream.of(
        LatexTestData(value = "foo", expected = "bar")
    )

    data class LatexTestData(
        val value: String,
        val expected: String
    )
}