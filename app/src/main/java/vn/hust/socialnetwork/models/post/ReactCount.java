package vn.hust.socialnetwork.models.post;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReactCount implements Parcelable {
    @SerializedName("react_type_1")
    @Expose
    private int reactType1;
    @SerializedName("react_type_2")
    @Expose
    private int reactType2;
    @SerializedName("react_type_3")
    @Expose
    private int reactType3;
    @SerializedName("react_type_4")
    @Expose
    private int reactType4;
    @SerializedName("react_type_5")
    @Expose
    private int reactType5;
    @SerializedName("react_type_6")
    @Expose
    private int reactType6;

    public ReactCount(int reactType1, int reactType2, int reactType3, int reactType4, int reactType5, int reactType6) {
        this.reactType1 = reactType1;
        this.reactType2 = reactType2;
        this.reactType3 = reactType3;
        this.reactType4 = reactType4;
        this.reactType5 = reactType5;
        this.reactType6 = reactType6;
    }

    protected ReactCount(Parcel in) {
        reactType1 = in.readInt();
        reactType2 = in.readInt();
        reactType3 = in.readInt();
        reactType4 = in.readInt();
        reactType5 = in.readInt();
        reactType6 = in.readInt();
    }

    public static final Creator<ReactCount> CREATOR = new Creator<ReactCount>() {
        @Override
        public ReactCount createFromParcel(Parcel in) {
            return new ReactCount(in);
        }

        @Override
        public ReactCount[] newArray(int size) {
            return new ReactCount[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(reactType1);
        parcel.writeInt(reactType2);
        parcel.writeInt(reactType3);
        parcel.writeInt(reactType4);
        parcel.writeInt(reactType5);
        parcel.writeInt(reactType6);
    }

    public int getReactType1() {
        return reactType1;
    }

    public void setReactType1(int reactType1) {
        this.reactType1 = reactType1;
    }

    public int getReactType2() {
        return reactType2;
    }

    public void setReactType2(int reactType2) {
        this.reactType2 = reactType2;
    }

    public int getReactType3() {
        return reactType3;
    }

    public void setReactType3(int reactType3) {
        this.reactType3 = reactType3;
    }

    public int getReactType4() {
        return reactType4;
    }

    public void setReactType4(int reactType4) {
        this.reactType4 = reactType4;
    }

    public int getReactType5() {
        return reactType5;
    }

    public void setReactType5(int reactType5) {
        this.reactType5 = reactType5;
    }

    public int getReactType6() {
        return reactType6;
    }

    public void setReactType6(int reactType6) {
        this.reactType6 = reactType6;
    }
}
