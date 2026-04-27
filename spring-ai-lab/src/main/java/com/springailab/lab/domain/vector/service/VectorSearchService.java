package com.springailab.lab.domain.vector.service;

import com.springailab.lab.domain.vector.config.VectorRedisProperties;
import com.springailab.lab.web.dto.VectorSearchItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis 向量检索服务（RediSearch FT.SEARCH KNN）。
 *
 * @author jiaolin
 */
@Service
public class VectorSearchService {

    private static final Logger log = LoggerFactory.getLogger(VectorSearchService.class);

    private final StringRedisTemplate stringRedisTemplate;

    private final VectorRedisProperties vectorRedisProperties;

    public VectorSearchService(StringRedisTemplate stringRedisTemplate, VectorRedisProperties vectorRedisProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.vectorRedisProperties = vectorRedisProperties;
    }

    /**
     * 向量 KNN 检索。
     *
     * @param queryVector 向量
     * @param topK topK
     * @return 命中项
     */
    public List<VectorSearchItem> search(List<Double> queryVector, Integer topK) {
        if (queryVector == null || queryVector.isEmpty()) {
            return Collections.emptyList();
        }
        int actualTopK = resolveTopK(topK);
        byte[] vectorBytes = toFloat32Bytes(queryVector);
        List<Object> response = this.stringRedisTemplate.execute((RedisCallback<List<Object>>) connection ->
                executeKnn(connection, vectorBytes, actualTopK));
        return parseResponse(response);
    }

    private int resolveTopK(Integer topK) {
        if (topK != null && topK > 0) {
            return topK;
        }
        if (this.vectorRedisProperties.getTopK() == null || this.vectorRedisProperties.getTopK() <= 0) {
            return 5;
        }
        return this.vectorRedisProperties.getTopK();
    }

    private List<Object> executeKnn(RedisConnection connection, byte[] vectorBytes, int topK) {
        String indexName = this.vectorRedisProperties.getIndexName();
        String vectorField = this.vectorRedisProperties.getVectorField();
        String contentField = this.vectorRedisProperties.getContentField();
        String query = "*=>[KNN " + topK + " @" + vectorField + " $BLOB AS score]";
        try {
            return (List<Object>) connection.execute("FT.SEARCH",// "FT.SEARCH".getBytes(StandardCharsets.UTF_8)
                    indexName.getBytes(StandardCharsets.UTF_8),
                    query.getBytes(StandardCharsets.UTF_8),
                    "PARAMS".getBytes(StandardCharsets.UTF_8),
                    "2".getBytes(StandardCharsets.UTF_8),
                    "BLOB".getBytes(StandardCharsets.UTF_8),
                    vectorBytes,
                    "SORTBY".getBytes(StandardCharsets.UTF_8),
                    "score".getBytes(StandardCharsets.UTF_8),
                    "RETURN".getBytes(StandardCharsets.UTF_8),
                    "2".getBytes(StandardCharsets.UTF_8),
                    contentField.getBytes(StandardCharsets.UTF_8),
                    "score".getBytes(StandardCharsets.UTF_8),
                    "DIALECT".getBytes(StandardCharsets.UTF_8),
                    "2".getBytes(StandardCharsets.UTF_8));
        } catch (DataAccessException ex) {
            log.error("Vector search failed: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private static byte[] toFloat32Bytes(List<Double> vector) {
        ByteBuffer buffer = ByteBuffer.allocate(vector.size() * Float.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (Double value : vector) {
            float f = value == null ? 0.0f : value.floatValue();
            buffer.putFloat(f);
        }
        return buffer.array();
    }

    private List<VectorSearchItem> parseResponse(List<Object> raw) {
        if (raw == null || raw.size() < 3) {
            return Collections.emptyList();
        }
        List<VectorSearchItem> items = new ArrayList<>();
        for (int i = 1; i + 1 < raw.size(); i += 2) {
            String id = objectToString(raw.get(i));
            Object fieldsObject = raw.get(i + 1);
            if (!(fieldsObject instanceof List<?>)) {
                continue;
            }
            List<?> fields = (List<?>) fieldsObject;
            if (fields.isEmpty()) {
                continue;
            }
            Map<String, String> fieldMap = toFieldMap(fields);
            String content = fieldMap.getOrDefault(this.vectorRedisProperties.getContentField(), "");
            Double score = parseScore(fieldMap.get("score"));
            items.add(new VectorSearchItem(id, content, score));
        }
        return items;
    }

    private static Map<String, String> toFieldMap(List<?> fields) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i + 1 < fields.size(); i += 2) {
            map.put(objectToString(fields.get(i)), objectToString(fields.get(i + 1)));
        }
        return map;
    }

    private static Double parseScore(String scoreText) {
        if (scoreText == null || scoreText.isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(scoreText);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String objectToString(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof byte[]) {
            return new String((byte[]) value, StandardCharsets.UTF_8);
        }
        return String.valueOf(value);
    }
}
