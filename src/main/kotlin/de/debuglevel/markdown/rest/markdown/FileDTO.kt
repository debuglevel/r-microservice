package de.debuglevel.markdown.rest.markdown

import java.nio.charset.Charset
import java.util.*

data class FileDTO(val name: String, val base64content: String) {
    val content: String
        get() = base64content.fromBase64()
}

fun String.fromBase64(): String {
    return Base64.getDecoder().decode(this.toByteArray()).toString(Charset.defaultCharset())
}