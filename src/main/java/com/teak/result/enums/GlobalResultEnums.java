package com.teak.result.enums;


/**
 * The enum Global result enums.
 *
 * @author 柚mingle木
 * @version 1.0
 * @date 2023 /2/19
 */
public enum GlobalResultEnums {
    /**
     * Success global result enums.
     */
    SUCCESS(200, "操作成功 "),
    /**
     * Forward global result enums.
     */
    FORWARD(302, "转发"),
    /**
     * Fail global result enums.
     */
    FAIL(400, "操作失败");

    private final Integer code;
    private final String message;

    GlobalResultEnums(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Gets code.
     *
     * @return the code
     */
    public Integer getCode() {
        return code;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }
}
