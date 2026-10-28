package com.danuvius.platform.common.core;

import cn.hutool.http.HttpStatus;
import com.danuvius.platform.common.exception.ErrorCode;
import lombok.*;
import lombok.experimental.Accessors;


import java.io.Serializable;

@Builder
@ToString
@Accessors(chain = true)
@AllArgsConstructor
public class ResultBean<T> implements Serializable{
    private  static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private  int code= HttpStatus.HTTP_OK;
    @Getter
    @Setter
    private String msg="success";
    @Getter
    @Setter
    private T data;

    public ResultBean() {super();}

    public ResultBean(T data) {
        super();
        this.data = data;
    }
    public ResultBean(T data, String msg) {
        super();
        this.data = data;
        this.msg = msg;
    }

    public ResultBean(Throwable e) {
        super();
        this.code = HttpStatus.HTTP_INTERNAL_ERROR;
        this.msg = e.getMessage();
    }

    public static <T> ResultBean<T> resultBean(T data,int code,String msg) {
        ResultBean<T> resultBean = new ResultBean<T>();
        resultBean.setCode(code);
        resultBean.setMsg(msg);
        resultBean.setData(data);
        return resultBean;
    }
    public static <T> ResultBean<T> restResult(T data, ErrorCode errorCode) {
        return resultBean(data, errorCode.getCode(), errorCode.getMsg());
    }
}
