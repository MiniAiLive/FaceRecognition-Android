package com.miniai.facerecognition;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.miniai.facerecognition.UserInfo;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class UserDB extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "users.db";
    public static final String CONTACTS_TABLE_NAME = "users";
    public static final String CONTACTS_COLUMN_ID = "userId";
    public static final String CONTACTS_COLUMN_NAME = "userName";
    public static final String CONTACTS_COLUMN_FACE = "faceImage";
    public static final String CONTACTS_COLUMN_FEATURE = "featData";

    public static ArrayList<UserInfo> userInfos = new ArrayList<UserInfo>();

    private Context appCtx;

    public UserDB(Context context) {
        super(context, DATABASE_NAME , null, 1);
        appCtx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table users " +
                        "(userId integer primary key autoincrement,  userName text, faceImage blob, featData blob)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public int insertUser (String userName, Bitmap faceImage, byte[] featData) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        faceImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] face = byteArrayOutputStream.toByteArray();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("userName", userName);
        contentValues.put("faceImage", face);
        contentValues.put("featData", featData);
        db.insert("users", null, contentValues);

        Cursor res =  db.rawQuery( "select last_insert_rowid() from users", null );
        res.moveToFirst();

        int userId = 0;
        while(res.isAfterLast() == false){
            userId = res.getInt(0);
            res.moveToNext();
        }
        return userId;
    }

    public int getLastUserId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select max(userId) from users", null );
        res.moveToFirst();

        int userId = 0;
        while(res.isAfterLast() == false){
            userId = res.getInt(0);
            res.moveToNext();
        }
        return userId;
    }

    public Integer deleteUser (String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("users",
                "userName = ? ",
                new String[] { name });
    }

    public Integer deleteAllUser () {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ CONTACTS_TABLE_NAME);
        return 0;
    }

    public void loadUsers() {
        userInfos.clear();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from users", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            int userId = res.getInt(res.getColumnIndex(CONTACTS_COLUMN_ID));
            String userName = res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME));
            byte[] faceData = res.getBlob(res.getColumnIndex(CONTACTS_COLUMN_FACE));
            byte[] featData = res.getBlob(res.getColumnIndex(CONTACTS_COLUMN_FEATURE));
            Bitmap faceImage = BitmapFactory.decodeByteArray(faceData, 0, faceData.length);

            UserInfo face = new UserInfo(userId, userName, faceImage, featData);
            userInfos.add(face);

            res.moveToNext();
        }
    }
}