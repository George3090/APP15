package com.example.ass15;

import android.app.Application;

import com.example.ass15.Models.User;


public class UserClient extends Application {

    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}