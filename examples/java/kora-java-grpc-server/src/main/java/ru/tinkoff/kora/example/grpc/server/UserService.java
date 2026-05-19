package ru.tinkoff.kora.example.grpc.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.generated.grpc.Message;
import ru.tinkoff.kora.generated.grpc.UserServiceGrpc;

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
