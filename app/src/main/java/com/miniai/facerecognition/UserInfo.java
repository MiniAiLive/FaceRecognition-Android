package com.miniai.facerecognition;

import android.graphics.Bitmap;

public class UserInfo {

    public String userName;
    public Bitmap faceImage;
    public byte[] featData;
    public int userId;

    public UserInfo() {

    }

    public UserInfo(int userId, String userName, Bitmap faceImage, byte[] featData) {
        this.userId = userId;
        this.userName = userName;
        this.faceImage = faceImage;
        this.featData = featData;
    }
}
