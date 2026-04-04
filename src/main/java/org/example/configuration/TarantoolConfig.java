package org.example.configuration;


import io.tarantool.client.box.TarantoolBoxClient;
import io.tarantool.client.factory.TarantoolFactory;
import io.tarantool.pool.InstanceConnectionGroup;

import java.util.Collections;

public class TarantoolConfig implements AutoCloseable {
    private final TarantoolBoxClient client;

    public TarantoolConfig() throws Exception {
        InstanceConnectionGroup group = InstanceConnectionGroup.builder()
                .withHost("localhost")
                .withPort(3301)
                .build();

        client = TarantoolFactory.box()
                .withGroups(Collections.singletonList(group))
                .build();
    }

    public TarantoolBoxClient getClient() {
        return client;
    }

    @Override
    public void close() throws Exception {
        if (client != null) {
            client.close();
        }
    }
}