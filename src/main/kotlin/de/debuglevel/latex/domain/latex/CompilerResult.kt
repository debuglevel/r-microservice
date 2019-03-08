package de.debuglevel.latex.domain.latex

import de.debuglevel.latex.domain.command.CommandResult
import java.nio.file.Path

data class CompilerResult(
    val success: Boolean,
    val exitValue: Int,
    val durationMilliseconds: Long,
    val files: Array<Path>,
    val output: String
) {
    constructor(
        commandResult: CommandResult,
        files: Array<Path>
    ) : this(
        commandResult.successful,
        commandResult.exitValue,
        commandResult.durationMilliseconds,
        files,
        commandResult.output
    )
}