package de.debuglevel.markdown.domain.markdown

import mu.KotlinLogging
import org.commonmark.parser.Parser
import org.commonmark.renderer.text.TextContentRenderer
import java.io.OutputStream
import java.io.OutputStreamWriter

/**
 * Converts markdown to Plaintext
 */
object PlaintextConverter : Converter {
    private val logger = KotlinLogging.logger {}

    /**
     * Convert the given markdown code to Plaintext
     *
     * @param markdown markdown code
     * @return the generated Plaintext
     */
    override fun convert(markdown: String, outputStream: OutputStream) {
        logger.debug { "Converting Markdown to Plaintext..." }

        val outputStreamWriter = OutputStreamWriter(outputStream)

        val parser = Parser.builder().build()
        val document = parser.parse(markdown)
        val renderer = TextContentRenderer.builder().build()
        renderer.render(document, outputStreamWriter)

        outputStreamWriter.close()

        logger.debug { "Converting Markdown to Plaintext done." }
    }
}