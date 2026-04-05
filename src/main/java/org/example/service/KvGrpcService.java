package org.example.service;


import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.repository.KvRepository;
import org.example.vk1grpc.proto.*;

import java.util.List;

public class KvGrpcService extends KvServiceGrpc.KvServiceImplBase {
    private final KvRepository kvRepository;

    public KvGrpcService(KvRepository kvRepository) {
        this.kvRepository = kvRepository;
    }

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        try {
            byte[] value = request.hasValue() ? request.getValue().toByteArray() : null;
            kvRepository.put(request.getKey(), value);

            responseObserver.onNext(PutResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void get(GetRequest request, StreamObserver<GetResponse> responseObserver) {
        try {
            byte[] value = kvRepository.get(request.getKey());
            GetResponse.Builder builder = GetResponse.newBuilder();
            if (value != null) {
                builder.setValue(ByteString.copyFrom(value));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        try {
            kvRepository.delete(request.getKey());
            responseObserver.onNext(DeleteResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void count(CountRequest request, StreamObserver<CountResponse> responseObserver) {
        try {
            long count = kvRepository.count();
            responseObserver.onNext(CountResponse.newBuilder().setCount(count).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void range(RangeRequest request, StreamObserver<RangeResponse> responseObserver) {
        try {
            List<List<Object>> results = kvRepository.range(request.getKeySince(), request.getKeyTo());

            for (List<Object> tuple : results) {
                if (tuple.size() < 1) continue;

                String key = String.valueOf(tuple.get(0));
                byte[] value = null;

                if (tuple.size() > 1 && tuple.get(1) != null) {
                    Object v = tuple.get(1);
                    if (v instanceof byte[] bytes) {
                        value = bytes;
                    }
                }

                RangeResponse response = RangeResponse.newBuilder()
                        .setKey(key)
                        .setValue(value != null ? ByteString.copyFrom(value) : ByteString.EMPTY)
                        .build();

                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
