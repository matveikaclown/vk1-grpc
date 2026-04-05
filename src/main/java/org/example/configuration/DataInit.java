package org.example.configuration;


import org.example.repository.KvRepository;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DataInit {
    private static final int TOTAL_RECORDS = 5_000_000;
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    private final KvRepository kvRepository;

    public DataInit(KvRepository kvRepository) {
        this.kvRepository = kvRepository;
    }

    public  void init(){
        byte[][] presetValues = {
                "Value_Alpha".getBytes(),
                "Value_Beta".getBytes(),
                "Value_Gamma".getBytes(),
                "Value_Delta".getBytes(),
                null,
                "Value_Epsilon".getBytes(),
                "Value_Zeta".getBytes(),
                "Value_Eta".getBytes(),
                "Value_Theta".getBytes(),
                "Value_Iota".getBytes()
        };

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger counter = new AtomicInteger(0);
        Random random = new Random();

        long startTime = System.currentTimeMillis();
        System.out.println("Starting population of 5,000,000 records...");

        for (int i = 0; i < TOTAL_RECORDS; i++) {
            executor.submit(() -> {
                try {
                    String key = "key_" + UUID.randomUUID();

                    byte[] value = presetValues[random.nextInt(presetValues.length)];

                    kvRepository.put(key, value);

                    int current = counter.incrementAndGet();
                    if (current % 200_000 == 0) {
                        System.out.println("Inserted " + current + " records...");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        try {
            executor.shutdown();
            if (!executor.awaitTermination(2, TimeUnit.HOURS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();
        System.out.println(">>> [DataInit] Finished in " + (endTime - startTime) / 1000 + " seconds.");
    }
}
