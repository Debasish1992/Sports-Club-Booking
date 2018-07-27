package com.conlistech.sportsclubbookingengine.models;

import java.io.Serializable;

public class UserConversation implements Serializable{

    String userId, userFullName, channelID, userImage;
    Boolean isOnline;
    String receiverLastMsg;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public Boolean getOnline() {
        return isOnline;
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }

    public String getReceiverLastMsg() {
        return receiverLastMsg;
    }

    public void setReceiverLastMsg(String receiverLastMsg) {
        this.receiverLastMsg = receiverLastMsg;
    }
}
