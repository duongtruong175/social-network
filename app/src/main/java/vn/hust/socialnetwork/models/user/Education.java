package vn.hust.socialnetwork.models.user;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Education implements Parcelable {
    @SerializedName("school")
    @Expose
    private String school;
    @SerializedName("majors")
    @Expose
    private String majors;

    public Education(String school, String majors) {
        this.school = school;
        this.majors = majors;
    }

    protected Education(Parcel in) {
        school = in.readString();
        majors = in.readString();
    }

    public static final Creator<Education> CREATOR = new Creator<Education>() {
        @Override
        public Education createFromParcel(Parcel in) {
            return new Education(in);
        }

        @Override
        public Education[] newArray(int size) {
            return new Education[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(school);
        parcel.writeString(majors);
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getMajors() {
        return majors;
    }

    public void setMajors(String majors) {
        this.majors = majors;
    }
}
