package io.koraframework.guide.s3.controller

import io.koraframework.common.annotation.Component
import io.koraframework.guide.s3.s3.FileMetadata
import io.koraframework.guide.s3.s3.S3FileClient
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.common.form.FormMultipart
import io.koraframework.http.common.header.HttpHeaders
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.http.server.common.response.HttpServerResponse
import io.koraframework.http.server.common.response.HttpServerResponseException
import io.koraframework.json.common.annotation.Json
import io.koraframework.s3.client.kora.exception.S3ClientNoSuchKeyException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID

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
            is FormMultipart.FormPart.MultipartFile -> upload(filePart.contentType(), filePart.content())

            // a streamed part carries an HttpBodyOutput, which knows how to write itself out
            is FormMultipart.FormPart.MultipartFileStream -> {
                val buffer = ByteArrayOutputStream()
                try {
                    filePart.content().write(buffer)
                } catch (e: IOException) {
                    throw HttpServerResponseException.of(400, "Failed to read uploaded file")
                }
                upload(filePart.content().contentType(), buffer.toByteArray())
            }

            else -> throw IllegalArgumentException("Part 'file' must be a multipart file")
        }
    }

    @HttpRoute(method = HttpMethod.GET, path = "/files")
    @Json
    fun listFiles(): List<FileMetadata> {
        return s3FileClient.listFiles().items()
            .map { toMetadata(it.key(), it.size(), null) }
    }

    @HttpRoute(method = HttpMethod.GET, path = "/files/{fileId}")
    fun downloadFile(fileId: String): HttpServerResponse {
        try {
            s3FileClient.downloadFile(fileId).use { obj ->
                obj.body().asInputStream().use { body ->
                    val bytes = body.readAllBytes()
                    val contentType = obj.headers().getFirst("Content-Type") ?: "application/octet-stream"
                    return HttpServerResponse.of(
                        200,
                        HttpHeaders.of("Content-Disposition", "attachment; filename=\"$fileId\""),
                        HttpBody.of(contentType, bytes)
                    )
                }
            }
        } catch (e: S3ClientNoSuchKeyException) {
            throw HttpServerResponseException.of(404, "File not found")
        } catch (e: IOException) {
            throw HttpServerResponseException.of(500, "Failed to read file")
        }
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/files/{fileId}")
    @Json
    fun deleteFile(fileId: String): DeleteFileResponse {
        s3FileClient.deleteFile(fileId)
        return DeleteFileResponse("File deleted successfully")
    }

    private fun upload(contentType: String?, body: ByteArray): FileMetadata {
        val actualContentType = if (contentType.isNullOrBlank()) "application/octet-stream" else contentType
        val fileId = UUID.randomUUID().toString()
        s3FileClient.uploadFile(fileId, body)
        return FileMetadata(fileId, body.size.toLong(), actualContentType)
    }

    private fun toMetadata(key: String, size: Long?, contentType: String?): FileMetadata {
        val normalized = key.removePrefix("files/")
        return FileMetadata(normalized, size, contentType)
    }

    @Json
    data class DeleteFileResponse(val message: String)
}
