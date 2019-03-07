package de.debuglevel.latex.domain.latex

import de.debuglevel.latex.domain.command.Command
import de.debuglevel.latex.domain.command.CommandResult
import mu.KotlinLogging
import java.nio.file.Path

/**
 * Compiles LaTex to PDF
 */
object PdfCompiler {
    private val logger = KotlinLogging.logger {}

    /**
     * Convert the given LaTeX to PDF
     */
    fun compile(workingDirectory: Path): CommandResult {
        return try {
            logger.debug { "Compiling LaTeX to PDF..." }

            val output = Command("pdflatex -interaction=nonstopmode main.tex", workingDirectory).run()
            val files = arrayOf<Path>(workingDirectory.resolve("main.pdf"))

            val commandResult = CommandResult(output, files)

            logger.debug { "Compiling LaTeX to PDF done." }
            logger.debug { "pdflatex output:\n$output" }

            commandResult
        } catch (e: Exception) {
            logger.error(e) { "Compiling LaTeX to PDF failed" }
            throw CommandException("Compiling LaTeX to PDF failed")
        }
    }

    class CommandException(s: String) : Exception()
}