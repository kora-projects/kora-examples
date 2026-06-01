package ru.tinkoff.kora.guide.s3.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.util.ByteBufferPublisherInputStream
import ru.tinkoff.kora.guide.s3.s3.FileMetadata
import ru.tinkoff.kora.guide.s3.s3.S3FileClient
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.common.form.FormMultipart
import ru.tinkoff.kora.http.common.header.HttpHeaders
import ru.tinkoff.kora.http.server.common.HttpServerResponse
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.s3.client.S3NotFoundException
import ru.tinkoff.kora.s3.client.model.S3Body
import java.io.ByteArrayInputStream
import java.io.InputStream

@Component
@HttpController
class DataController(
    private val s3FileClient: S3FileClient
) {

    @HttpRoute(method = HttpMethod.POST, path = "/files/upload")
    @Json
    fun uploadFile(multipart: FormMultipart): FileMetadata {
        val filePart = multipart.parts()
            .firstOrNull { it.name() == "file" }
            ?: throw IllegalArgumentException("No file part named 'file' provided")

        return when (filePart) {
            is FormMultipart.FormPart.MultipartFile -> {
                uploadStream(filePart.fileName(), filePart.contentType(), ByteArrayInputStream(filePart.content()))
            }

            is FormMultipart.FormPart.MultipartFileStream -> {
                uploadStream(
                    filePart.fileName(),
                    filePart.contentType(),
                    ByteBufferPublisherInputStream(filePart.content())
                )
            }

            else -> throw IllegalArgumentException("Part 'file' must be a multipart file")
        }
    }

    private fun uploadStream(fileName: String?, contentType: String?, inputStream: InputStream): FileMetadata {
        val actualContentType = if (contentType.isNullOrBlank()) "application/octet-stream" else contentType
        val fileId = java.util.UUID.randomUUID().toString()
        val body = S3Body.ofInputStreamReadAll(inputStream, actualContentType)
        s3FileClient.uploadFile(fileId, body)
        return FileMetadata(fileId, body.size(), actualContentType)
    }

    @HttpRoute(method = HttpMethod.GET, path = "/files")
    @Json
    fun listFiles(): List<FileMetadata> {
        return s3FileClient.listFiles().objects()
            .map { toMetadata(it.key(), it.size(), it.body().type()) }
    }

    @HttpRoute(method = HttpMethod.GET, path = "/files/{fileId}")
    fun downloadFile(fileId: String): HttpServerResponse {
        try {
            val obj = s3FileClient.downloadFile(fileId)
            val bytes = obj.body().asBytes()
            val contentType = obj.body().type() ?: "application/octet-stream"
            return HttpServerResponse.of(
                200,
                HttpHeaders.of("Content-Disposition", "attachment; filename=\"$fileId\""),
                HttpBody.of(contentType, bytes)
            )
        } catch (e: S3NotFoundException) {
            throw HttpServerResponseException.of(404, "File not found")
        }
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/files/{fileId}")
    @Json
    fun deleteFile(fileId: String): DeleteFileResponse {
        s3FileClient.deleteFile(fileId)
        return DeleteFileResponse("File deleted successfully")
    }

    private fun toMetadata(key: String, size: Long?, contentType: String?): FileMetadata {
        val normalized = if (key.startsWith("files/")) key.removePrefix("files/") else key
        return FileMetadata(normalized, size, contentType)
    }

    @Json
    data class DeleteFileResponse(val message: String)
}
