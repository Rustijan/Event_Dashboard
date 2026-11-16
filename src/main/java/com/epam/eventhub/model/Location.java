package com.epam.eventhub.model;

import java.util.Objects;

public class Location {
    private Long id;
    private String name;
    private String city;
    private String address;
    private int capacity;


    public Location(){}
    public Location(Long id, String name, String city, String address, int capacity) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.capacity = capacity;
    }


    public Long getId(){
        return this.id;
    }
    public void setId(Long id){
        this.id = id;
    }
    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getCity(){
        return this.city;
    }
    public void setCity(String city){
        this.city = city;
    }
    public String getAddress(){
        return this.address;
    }
    public void setAddress(String address){
        this.address = address;
    }
    public int getCapacity(){
        return this.capacity;
    }
    public void setCapacity(int capacity){
        this.capacity = capacity;
    }

    @Override
    public int  hashCode() {
        return Objects.hash(id,  name, city, address, capacity);
    }
    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        Location other = (Location)obj;
        return Objects.equals(this.id, other.id)
                && Objects.equals(this.name, other.name)
                && Objects.equals(this.city, other.city)
                && Objects.equals(this.address, other.address)
                && this.capacity == other.capacity;
    }

    @Override
    public String toString(){
        return "Location [id=" + id + ", name=" + name + ", city=" + city + ", address=" + address + ", capacity=" + capacity + "]";
    }

}
