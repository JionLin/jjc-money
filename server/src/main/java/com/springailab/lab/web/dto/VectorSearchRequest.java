package com.springailab.lab.web.dto;

import java.util.List;

/**
 * 向量检索请求。
 *
 * @author jiaolin
 */
public class VectorSearchRequest {

    private List<Double> queryVector;

    private Integer topK;

    public List<Double> getQueryVector() {
        return this.queryVector;
    }

    public void setQueryVector(List<Double> queryVector) {
        this.queryVector = queryVector;
    }

    public Integer getTopK() {
        return this.topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }
}
