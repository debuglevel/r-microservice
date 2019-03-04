package de.debuglevel.markdown.rest.markdown

import com.google.gson.Gson
import de.debuglevel.markdown.domain.markdown.Converter
import de.debuglevel.markdown.domain.markdown.DocumentStorage
import de.debuglevel.markdown.domain.markdown.HtmlConverter
import de.debuglevel.markdown.domain.markdown.PlaintextConverter
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
                val markdown = DocumentStorage.get(UUID.fromString(id)).file.content
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
                val markdown = DocumentStorage.get(UUID.fromString(id)).file.content
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

    fun postOne(): RouteHandler.() -> Any {
        return {
            if (request.contentType() != "application/json") {
                logger.info("Content-Type ${request.contentType()} is not supported.")
                status(415)
                type("application/json")
                "{\"message\":\"${request.contentType()} is not supported.\"}"
            } else {
                val markdownDTO = Gson().fromJson(request.body(), MarkdownDTO::class.java)

                val markdownWithUUID = DocumentStorage.add(markdownDTO)
                status(201)
                type("text/plain")
                markdownWithUUID.uuid
            }
        }
    }

    class UnsupportedContentTypeException(contentType: String) : Exception("Content-Type $contentType not supported.")
}