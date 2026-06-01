package ru.tinkoff.kora.guide.s3.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.util.ByteBufferPublisherInputStream;
import ru.tinkoff.kora.guide.s3.s3.FileMetadata;
import ru.tinkoff.kora.guide.s3.s3.S3FileClient;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.common.form.FormMultipart;
import ru.tinkoff.kora.http.common.header.HttpHeaders;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.json.common.annotation.Json;
import ru.tinkoff.kora.s3.client.S3NotFoundException;
import ru.tinkoff.kora.s3.client.model.S3Body;

@Component
@HttpController
public final class DataController {

    private final S3FileClient s3FileClient;

    public DataController(S3FileClient s3FileClient) {
        this.s3FileClient = s3FileClient;
    }

    @HttpRoute(method = HttpMethod.POST, path = "/files/upload")
    @Json
    public FileMetadata uploadFile(FormMultipart multipart) {
        var filePart = multipart.parts().stream()
                .filter(part -> "file".equals(part.name()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No file part named 'file' provided"));

        if (filePart instanceof FormMultipart.FormPart.MultipartFile mf) {
            return this.uploadStream(mf.fileName(), mf.contentType(), new ByteArrayInputStream(mf.content()));
        }
        if (filePart instanceof FormMultipart.FormPart.MultipartFileStream mfs) {
            return this.uploadStream(mfs.fileName(), mfs.contentType(), new ByteBufferPublisherInputStream(mfs.content()));
        }

        throw new IllegalArgumentException("Part 'file' must be a multipart file");
    }

    @HttpRoute(method = HttpMethod.GET, path = "/files")
    @Json
    public List<FileMetadata> listFiles() {
        return this.s3FileClient.listFiles().objects().stream()
                .map(object -> this.toMetadata(object.key(), object.size(), object.body().type()))
                .toList();
    }

    @HttpRoute(method = HttpMethod.GET, path = "/files/{fileId}")
    public HttpServerResponse downloadFile(String fileId) {
        try {
            var object = this.s3FileClient.downloadFile(fileId);
            var bytes = object.body().asBytes();
            var contentType = object.body().type() == null ? "application/octet-stream" : object.body().type();
            return HttpServerResponse.of(
                    200,
                    HttpHeaders.of("Content-Disposition", "attachment; filename=\"" + fileId + "\""),
                    HttpBody.of(contentType, bytes));
        } catch (S3NotFoundException e) {
            throw HttpServerResponseException.of(404, "File not found");
        }
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/files/{fileId}")
    @Json
    public DeleteFileResponse deleteFile(String fileId) {
        this.s3FileClient.deleteFile(fileId);
        return new DeleteFileResponse("File deleted successfully");
    }

    private FileMetadata uploadStream(String fileName, String contentType, InputStream inputStream) {
        String actualContentType = (contentType == null || contentType.isBlank()) ? "application/octet-stream" : contentType;
        String fileId = UUID.randomUUID().toString();
        S3Body body = S3Body.ofInputStreamReadAll(inputStream, actualContentType);
        this.s3FileClient.uploadFile(fileId, body);
        return new FileMetadata(fileId, body.size(), actualContentType);
    }

    private FileMetadata toMetadata(String key, Long size, String contentType) {
        String normalized = key.startsWith("files/") ? key.substring("files/".length()) : key;
        return new FileMetadata(normalized, size, contentType);
    }

    @Json
    public record DeleteFileResponse(String message) {}
}
