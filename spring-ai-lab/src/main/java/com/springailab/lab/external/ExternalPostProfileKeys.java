package com.springailab.lab.external;

/**
 * 外部 POST JSON 端点 profile 键（与 {@code lab.external.post.endpoints.*} 对齐）。
 *
 * @author jiaolin
 */
public final class ExternalPostProfileKeys {

    /**
     * 天气类契约。
     */
    public static final String WEATHER = "weather";

    /**
     * 演示回显类契约。
     */
    public static final String DEMO_ECHO = "demo-echo";

    /**
     * 用户名查询契约。
     */
    public static final String USER_QUERY = "user-query";

    /**
     * 向量检索契约。
     */
    public static final String VECTOR_SEARCH = "vector-search";

    private ExternalPostProfileKeys() {
    }
}
