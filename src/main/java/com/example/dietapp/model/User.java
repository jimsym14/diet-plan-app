package com.example.dietapp.model;

public class User {
    private double weight, height;
    private int age;
    private String gender;

    public User(double weight, double height, int age, String gender) {

        if (weight < 30 || weight > 200) {
            throw new IllegalArgumentException("Weight must be between 30 and 200 kg");
        }
        if (age < 5 || age > 100) {
            throw new IllegalArgumentException("Age must be between 5 and 100");
        }
        this.weight = weight;
        this.height = height;
        this.age = age;
        this.gender = gender;
    }

    public double getWeight() { return weight; }
    public double getHeight() { return height; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
}