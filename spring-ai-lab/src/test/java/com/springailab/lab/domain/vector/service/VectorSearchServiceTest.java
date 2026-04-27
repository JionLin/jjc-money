package com.springailab.lab.domain.vector.service;

import com.springailab.lab.domain.vector.config.VectorRedisProperties;
import com.springailab.lab.web.dto.VectorSearchItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link VectorSearchService} 行为测试。
 *
 * @author jiaolin
 */
@ExtendWith(MockitoExtension.class)
class VectorSearchServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    private VectorSearchService vectorSearchService;

    /**
     * 初始化被测对象。
     */
    @BeforeEach
    void setUp() {
        VectorRedisProperties properties = new VectorRedisProperties();
        properties.setIndexName("my_vector_index");
        properties.setVectorField("vec");
        properties.setContentField("content");
        properties.setTopK(5);
        this.vectorSearchService = new VectorSearchService(this.stringRedisTemplate, properties);
    }

    /**
     * 空向量应直接返回空结果。
     */
    @Test
    void searchReturnsEmptyWhenQueryVectorEmpty() {
        List<VectorSearchItem> result = this.vectorSearchService.search(new ArrayList<Double>(), 3);
        assertThat(result).isEmpty();
        verifyNoInteractions(this.stringRedisTemplate);
    }

    /**
     * Redis 返回命中时应正确解析 id/content/score。
     */
    @Test
    void searchParsesRedisResult() {
        List<Object> redisRaw = buildRedisRawResult();
        when(this.stringRedisTemplate.execute(any(RedisCallback.class))).thenReturn(redisRaw);

        List<VectorSearchItem> result = this.vectorSearchService.search(Arrays.asList(0.1D, 0.2D, 0.3D), 2);

        assertThat(result).hasSize(1);
        VectorSearchItem item = result.get(0);
        assertThat(item.getId()).isEqualTo("doc:1");
        assertThat(item.getContent()).isEqualTo("肺部感染相关文本");
        assertThat(item.getScore()).isEqualTo(0.0123D);
    }

    private static List<Object> buildRedisRawResult() {
        List<Object> fields = Arrays.asList(
                "content".getBytes(StandardCharsets.UTF_8), "肺部感染相关文本".getBytes(StandardCharsets.UTF_8),
                "score".getBytes(StandardCharsets.UTF_8), "0.0123".getBytes(StandardCharsets.UTF_8));
        return Arrays.asList(1L, "doc:1".getBytes(StandardCharsets.UTF_8), fields);
    }
}
