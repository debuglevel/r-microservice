package de.debuglevel.r.domain.r

import de.debuglevel.r.domain.command.Command
import de.debuglevel.r.rest.Configuration
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

/**
 * Processes data with R
 */
object RProcessor {
    private val logger = KotlinLogging.logger {}
    const val outputDirectory = "output"

    /**
     * Process the "main.R" R file in the working directory.
     * The output is assumed to be placed in the directory specified in the 'outputDirectory' constant.
     */
    fun compile(workingDirectory: Path): CompilerResult {
        return try {
            logger.debug { "Processing data with R..." }

            Files.createDirectory(workingDirectory.resolve("output"))
            val commandResult =
                Command(
                    "Rscript --vanilla ${Configuration.mainFile}",
                    workingDirectory
                ).run()

            val files = Files.walk(workingDirectory.resolve(outputDirectory))
                .filter { Files.isRegularFile(it) }
                .peek { logger.debug { "File found in output directory: '$it' (${it.toAbsolutePath()})" } }
                .toList()
                .toTypedArray()

            logger.debug { "Number of files in output directory: ${files.size}" }

            val compilerResult = CompilerResult(commandResult, files)

            logger.debug { "Processing data with R done." }
            compilerResult
        } catch (e: Exception) {
            // this exception is NOT raised if the exit value is != 0
            logger.error(e) { "Processing data with R failed" }
            throw CommandException("Processing data with R failed")
        }
    }

    class CommandException(message: String) : Exception(message)
}