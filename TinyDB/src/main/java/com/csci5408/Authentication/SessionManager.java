package com.csci5408.Authentication;

import com.csci5408.Models.User;
import com.csci5408.OS.IO;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    private List<User> userSessions;

    public SessionManager() {
        this.userSessions = new ArrayList<>();
    }

    public boolean login(String userID) {

        User checkUser = isUserPresent(userID);

        if(checkUser != null){
            IO.println("User is already logged In!");
            return false;
        }

        User user = new User();
        user.setUserID(userID);
        user.setStatus(true);
        userSessions.add(user);

        return true;
    }

    public boolean logout(String userID) {

        User user = isUserPresent(userID);

        if (user == null){
            IO.println("User is not logged In!");
            return false;
        }

        userSessions.remove(user);
        return true;
    }

    public User isUserPresent(String userID){

        for (User user : userSessions){
            if (user.getUserID().equals(userID)){
                return user;
            }
        }

        return null;
    }
}
