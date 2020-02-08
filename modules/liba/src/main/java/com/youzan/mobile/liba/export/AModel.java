package com.youzan.mobile.liba.export;

import android.os.Parcel;
import android.os.Parcelable;

import com.youzan.mobile.lib_common.annotation.Export;

@Export
public class AModel implements Parcelable {

    public String name;
    public String age;

    public AModel(String name, String age) {
        this.name = name;
        this.age = age;
    }

    protected AModel(Parcel in) {
        name = in.readString();
        age = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(age);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AModel> CREATOR = new Creator<AModel>() {
        @Override
        public AModel createFromParcel(Parcel in) {
            return new AModel(in);
        }

        @Override
        public AModel[] newArray(int size) {
            return new AModel[size];
        }
    };
}
