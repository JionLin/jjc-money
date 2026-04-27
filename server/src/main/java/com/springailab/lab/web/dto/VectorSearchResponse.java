package com.springailab.lab.web.dto;

import java.util.List;

/**
 * 向量检索响应。
 *
 * @author jiaolin
 */
public class VectorSearchResponse {

    private List<VectorSearchItem> items;

    public VectorSearchResponse(List<VectorSearchItem> items) {
        this.items = items;
    }

    public List<VectorSearchItem> getItems() {
        return this.items;
    }

    public void setItems(List<VectorSearchItem> items) {
        this.items = items;
    }
}
