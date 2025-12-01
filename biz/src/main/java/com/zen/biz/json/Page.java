package com.zen.biz.json;

public class Page {
    /*    {
            "page":"1",
                "pageSize":"20"，
            "updatetime":"2018-02-01 00:00:00"
        }*/
    private String page;
    private String pageSize;
    private String updatetime;

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    public String getPageSize() {
        return pageSize;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getPage() {
        return page;
    }
}
