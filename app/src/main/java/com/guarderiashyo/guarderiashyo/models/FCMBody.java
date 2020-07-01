package com.guarderiashyo.guarderiashyo.models;

import java.util.Map;



public class FCMBody {
    private String to;
    private String priority;
    private String ttl;
    Map<String, String> data;

    public FCMBody(String to, String priority, String ttl, Map<String, String> data) {
        this.to = to;
        this.priority = priority;
        this.data = data;
        this.ttl = ttl;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }


    // Getter Methods

    public String getTo() {
        return to;
    }

    public String getPriority() {
        return priority;
    }



    // Setter Methods

    public void setTo( String to ) {
        this.to = to;
    }

    public void setPriority( String priority ) {
        this.priority = priority;
    }
    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }

}