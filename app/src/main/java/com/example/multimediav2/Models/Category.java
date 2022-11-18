package com.example.multimediav2.Models;

import java.util.List;

public class Category {
    private Long id;

    private Long orgID;

    private String name;

    private Long parent_id;

    private List<Category> child_list;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrgID() {
        return orgID;
    }

    public void setOrgID(Long orgID) {
        this.orgID = orgID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParent_id() {
        return parent_id;
    }

    public void setParent_id(Long parent_id) {
        this.parent_id = parent_id;
    }

    public List<Category> getChild_list() {
        return child_list;
    }

    public void setChild_list(List<Category> child_list) {
        this.child_list = child_list;
    }
}
