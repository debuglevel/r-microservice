package de.debuglevel.r.rest.r

import java.nio.file.Path
import java.util.*

data class StoredFileTransferDTO(
    val files: Array<FileDTO>,
    val uuid: UUID,
    val path: Path?
)