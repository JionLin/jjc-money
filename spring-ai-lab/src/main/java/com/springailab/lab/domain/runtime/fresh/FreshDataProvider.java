package com.springailab.lab.domain.runtime.fresh;

import java.util.Map;
import java.util.Optional;

public interface FreshDataProvider {

    String name();

    Optional<Map<String, Object>> fetch(String ticker);
}
