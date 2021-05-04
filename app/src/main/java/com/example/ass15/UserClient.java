package com.example.ass15;

import android.app.Application;

import com.example.ass15.Models.User;
import com.example.ass15.Models.UserLocation;


public class UserClient extends Application {
    // test
    private UserLocation userLocation = null;
    public UserLocation getUserLocation(){return userLocation;}
    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }



    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}