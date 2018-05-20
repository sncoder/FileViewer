package cn.sncoder.fv.entity;

import com.alibaba.fastjson.JSONObject;

public class Pager<T> {

    private int pageSize;

    private int currentPage;

    private int total;

    private T data;

    public Pager(int pageSize, int currentPage, int total, T data) {
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.total = total;
        this.data = data;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("pageSize", pageSize);
        json.put("currentPage", currentPage);
        json.put("total", total);
        return json;
    }
}
