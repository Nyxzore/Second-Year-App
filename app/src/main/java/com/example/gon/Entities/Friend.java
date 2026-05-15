package com.example.gon.Entities;

public class Friend {
    private String userID;
    private String username;

    private String status;
    public Friend(String status,String userID, String username){
        this.status = status;
        this.username = username;
        this.userID = userID;

    }


    public String getStatus(){ return status;}
    public String getUsername(){
        return username;
    }

    public String getUserID(){
        return userID;
    }
}
