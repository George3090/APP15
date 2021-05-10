package com.example.ass15.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String email;
    private String user_id;
    private String username;
    private String age;
    private String FullName;
    private String Password;
    private String PhoneNr;




    public User(String email, String user_id, String username, String age, String FullName, String Password, String PhoneNr) {
        this.email = email;
        this.user_id = user_id;
        this.username = username;
        this.age = age;
        this.FullName = FullName;
        this.Password = Password;
        this.PhoneNr = PhoneNr;
    }

    public User() {

    }

    protected User(Parcel in) {
        email = in.readString();
        user_id = in.readString();
        username = in.readString();
        age = in.readString();
        FullName=in.readString();
        Password = in.readString();

    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };


    public static Creator<User> getCREATOR() {
        return CREATOR;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String FullName) {
        this.FullName = FullName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String Password) {
        this.Password = Password;
    }

    public String getPhoneNr() {
        return PhoneNr;
    }

    public void setPhoneNr(String PhoneNr) {
        this.PhoneNr = PhoneNr;
    }




    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", username='" + username + '\'' +
                ", age='" + age + '\'' +
                ", FullName='" + FullName + '\''+
                ", Password='" + Password + '\''+
                ", PhoneNr='" + PhoneNr + '\''+
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(user_id);
        dest.writeString(username);
        dest.writeString(age);
        dest.writeString(FullName);
        dest.writeString(Password);
        dest.writeString(PhoneNr);
    }



}
