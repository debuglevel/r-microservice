package de.debuglevel.latex.domain.command

import mu.KotlinLogging
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class Command(
    private val command: String,
    private val workingDirectory: Path
) {
    private val logger = KotlinLogging.logger {}

    fun run(): String {
        logger.debug { "Running command '$command'..." }

        val parts = command.split("\\s".toRegex())
        val process = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDirectory.toFile())
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        process.waitFor(60, TimeUnit.MINUTES)

        logger.debug { "Running command '$command' done." }

        return process.inputStream.bufferedReader().readText()
    }
}