package de.debuglevel.latex.domain.latex

import java.nio.file.Path

data class CompilerResult(
    val success: Boolean,
    val exitValue: Int,
    val durationMilliseconds: Long,
    val files: Array<Path>,
    val output: String
)