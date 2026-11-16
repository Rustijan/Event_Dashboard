package com.epam.eventhub.model;

import java.util.Objects;

public class User{

    private Long id;
    private String fullName;
    private String email;
    private String passwordHash;
    private Role role;

    public User(){}
    public User(Long id, String fullName, String email,
                String passwordHash, Role role){
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;

    }
    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null){
            return false;
        }
        if(obj == this){
            return true;
        }
        if(obj.getClass() != this.getClass()){
            return false;
        }
        User other = (User) obj;
        return Objects.equals(this.id, other.id)
                && Objects.equals(this.fullName, other.fullName)
                && Objects.equals(this.email, other.email)
                && Objects.equals(this.role, other.role);

    }
    @Override
    public int hashCode(){
        return Objects.hash(this.id, this.fullName, this.email,  this.role);
    }
    @Override
    public String toString(){
        return "User [id=" +this.getId() + ", fullName=" + this.fullName + ", email=" + this.email + ", role=" + this.role + "]";
    }

}