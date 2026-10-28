package com.danuvius.platform.common.exception;

public enum ErrorCode {
    OK(200,"OK");
    private final int code;
    private final String msg;
    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public int getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }
    public String toString() {return String.format("ErrorCode{code=%d,msg='%s'}", code, msg);}
}
