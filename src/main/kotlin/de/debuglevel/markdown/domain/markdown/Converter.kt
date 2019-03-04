package de.debuglevel.markdown.domain.markdown

import java.io.OutputStream

interface Converter {
    /**
     * Convert the given markdown code
     *
     * @param markdown markdown code
     * @return the generated content
     */
    fun convert(markdown: String, outputStream: OutputStream)

    class ConversionException(message: String) : Exception(message)
}