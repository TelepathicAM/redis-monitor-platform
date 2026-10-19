package com.danuvius.platform.common.exception;

/**
 * 参数校验异常
 */
public class ParameterException extends RuntimeException {

    private Integer code;
    private String msg;

    public ParameterException() {
        super();
    }

    public ParameterException(String msg) {
        super(msg);
        this.code = ParameterCode.PARAM_ERROR.getCode();
        this.msg = msg;
    }

    public ParameterException(ParameterCode parameterCode) {
        super(parameterCode.getMsg());
        this.code = parameterCode.getCode();
        this.msg = parameterCode.getMsg();
    }

    public ParameterException(ParameterCode parameterCode, String msg) {
        super(msg);
        this.code = parameterCode.getCode();
        this.msg = msg;
    }

    public ParameterException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}