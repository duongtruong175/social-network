package vn.hust.socialnetwork.models.user;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User implements Parcelable {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("cover_image")
    @Expose
    private String coverImage;
    @SerializedName("short_description")
    @Expose
    private String shortDescription;
    @SerializedName("birthday")
    @Expose
    private String birthday;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("relationship_status")
    @Expose
    private String relationshipStatus;
    @SerializedName("hometown")
    @Expose
    private String hometown;
    @SerializedName("current_residence")
    @Expose
    private String currentResidence;
    @SerializedName("job")
    @Expose
    private Job job;
    @SerializedName("education")
    @Expose
    private Education education;
    @SerializedName("website")
    @Expose
    private String website;
    @SerializedName("counts")
    @Expose
    private CountsUser counts;
    @SerializedName("relation")
    @Expose
    private String relation;
    @SerializedName("role_id")
    @Expose
    private int roleId;
    @SerializedName("user_status_code")
    @Expose
    private int userStatusCode;
    @SerializedName("email_verified_at")
    @Expose
    private String emailVerifiedAt;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public User(int id, String email, String phone, String name, String avatar, String coverImage, String shortDescription, String birthday, String gender, String relationshipStatus, String hometown, String currentResidence, Job job, Education education, String website, CountsUser counts, String relation, int roleId, int userStatusCode, String emailVerifiedAt, String createdAt, String updatedAt) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.avatar = avatar;
        this.coverImage = coverImage;
        this.shortDescription = shortDescription;
        this.birthday = birthday;
        this.gender = gender;
        this.relationshipStatus = relationshipStatus;
        this.hometown = hometown;
        this.currentResidence = currentResidence;
        this.job = job;
        this.education = education;
        this.website = website;
        this.counts = counts;
        this.relation = relation;
        this.roleId = roleId;
        this.userStatusCode = userStatusCode;
        this.emailVerifiedAt = emailVerifiedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    protected User(Parcel in) {
        id = in.readInt();
        email = in.readString();
        phone = in.readString();
        name = in.readString();
        avatar = in.readString();
        coverImage = in.readString();
        shortDescription = in.readString();
        birthday = in.readString();
        gender = in.readString();
        relationshipStatus = in.readString();
        hometown = in.readString();
        currentResidence = in.readString();
        job = in.readParcelable(Job.class.getClassLoader());
        education = in.readParcelable(Education.class.getClassLoader());
        website = in.readString();
        counts = in.readParcelable(CountsUser.class.getClassLoader());
        relation = in.readString();
        roleId = in.readInt();
        userStatusCode = in.readInt();
        emailVerifiedAt = in.readString();
        createdAt = in.readString();
        updatedAt = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(email);
        dest.writeString(phone);
        dest.writeString(name);
        dest.writeString(avatar);
        dest.writeString(coverImage);
        dest.writeString(shortDescription);
        dest.writeString(birthday);
        dest.writeString(gender);
        dest.writeString(relationshipStatus);
        dest.writeString(hometown);
        dest.writeString(currentResidence);
        dest.writeParcelable(job, flags);
        dest.writeParcelable(education, flags);
        dest.writeString(website);
        dest.writeParcelable(counts, flags);
        dest.writeString(relation);
        dest.writeInt(roleId);
        dest.writeInt(userStatusCode);
        dest.writeString(emailVerifiedAt);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRelationshipStatus() {
        return relationshipStatus;
    }

    public void setRelationshipStatus(String relationshipStatus) {
        this.relationshipStatus = relationshipStatus;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public String getCurrentResidence() {
        return currentResidence;
    }

    public void setCurrentResidence(String currentResidence) {
        this.currentResidence = currentResidence;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Education getEducation() {
        return education;
    }

    public void setEducation(Education education) {
        this.education = education;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public CountsUser getCounts() {
        return counts;
    }

    public void setCounts(CountsUser counts) {
        this.counts = counts;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getUserStatusCode() {
        return userStatusCode;
    }

    public void setUserStatusCode(int userStatusCode) {
        this.userStatusCode = userStatusCode;
    }

    public String getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(String emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
