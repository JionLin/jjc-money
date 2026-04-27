package com.springailab.lab.domain.vector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redis 向量检索配置。
 *
 * @author jiaolin
 */
@Component
@ConfigurationProperties(prefix = "lab.vector.redis")
public class VectorRedisProperties {

    private String indexName;

    private String vectorField;

    private String contentField;

    private Integer topK;

    private String distanceMetric;

    public String getIndexName() {
        return this.indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getVectorField() {
        return this.vectorField;
    }

    public void setVectorField(String vectorField) {
        this.vectorField = vectorField;
    }

    public String getContentField() {
        return this.contentField;
    }

    public void setContentField(String contentField) {
        this.contentField = contentField;
    }

    public Integer getTopK() {
        return this.topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public String getDistanceMetric() {
        return this.distanceMetric;
    }

    public void setDistanceMetric(String distanceMetric) {
        this.distanceMetric = distanceMetric;
    }
}
