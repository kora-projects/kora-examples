package io.koraframework.guide.s3.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.util.ByteBufferInputStream;
import io.koraframework.guide.s3.s3.FileMetadata;
import io.koraframework.guide.s3.s3.S3FileClient;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.common.form.FormMultipart;
import io.koraframework.http.common.header.HttpHeaders;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.response.HttpServerResponseException;
import io.koraframework.http.server.common.annotation.HttpController;
import io.koraframework.json.common.annotation.Json;
import io.koraframework.s3.client.kora.exception.S3ClientNoSuchKeyException;

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
            return this.upload(mf.contentType(), mf.content());
        }
        // a streamed part carries an HttpBodyOutput, which knows how to write itself out
        if (filePart instanceof FormMultipart.FormPart.MultipartFileStream mfs) {
            var buffer = new ByteArrayOutputStream();
            try {
                mfs.content().write(buffer);
            } catch (IOException e) {
                throw HttpServerResponseException.of(400, "Failed to read uploaded file");
            }
            return this.upload(mfs.content().contentType(), buffer.toByteArray());
        }

        throw new IllegalArgumentException("Part 'file' must be a multipart file");
    }

    @HttpRoute(method = HttpMethod.GET, path = "/files")
    @Json
    public List<FileMetadata> listFiles() {
        return this.s3FileClient.listFiles().items().stream()
                .map(item -> this.toMetadata(item.key(), item.size(), null))
                .toList();
    }

    @HttpRoute(method = HttpMethod.GET, path = "/files/{fileId}")
    public HttpServerResponse downloadFile(String fileId) {
        try (var object = this.s3FileClient.downloadFile(fileId); var body = object.body().asInputStream()) {
            var bytes = body.readAllBytes();
            var contentType = object.headers().getFirst("Content-Type");
            return HttpServerResponse.of(
                    200,
                    HttpHeaders.of("Content-Disposition", "attachment; filename=\"" + fileId + "\""),
                    HttpBody.of(contentType == null ? "application/octet-stream" : contentType, bytes));
        } catch (S3ClientNoSuchKeyException e) {
            throw HttpServerResponseException.of(404, "File not found");
        } catch (IOException e) {
            throw HttpServerResponseException.of(500, "Failed to read file");
        }
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/files/{fileId}")
    @Json
    public DeleteFileResponse deleteFile(String fileId) {
        this.s3FileClient.deleteFile(fileId);
        return new DeleteFileResponse("File deleted successfully");
    }

    private FileMetadata upload(String contentType, byte[] body) {
        String actualContentType = (contentType == null || contentType.isBlank()) ? "application/octet-stream" : contentType;
        String fileId = UUID.randomUUID().toString();
        this.s3FileClient.uploadFile(fileId, body);
        return new FileMetadata(fileId, (long) body.length, actualContentType);
    }

    private FileMetadata toMetadata(String key, Long size, String contentType) {
        String normalized = key.startsWith("files/") ? key.substring("files/".length()) : key;
        return new FileMetadata(normalized, size, contentType);
    }

    @Json
    public record DeleteFileResponse(String message) {}
}
