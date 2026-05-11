package com.example.gon;

public class Friend {
    private String userID;
    private String username;

    public Friend(String userID, String username){
        this.username = username;
        this.userID = userID;
    }



    public String getUsername(){
        return username;
    }

    public String getUserID(){
        return userID;
    }
}
