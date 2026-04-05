package org.example.repository;


import io.tarantool.client.box.TarantoolBoxClient;
import io.tarantool.mapping.SelectResponse;
import io.tarantool.mapping.Tuple;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class KvRepository {
    private final TarantoolBoxClient client;
    private static final String SPACE_NAME = "KV";

    public KvRepository(TarantoolBoxClient client) {
        this.client = client;
    }

    public void put(String key, byte[] value) throws Exception {
        client.space(SPACE_NAME).replace(Arrays.asList(key, value)).get();
    }

    public byte[] get(String key) throws Exception {
        SelectResponse<?> response = client.space(SPACE_NAME)
                .select(Collections.singletonList(key))
                .get();

        Object data = response.get();
        if (data == null) {
            return null;
        }

        if (data instanceof List<?> tuples && !tuples.isEmpty()) {
            Object firstTuple = tuples.get(0);

            if (firstTuple instanceof Tuple<?> tuple) {
                Object tupleData = tuple.get();
                if (tupleData instanceof List<?> fields && fields.size() > 1) {
                    Object valueObj = fields.get(1);
                    if (valueObj == null) {
                        return null;
                    }
                    if (valueObj instanceof byte[] bytes) {
                        return bytes;
                    }
                }
            }
            else if (firstTuple instanceof List<?> fields && fields.size() > 1) {
                Object valueObj = fields.get(1);
                if (valueObj == null) {
                    return null;
                }
                if (valueObj instanceof byte[] bytes) {
                    return bytes;
                }
            }
        }

        return null;
    }

    public void delete(String key) throws Exception {
        client.space(SPACE_NAME).delete(Collections.singletonList(key)).get();
    }

    public long count() throws Exception {
        var response = client.eval("return box.space.KV:count()").get();

        Object data = response.get();

        if (data instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Number number) {
                return number.longValue();
            }
        }

        return 0L;
    }

    public List<List<Object>> range(String keySince, String keyTo) throws Exception {
        String luaScript = "local s, t = ... local r = {} for _, v in box.space.KV.index.primary:pairs(s, {iterator = 'GE'}) do if v[1] > t then break end table.insert(r, {v[1], v[2]}) end return r";

        var response = client.eval(luaScript, Arrays.asList(keySince, keyTo)).get();
        Object data = response.get();

        List<List<Object>> finalizedResults = new java.util.ArrayList<>();

        if (data instanceof List<?> outerList && !outerList.isEmpty()) {
            Object innerData = outerList.get(0);

            if (innerData instanceof List<?> items) {
                for (Object item : items) {
                    if (item instanceof List<?> fields) {
                        finalizedResults.add(new java.util.ArrayList<>(fields));
                    }
                }
            }
        }
        return finalizedResults;
    }
}
