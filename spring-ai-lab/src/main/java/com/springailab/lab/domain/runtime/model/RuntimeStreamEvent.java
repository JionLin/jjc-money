package com.springailab.lab.domain.runtime.model;

import java.util.Map;

public record RuntimeStreamEvent(String name, String content, Map<String, Object> metadata) {
}
