package com.danuvius.platform.util.http;

import lombok.Data;

import java.io.Serializable;

/**
 * 执行器HTTP 统一响应体。
 */
@Data
public class AgentCollectResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;

    private String message;

    private T data;
}
