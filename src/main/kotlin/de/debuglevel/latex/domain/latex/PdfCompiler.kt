package de.debuglevel.latex.domain.latex

import de.debuglevel.latex.domain.command.Command
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

/**
 * Compiles LaTeX to PDF
 */
object PdfCompiler {
    private val logger = KotlinLogging.logger {}

    /**
     * Compile the given LaTeX to PDF
     */
    fun compile(workingDirectory: Path): CompilerResult {
        return try {
            logger.debug { "Compiling LaTeX to PDF..." }

            val commandResult =
                Command("pdflatex -interaction=nonstopmode -output-directory=output main.tex", workingDirectory).run()

            val files = Files.walk(workingDirectory.resolve("output"))
                .filter { Files.isRegularFile(it) }
                .peek { logger.debug { "File found in output directory: '$it' (${it.toAbsolutePath()})" } }
                .toList()
                .toTypedArray()

            logger.debug { "Number of files in output directory: ${files.size}" }

            val compilerResult = CompilerResult(
                commandResult.succesful,
                commandResult.exitValue,
                commandResult.durationMilliseconds,
                files,
                commandResult.output
            )

            logger.debug { "Compiling LaTeX to PDF done." }

            compilerResult
        } catch (e: Exception) {
            // this exception is NOT raised if the exit value is != 0
            logger.error(e) { "Compiling LaTeX to PDF failed" }
            throw CommandException("Compiling LaTeX to PDF failed")
        }
    }

    class CommandException(s: String) : Exception(s)
}