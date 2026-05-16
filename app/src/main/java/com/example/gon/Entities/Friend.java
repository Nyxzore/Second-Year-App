package com.example.gon.Entities;

public class Friend {
    private String userID;
    private String username;

    private String status;
    private int profilePicIndex;

    public Friend(String status, String userID, String username, int profilePicIndex){
        this.status = status;
        this.username = username;
        this.userID = userID;
        this.profilePicIndex = profilePicIndex;
    }

    public String getStatus(){ return status;}
    public String getUsername(){
        return username;
    }

    public String getUserID(){
        return userID;
    }

    public int getProfilePicIndex() {
        return profilePicIndex;
    }
}
