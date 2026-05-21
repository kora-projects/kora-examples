package ru.tinkoff.kora.kotlin.example.grpc.server

import com.google.protobuf.ByteString
import com.google.protobuf.Timestamp
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.generated.grpc.Message
import ru.tinkoff.kora.generated.grpc.UserServiceGrpc
import java.time.OffsetDateTime

@Component
class MyServerInterceptor : ServerInterceptor {
    private val logger = LoggerFactory.getLogger(MyServerInterceptor::class.java)

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        logger.info("INTERCEPTED")
        return next.startCall(call, headers)
    }
}


