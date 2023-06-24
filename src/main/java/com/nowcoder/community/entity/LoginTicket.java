package com.nowcoder.community.entity;

import java.util.Date;


/**
 * (LoginTicket)表实体类
 *
 * @author makejava
 * @since 2023-03-08 17:04:18
 */
@SuppressWarnings("serial")


public class LoginTicket  {

    private Integer id;

    
    private Integer userId;
    
    private String ticket;
    //0-有效; 1-无效;
    private Integer status;
    
    private Date expired;

    @Override
    public String toString() {
        return "LoginTicket{" +
                "id=" + id +
                ", userId=" + userId +
                ", ticket='" + ticket + '\'' +
                ", status=" + status +
                ", expired=" + expired +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getExpired() {
        return expired;
    }

    public void setExpired(Date expired) {
        this.expired = expired;
    }
}

