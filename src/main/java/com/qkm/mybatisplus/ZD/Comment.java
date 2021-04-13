package com.qkm.mybatisplus.ZD;

import java.io.Serializable;

/**
 * null
 * @TableName comment
 */
public class Comment implements Serializable {
    /**
     * 
     */
    private String comment;

    /**
     * 
     */
    private String creatat;

    /**
     * 
     */
    private String blogcreatatandname;

    /**
     * 
     */
    private String bossid;

    private static final long serialVersionUID = 1L;

    /**
     */
    public String getComment() {
        return comment;
    }

    /**
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     */
    public String getCreatat() {
        return creatat;
    }

    /**
     */
    public void setCreatat(String creatat) {
        this.creatat = creatat;
    }

    /**
     */
    public String getBlogcreatatandname() {
        return blogcreatatandname;
    }

    /**
     */
    public void setBlogcreatatandname(String blogcreatatandname) {
        this.blogcreatatandname = blogcreatatandname;
    }

    /**
     */
    public String getBossid() {
        return bossid;
    }

    /**
     */
    public void setBossid(String bossid) {
        this.bossid = bossid;
    }
}