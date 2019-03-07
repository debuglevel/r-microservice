package de.debuglevel.latex.domain.latex

import mu.KotlinLogging
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.io.OutputStream
import java.io.OutputStreamWriter

/**
 * Converts markdown to HTML
 */
object HtmlConverter : Converter {
    private val logger = KotlinLogging.logger {}

    /**
     * Convert the given markdown code to HTML
     *
     * @param markdown markdown code
     * @return the generated HTML
     */
    override fun convert(markdown: String, outputStream: OutputStream) {
        logger.debug { "Converting Markdown to HTML..." }

        val outputStreamWriter = OutputStreamWriter(outputStream)

        val parser = Parser.builder().build()
        val document = parser.parse(markdown)
        val renderer = HtmlRenderer.builder().build()
        renderer.render(document, outputStreamWriter)

        outputStreamWriter.close()

        logger.debug { "Converting Markdown to HTML done." }
    }
}