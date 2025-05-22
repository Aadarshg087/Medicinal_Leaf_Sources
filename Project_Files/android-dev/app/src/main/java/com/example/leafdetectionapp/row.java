package com.example.leafdetectionapp;

public class row {

    // 7- Creating row class as a model class
    private int img;
    private String name;

    // constructor
    public row(int img,String name) {
        this.img = img;
        this.name = name;
    }

    // Getters & Setters


    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
