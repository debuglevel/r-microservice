package de.debuglevel.latex.rest.latex

data class ResponseFileTransferDTO(
    val success: Boolean,
    val exitValue: Int,
    val durationMilliseconds: Long,
    val files: Array<FileDTO>,
    val output: String
)