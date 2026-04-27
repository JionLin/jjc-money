package com.springailab.lab.web.dto;

/**
 * 向量检索命中项。
 *
 * @author jiaolin
 */
public class VectorSearchItem {

    private String id;

    private String content;

    private Double score;

    public VectorSearchItem(String id, String content, Double score) {
        this.id = id;
        this.content = content;
        this.score = score;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Double getScore() {
        return this.score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
