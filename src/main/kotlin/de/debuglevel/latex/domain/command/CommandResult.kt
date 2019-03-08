package de.debuglevel.latex.domain.command

data class CommandResult(
    val exitValue: Int,
    val durationMilliseconds: Long,
    val output: String
) {
    val succesful
        get() = exitValue == 0
}