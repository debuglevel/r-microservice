package de.debuglevel.latex.rest.latex

import java.nio.charset.Charset
import java.util.*

data class FileDTO(
    val name: String,
    val base64data: String
) {
    val asString: String
        get() = asByteArray.toString(Charset.defaultCharset())

    val asByteArray: ByteArray
        get() = Base64.getDecoder().decode(base64data.toByteArray())
}