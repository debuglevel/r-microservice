package de.debuglevel.markdown.rest.markdown

import com.google.gson.Gson
import de.debuglevel.markdown.domain.markdown.Converter
import de.debuglevel.markdown.domain.markdown.DocumentStorage
import de.debuglevel.markdown.domain.markdown.HtmlConverter
import de.debuglevel.markdown.domain.markdown.PlaintextConverter
import de.debuglevel.markdown.rest.responsetransformer.JsonTransformer
import mu.KotlinLogging
import spark.kotlin.RouteHandler
import java.io.ByteArrayOutputStream
import java.util.*

object MarkdownController {
    private val logger = KotlinLogging.logger {}

    fun getOneHtml(): RouteHandler.() -> Any {
        return {
            val id = params(":id")

            try {
                // Note: not using response.raw().outputStream because this complicates setting headers etc.
                val outputStream = ByteArrayOutputStream()
                val markdown = DocumentStorage.get(UUID.fromString(id)).file.asString
                HtmlConverter.convert(markdown, outputStream)

                type("text/html")
                outputStream
            } catch (e: Converter.ConversionException) {
                logger.info("Document '$id' could not be converted: ", e.message)
                type("application/json")
                status(400)
                "{\"message\":\"document '$id' could not be converted: ${e.message}\"}"
            }
        }
    }

    fun getOnePlaintext(): RouteHandler.() -> Any {
        return {
            val id = params(":id")

            try {
                val outputStream = ByteArrayOutputStream()
                val markdown = DocumentStorage.get(UUID.fromString(id)).file.asString
                PlaintextConverter.convert(markdown, outputStream)

                type(contentType = "text/plain")
                outputStream
            } catch (e: Converter.ConversionException) {
                logger.info("Document '$id' could not be converted: ", e.message)
                type("application/json")
                status(400)
                "{\"message\":\"document '$id' could not be converted: ${e.message}\"}"
            }
        }
    }

    fun String.toBase64() = Base64.getEncoder().encodeToString(this.toByteArray())
    fun ByteArray.toBase64() = Base64.getEncoder().encodeToString(this)

    fun getOneJson(): RouteHandler.() -> Any {
        return {
            val id = params(":id")

            val markdown = DocumentStorage.get(UUID.fromString(id)).file.asString

            val plaintextBase64 = ({
                val outputStream = ByteArrayOutputStream()
                PlaintextConverter.convert(markdown, outputStream)
                outputStream.toByteArray().toBase64()
            })()

            val htmlBase64 = ({
                val outputStream = ByteArrayOutputStream()
                HtmlConverter.convert(markdown, outputStream)
                outputStream.toByteArray().toBase64()
            })()

            try {
                val files = FileTransferDTO(
                    arrayOf(
                        FileDTO("$id.txt", plaintextBase64),
                        FileDTO("$id.html", htmlBase64)
                    )
                )

                type(contentType = "application/json")
                JsonTransformer.render(files)
            } catch (e: Converter.ConversionException) {
                logger.info("Document '$id' could not be converted: ", e.message)
                type("application/json")
                status(400)
                "{\"message\":\"document '$id' could not be converted: ${e.message}\"}"
            }
        }
    }

    fun postOne(): RouteHandler.() -> Any {
        return {
            if (request.contentType() != "application/json") {
                logger.info("Content-Type ${request.contentType()} is not supported.")
                status(415)
                type("application/json")
                "{\"message\":\"${request.contentType()} is not supported.\"}"
            } else {
                val markdownDTO = Gson().fromJson(request.body(), FileTransferDTO::class.java)

                val markdownWithUUID = DocumentStorage.add(markdownDTO)
                status(201)
                type("text/plain")
                markdownWithUUID.uuid
            }
        }
    }
}