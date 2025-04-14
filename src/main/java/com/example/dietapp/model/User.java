package com.example.dietapp.model;

public class User {
    private double weight, height;
    private int age;
    private String gender;
private int num;
    public User(double weight, double height, int age, String gender) {
        this.weight = weight;
        this.height = height;
        this.age = age;
        this.gender = gender;
       this.num=num;
    }

    public double getWeight() { return weight; }
    public double getHeight() { return height; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
}