package com.appmusicale.model;

import javafx.beans.property.*;

//MODELLO DEL MEMBRO (USER-ADMIN)

public class Member {

    private Integer id;
    private String username;
    private String email;
    private String password;
    private Role role;
    private Status status;

    //id
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    //username
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) { this.username = username; }

    //email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) { this.email = email; }


    //password
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) { this.password = password; }

    //ruolo
    public Role getRole() {
        return role;
    }
    public void setRole(Role role) { this.role = role; }

    //stato
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) { this.status = status; }

    //toString
    @Override
    public String toString() {
        return username + " (" + email + ")";
    }
}