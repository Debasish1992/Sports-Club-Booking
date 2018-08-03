package com.conlistech.sportsclubbookingengine.models;

public class NotificationModel {

    private String notificationID, senderUserID, senderFullName, receiverUserID, notifyMessage,
            notifyTime, notifyType, imgProfile;

    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    public String getSenderUserID() {
        return senderUserID;
    }

    public void setSenderUserID(String senderUserID) {
        this.senderUserID = senderUserID;
    }

    public String getSenderFullName() {
        return senderFullName;
    }

    public void setSenderFullName(String senderFullName) {
        this.senderFullName = senderFullName;
    }

    public String getReceiverUserID() {
        return receiverUserID;
    }

    public void setReceiverUserID(String receiverUserID) {
        this.receiverUserID = receiverUserID;
    }

    public String getNotifyMessage() {
        return notifyMessage;
    }

    public void setNotifyMessage(String notifyMessage) {
        this.notifyMessage = notifyMessage;
    }

    public String getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(String notifyTime) {
        this.notifyTime = notifyTime;
    }

    public String getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(String notifyType) {
        this.notifyType = notifyType;
    }

    public String getImgProfile() {
        return imgProfile;
    }

    public void setImgProfile(String imgProfile) {
        this.imgProfile = imgProfile;
    }
}
