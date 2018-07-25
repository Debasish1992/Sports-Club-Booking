package com.conlistech.sportsclubbookingengine.models;

public class UserMessage {

    String message;
    String createdAt;
    String nickNmae;
    String profileImgURL;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getNickNmae() {
        return nickNmae;
    }

    public void setNickNmae(String nickNmae) {
        this.nickNmae = nickNmae;
    }

    public String getProfileImgURL() {
        return profileImgURL;
    }

    public void setProfileImgURL(String profileImgURL) {
        this.profileImgURL = profileImgURL;
    }
}
