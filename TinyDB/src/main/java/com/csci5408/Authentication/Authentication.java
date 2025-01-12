package com.csci5408.Authentication;

import com.csci5408.OS.FileOperations;
import com.csci5408.OS.IO;
import com.csci5408.Models.User;
import com.csci5408.OS.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Authentication {

    private final String USER_PROFILE_FILE_PATH = "DataStore/User_Profile.txt";
    private List<User> registeredUsers;
    private boolean isUserProfileFileLoaded = false;
    private SessionManager sessionManager;
    private final String CAPTCHA_CHARACTERS = "abcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*";
    private FileOperations fileOperations;
    private Logger logger;

    public Authentication() {
        this.sessionManager = new SessionManager();
        this.fileOperations = new FileOperations();
        this.logger = Logger.getInstance();
    }

    public List<User> loadUsers(){

        if (this.isUserProfileFileLoaded){
            return this.registeredUsers;
        }

        if (registeredUsers != null && !registeredUsers.isEmpty()){
            this.registeredUsers.clear();
        }

        List<String> content = fileOperations.read(USER_PROFILE_FILE_PATH);

        List<User> users = decodeUsers(content);

        this.registeredUsers = users;

        this.isUserProfileFileLoaded = true;

        return users;
    }

    private List<User> decodeUsers(List<String> content){

        List<User> users = new ArrayList<>();

        List<String> securityQueAns = new ArrayList<>();

        for (String currLine : content){

            if (currLine.startsWith("@")){
                String[] queAns = currLine.substring(1).split("#");
                securityQueAns.add(queAns[0]);
                securityQueAns.add(queAns[1]);
            }
            else{
                if (!users.isEmpty()){
                    User user = users.get(users.size()-1);
                    user.setSecurityQuestions(new ArrayList<>(securityQueAns));
                    securityQueAns.clear();
                }

                String[] IDPass = currLine.split("#");
                users.add(new User(IDPass[0], IDPass[1]));
            }
        }

        if (!users.isEmpty()){
            User user = users.get(users.size()-1);
            user.setSecurityQuestions(new ArrayList<>(securityQueAns));
            securityQueAns.clear();
        }

        return users;
    }

    public boolean registerUser(User user){

        if (isUserExist(user.getUserID())){
            IO.println("UserID already exist!");
            return false;
        }

        List<String> content = new ArrayList<>();

        user.setPassword(hashPassword(user.getPassword()));

        content.add(user.getUserID() + "#" + user.getPassword());

        List<String> securityQueAns = user.getSecurityQuestions();
        content.add("@" + securityQueAns.get(0) + "#" + securityQueAns.get(1));
        content.add("@" + securityQueAns.get(2) + "#" + securityQueAns.get(3));

        boolean response  = fileOperations.write(USER_PROFILE_FILE_PATH, content, false);

        if (registeredUsers != null){
            registeredUsers.add(user);
        }
        else {
            registeredUsers = new ArrayList<>();
            registeredUsers.add(user);
        }

        return response;
    }

    public boolean loginUser(String userID, String password){

        if (isUserExist(userID)){
            User user = isUserValid(userID, password);
            if (user != null){
                if (verifySecurityQuestion(user)){
                    boolean isCaptchaValid = captchaVerification();
                    if (isCaptchaValid) {
                        if (this.sessionManager.login(userID)){
                            logger.logEvent("Successful", "User '" + userID + "' logged in successfully.");
                            IO.println("Login successful!");
                            return true;
                        }
                        return false;
                    } else {
                        logger.logEvent("Failed", "User '" + userID + "' failed captcha verification.");
                        IO.println("Invalid captcha");
                        return false;
                    }
                }
                else {
                    IO.println("Wrong Security Answer!");
                    return false;
                }
            }
            else {
                IO.println("Incorrect Credentials!");
                return false;
            }
        }
        else {
            IO.println("User Doesn't Exist!");
            return false;
        }
    }


    private boolean verifySecurityQuestion(User user){

        List<String> securityQuestions = user.getSecurityQuestions();

        Random random = new Random();
        int randomQueNo = random.nextInt(2) + 1;

        String randomQuestion = null;
        String answer = null;

        if (randomQueNo == 1){
            randomQuestion = securityQuestions.get(0);
            answer = securityQuestions.get(1);
        }
        else {
            randomQuestion = securityQuestions.get(2);
            answer = securityQuestions.get(3);
        }

        IO.print(randomQuestion + "?\n");
        String providedAnswer = IO.readLine();

        if(providedAnswer.equals(answer)){
            return true;
        }

        return false;
    }

    private String hashPassword(String password){

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] result = md.digest(password.getBytes());

            return Base64.getEncoder().encodeToString(result);
        }
        catch (NoSuchAlgorithmException exception){
            IO.println("MD5 Algo Not Found!");
            return null;
        }
    }

    private User isUserValid(String userID, String password){

        if (registeredUsers == null || registeredUsers.isEmpty()){
            loadUsers();
        }

        for (User user : registeredUsers){
            if (user.getUserID().equals(userID) && user.getPassword().equals(hashPassword(password))){
                return user;
            }
        }

        return null;
    }

    private boolean isUserExist(String userID){

        if (registeredUsers == null || registeredUsers.isEmpty()){
            loadUsers();
        }

        for (User user : registeredUsers){
            if (user.getUserID().equals(userID)){
                return true;
            }
        }

        return false;

    }

    public boolean captchaVerification() {
        String expectedCaptcha = generate(5);

        IO.print("Captcha: " + expectedCaptcha);

        IO.print("\nPlease enter above captcha: ");

        String inputCaptcha = IO.readLine();

        return inputCaptcha.equals(expectedCaptcha);
    }

    public String generate(int length) {
        StringBuilder captcha = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            captcha.append(CAPTCHA_CHARACTERS.charAt(random.nextInt(CAPTCHA_CHARACTERS.length())));
        }
        return captcha.toString();
    }

    public boolean logOutUser(String userID){
        if (this.sessionManager.logout(userID)){
            IO.println("User logged out successfully!");
            return true;
        }
        return false;
    }

}
