package com.springailab.lab.domain.runtime.fresh;

import com.springailab.lab.domain.runtime.model.FreshFactRecord;

import java.util.List;

public record FreshDataResult(boolean success,
                              FreshFactRecord factRecord,
                              String degradeReason,
                              List<String> providerAttempts) {
}
