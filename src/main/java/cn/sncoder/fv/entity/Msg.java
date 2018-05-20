package cn.sncoder.fv.entity;

import com.alibaba.fastjson.JSONObject;

/**
 * 保存从服务器返回到用户的消息
 *
 * Created by shibiao on 2017/7/18.
 */
public class Msg {

    private boolean success;

    private String msg;

    private Object obj;

    public Msg(boolean success) {
        this.success = success;
    }

    public Msg(String msg) {
        this.msg = msg;
    }

    public Msg(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public Msg(boolean success, Object obj) {
        this.success = success;
        this.obj = obj;
    }

    public Msg(boolean success, String msg, Object obj) {
        this.success = success;
        this.msg = msg;
        this.obj = obj;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
