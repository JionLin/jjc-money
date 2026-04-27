package com.springailab.lab.web;

import com.springailab.lab.domain.vector.service.VectorSearchService;
import com.springailab.lab.web.dto.VectorSearchItem;
import com.springailab.lab.web.dto.VectorSearchRequest;
import com.springailab.lab.web.dto.VectorSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 向量检索 API（项目内）。
 *
 * @author jiaolin
 */
@RestController
@RequestMapping("/api/vector")
public class VectorSearchController {

    private static final Logger log = LoggerFactory.getLogger(VectorSearchController.class);

    private final VectorSearchService vectorSearchService;

    public VectorSearchController(VectorSearchService vectorSearchService) {
        this.vectorSearchService = vectorSearchService;
    }

    /**
     * 向量检索。
     *
     * @param request 请求体
     * @return 命中结果
     */
    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public VectorSearchResponse search(@RequestBody VectorSearchRequest request) {
        int dim = request.getQueryVector() == null ? 0 : request.getQueryVector().size();
        log.info("Vector search request received, dim={}, topK={}", dim, request.getTopK());
        List<VectorSearchItem> items = this.vectorSearchService.search(request.getQueryVector(), request.getTopK());
        return new VectorSearchResponse(items);
    }
}
