package io.koraframework.example.grpc.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.koraframework.common.annotation.Component;
import io.koraframework.generated.grpc.Message;
import io.koraframework.generated.grpc.UserServiceGrpc;

@Component
public final class UserService extends UserServiceGrpc.UserServiceImplBase {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void createUser(Message.RequestEvent request, StreamObserver<Message.ResponseEvent> responseObserver) {
        logger.info("Received request for name {} and code {}", request.getName(), request.getCode());

        responseObserver.onNext(Message.ResponseEvent.newBuilder()
                .setId(ByteString.copyFromUtf8(UUID.randomUUID().toString()))
                .setStatus(Message.ResponseEvent.StatusType.SUCCESS)
                .setType(Message.ResponseEvent.Type.OPENED)
                .setCreatedAt(Timestamp.newBuilder()
                        .setSeconds(OffsetDateTime.now().toEpochSecond())
                        .build())
                .build());

        logger.info("Processed request for name {} and code {}", request.getName(), request.getCode());
        responseObserver.onCompleted();
    }
}
