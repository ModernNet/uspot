package com.modernets.projectpluto;

import android.os.Parcel;
import android.os.Parcelable;


public class InterestPoint implements Parcelable {
    public String name;
    public String category;
    public String description;
    public double lat;
    public double lon;
    public boolean adapted;
    public float distance;

    public InterestPoint() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(category);
        out.writeString(description);
        out.writeDouble(lat);
        out.writeDouble(lon);
        out.writeByte((byte) (adapted ? 1 : 0));
        out.writeFloat(distance);
    }

    public static final Parcelable.Creator<InterestPoint> CREATOR = new Parcelable.Creator<InterestPoint>() {
        public InterestPoint createFromParcel(Parcel in) {
            return new InterestPoint(in);
        }

        public InterestPoint[] newArray(int size) {
            return new InterestPoint[size];
        }
    };

    private InterestPoint(Parcel in) {
        name = in.readString();
        category = in.readString();
        description = in.readString();
        lat = in.readDouble();
        lon = in.readDouble();
        adapted = in.readByte() != 0;
        distance = in.readFloat();
    }
}
