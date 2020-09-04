package com.nowcoder.community.entity;

// 封装分页相关的信息
public class Page {

    // 当前页码
    private int current = 1;
    // 显示上限
    private int limit = 10;
    // 数据总数(用于计算总页数)
    private int rows;
    // 查询路径(用于复用分页链接)使得页面上写的逻辑更清晰简洁一点,直接在Page中就已经存好的,否则的话就要在html模板中进行跳转页面地址的拼接
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    // 设置为最多100条 一方面防止服务器放回给浏览器太多数据 浏览器直接卡死了,一方面防止服务器查询如此多数据压力太大
    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行
     *
     * @return
     */
    public int getOffset() {
        // current * limit - limit
        return (current - 1) * limit;
    }

    /**
     * 获取总页数
     *
     * @return
     */
    public int getTotal() {
        // rows / limit [+1]
        if (rows % limit == 0) {
            return rows / limit;
        } else {
            return rows / limit + 1;
        }
    }

    /**
     * 获取起始页码
     *
     * @return
     */
    public int getFrom() {
        int from = current - 2;
        return from < 1 ? 1 : from;
    }

    /**
     * 获取结束页码
     *
     * @return
     */
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return to > total ? total : to;
    }


}
