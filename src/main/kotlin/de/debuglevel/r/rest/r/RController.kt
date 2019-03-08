package de.debuglevel.r.rest.r

import com.google.gson.Gson
import de.debuglevel.r.domain.r.DocumentStorage
import de.debuglevel.r.domain.r.RProcessor
import de.debuglevel.r.rest.responsetransformer.JsonTransformer
import mu.KotlinLogging
import spark.kotlin.RouteHandler
import java.util.*

object RController {
    private val logger = KotlinLogging.logger {}

    fun String.toBase64() = Base64.getEncoder().encodeToString(this.toByteArray())
    fun ByteArray.toBase64() = Base64.getEncoder().encodeToString(this)

    fun getOneJson(): RouteHandler.() -> Any {
        return {
            val id = params(":id")

            val storedFileTransfer = DocumentStorage.get(UUID.fromString(id))
            if (storedFileTransfer.path == null) {
                throw Exception("Path must not be null")
            }

            try {
                val workingDirectory = storedFileTransfer.path
                val compilerResult = RProcessor.compile(workingDirectory)
                val fileTransferDto = ResponseFileTransferDTO(
                    compilerResult.success,
                    compilerResult.exitValue,
                    compilerResult.durationMilliseconds,
                    compilerResult.files.map {
                        FileDTO(
                            workingDirectory.relativize(it).toString(),
                            it.toFile().readBytes().toBase64()
                        )
                    }.toTypedArray(),
                    compilerResult.output
                )

                type(contentType = "application/json")
                JsonTransformer.render(fileTransferDto)
            } catch (e: RProcessor.CommandException) {
                logger.info("Document '$id' could not be compiled: ", e.message)
                type("application/json")
                status(400)
                "{\"message\":\"document '$id' could not be compiled: ${e.message}\"}"
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
                val fileTransferDTO = Gson().fromJson(request.body(), RequestFileTransferDTO::class.java)

                val latexWithUUID = DocumentStorage.add(fileTransferDTO)
                status(201)
                type("text/plain")
                latexWithUUID.uuid
            }
        }
    }
}