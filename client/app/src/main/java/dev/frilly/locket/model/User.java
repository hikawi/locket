package dev.frilly.locket.model;

import com.google.firebase.Timestamp;

public class User {

    private String userId;
    private String username;
    private String email;
    private String password;
    private String dob;
    private Timestamp createdTimestamp;

    public User() {
    }

    public User(String dob, String username, String email, String password, Timestamp createdTimestamp, String userId) {
        this.dob = dob;
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdTimestamp = createdTimestamp;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public void setText(String username) {
    }
}
