package de.debuglevel.latex.domain.latex

import de.debuglevel.latex.rest.latex.FileTransferDTO
import de.debuglevel.latex.rest.latex.StoredMarkdownDTO
import java.util.*

object DocumentStorage {
    private val documents = mutableMapOf<UUID, StoredMarkdownDTO>()

    fun get(uuid: UUID): StoredMarkdownDTO {
        return documents[uuid] ?: throw DocumentNotFoundException(uuid)
    }

    fun add(markdown: FileTransferDTO): StoredMarkdownDTO {
        val uuid = UUID.randomUUID()
        val markdownWithUUID = StoredMarkdownDTO(markdown.files.first(), uuid)
        documents[uuid] = markdownWithUUID
        return markdownWithUUID
    }

    class DocumentNotFoundException(uuid: UUID) : Exception("Document '$uuid' does not exist")
}