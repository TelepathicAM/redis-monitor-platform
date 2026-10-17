package com.danuvius.platform.common;

import com.danuvius.platform.common.exception.BusinessCode;

public class ResultUtils {

    /**
     * 成功
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败
     *
     * @param businessCode
     * @return
     */
    public static BaseResponse error(BusinessCode businessCode) {
        return new BaseResponse<>(businessCode);
    }

    /**
     * 失败
     *
     * @param code
     * @param message
     * @return
     */
    public static BaseResponse error(int code, String message) {
        return new BaseResponse(code, null, message);
    }

    /**
     * 失败
     *
     * @param businessCode
     * @return
     */
    public static BaseResponse error(BusinessCode businessCode, String message) {
        return new BaseResponse(businessCode.getCode(), null, message);
    }
}
