package com.example.valorquest.model;

import java.util.ArrayList;
import java.util.List;

public class Alliance {
    private String id;
    private String name;
    private String leaderId;
    private List<String> members = new ArrayList<>();

    public Alliance(String id, String name, String leaderId) {
        this.id = id;
        this.name = name;
        this.leaderId = leaderId;
    }

    public Alliance(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}
