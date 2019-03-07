package de.debuglevel.latex.domain.command

import java.nio.file.Path

data class CommandResult(
    val output: String,
    val files: Array<Path>
)