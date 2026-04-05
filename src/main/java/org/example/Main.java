package org.example;


import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.example.configuration.DataInit;
import org.example.configuration.TarantoolConfig;
import org.example.repository.KvRepository;
import org.example.service.KvGrpcService;

public class Main {
    public static void main(String[] args) {
        try (TarantoolConfig config = new TarantoolConfig()) {
            KvRepository repository = new KvRepository(config.getClient());

            // ==========================================================
            // РАСКОММЕНТИРУЙТЕ СТРОКИ НИЖЕ ДЛЯ НАПОЛНЕНИЯ БАЗЫ (5 млн записей)
            // DataInit dataInit = new DataInit(repository);
            // dataInit.init();
            // ==========================================================

            Server server = ServerBuilder.forPort(9090)
                    .addService(new KvGrpcService(repository))
                    .addService(ProtoReflectionService.newInstance())
                    .build();

            System.out.println("=> gRPC Server started on port 9090 <=");
            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Stopping gRPC server...");
                server.shutdown();
            }));

            server.awaitTermination();
        } catch (Exception e) {
            System.err.println("Critical error: " + e.getMessage());
        }
    }
}