package com.conlistech.sportsclubbookingengine.models;

public class UserModel {

    String userId, userEmail, userPhoneNumber, userFullName, favSport, notificationToken;
    boolean profile_visibility, contact_visibility;

    public String getNotificationToken() {
        return notificationToken;
    }

    public void setNotificationToken(String notificationToken) {
        this.notificationToken = notificationToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getFavSport() {
        return favSport;
    }

    public void setFavSport(String favSport) {
        this.favSport = favSport;
    }

    public boolean isProfile_visibility() {
        return profile_visibility;
    }

    public void setProfile_visibility(boolean profile_visibility) {
        this.profile_visibility = profile_visibility;
    }

    public boolean isContact_visibility() {
        return contact_visibility;
    }

    public void setContact_visibility(boolean contact_visibility) {
        this.contact_visibility = contact_visibility;
    }
}
