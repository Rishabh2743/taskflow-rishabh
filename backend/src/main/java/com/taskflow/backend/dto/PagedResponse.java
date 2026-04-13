package com.taskflow.backend.dto;

import java.util.List;

public class PagedResponse<T> {

    private List<T> data;
    private int page;
    private int limit;
    private long total;
    private int totalPages;

    public PagedResponse(List<T> data, int page, int limit, long total) {
        this.data = data;
        this.page = page;
        this.limit = limit;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / limit);
    }

    public List<T> getData() { return data; }
    public int getPage() { return page; }
    public int getLimit() { return limit; }
    public long getTotal() { return total; }
    public int getTotalPages() { return totalPages; }
}