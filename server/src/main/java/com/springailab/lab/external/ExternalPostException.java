package com.springailab.lab.external;

/**
 * 统一 POST JSON 出站失败时抛出的异常（不向模型暴露堆栈）。
 *
 * @author jiaolin
 */
public class ExternalPostException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String shortUserMessage;

    /**
     * @param shortUserMessage 给模型或调用方的短描述
     * @param cause            原始原因（可 null）
     */
    public ExternalPostException(String shortUserMessage, Throwable cause) {
        super(shortUserMessage, cause, false, false);
        this.shortUserMessage = shortUserMessage;
    }

    /**
     * @return 短描述（与 {@link #getMessage()} 一致）
     */
    public String getShortUserMessage() {
        return this.shortUserMessage;
    }
}
