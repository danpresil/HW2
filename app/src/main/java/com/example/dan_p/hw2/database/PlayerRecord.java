package com.example.dan_p.hw2.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dan_p on 1/7/2017.
 */
public class PlayerRecord implements Parcelable {

    public static int ID = 1;
    private String id;
    private String name;
    private String difficulty;
    private int time;
    private double latitude;
    private double longitude;
    private double rotation;

    public PlayerRecord(){
    }

    public PlayerRecord(String name, String difficulty, int time, double latitude, double longitude, double rotation){
        this.name = name;
        this.difficulty = difficulty;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rotation = rotation;
    }
    public PlayerRecord(String id, String name, String _lName, String difficulty, int time, double latitude, double longitude, double rotation){
        this.id = id;
        this.name = name;
        this.difficulty = difficulty;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rotation = rotation;
    }

    protected PlayerRecord(Parcel in) {
        id = in.readString();
        name = in.readString();
        difficulty = in.readString();
        time = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        rotation = in.readDouble();
    }

    public static final Creator<PlayerRecord> CREATOR = new Creator<PlayerRecord>() {
        @Override
        public PlayerRecord createFromParcel(Parcel in) {
            return new PlayerRecord(in);
        }

        @Override
        public PlayerRecord[] newArray(int size) {
            return new PlayerRecord[size];
        }
    };

    public String getID() {
        return id;
    }

    public void setID(String _id) {
        this.id = _id;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getDifficulty() {return difficulty;}

    public void setDifficulty(String difficulty) {this.difficulty = difficulty;}

    public int getTime() {return time;}

    public void setTime(int time) {this.time = time;}

    public double getLatitude() {return latitude;}

    public void setLatitude(double latitude) {this.latitude = latitude;}

    public double getLongitude() {return longitude;}

    public void setLongitude(double longitude) {this.longitude = longitude;}

    public double getRotation() {return rotation;}

    public void setRotation(double rotation) {this.rotation = rotation;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(difficulty);
        dest.writeInt(time);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(rotation);
    }

    @Override
    public String toString() {
        return "PlayerRecord{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", time=" + time +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", rotation=" + rotation +
                '}';
    }
}