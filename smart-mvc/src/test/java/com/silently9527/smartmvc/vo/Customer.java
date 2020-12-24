package com.silently9527.smartmvc.vo;

public class Customer {
    private String name;
    private int age;
    private String area;

    public Customer(String name, int age, String area) {
        this.name = name;
        this.age = age;
        this.area = area;
    }

    public String getArea() {
        return area;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "name='" + name + '\'' +
                '}';
    }
}
