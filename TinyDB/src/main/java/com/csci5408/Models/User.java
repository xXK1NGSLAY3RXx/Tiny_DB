package com.csci5408.Models;

import java.util.List;
import java.util.Map;

public class User {

    private String userID;
    private String password;
    private List<String> securityQuestions;
    private boolean status;

    public User(){}

    public User(String userID, String password) {
        this.userID = userID;
        this.password = password;
    }

    public User(String userID, String password, List<String> securityQuestions) {
        this.userID = userID;
        this.password = password;
        this.securityQuestions = securityQuestions;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getSecurityQuestions() {
        return securityQuestions;
    }

    public void setSecurityQuestions(List<String> securityQuestions) {
        this.securityQuestions = securityQuestions;
    }

    @Override
    public String toString() {
        return "User{" +
                "userID='" + userID + '\'' +
                ", password='" + password + '\'' +
                ", securityQuestions=" + securityQuestions +
                '}';
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
