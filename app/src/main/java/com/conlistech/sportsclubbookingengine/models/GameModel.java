package com.conlistech.sportsclubbookingengine.models;

import com.google.firebase.database.PropertyName;

import java.util.ArrayList;

public class GameModel {

    String gameId, gameName, gameTotalAmount,
            timeSlot, gameNote, venueId, gameTotalNoOfplayers,
            gameDate, gameSport, gameCreatorUserId,
            gameCreatorUserName;
    ArrayList<GamePlayersModel> gamePlayers;
    @PropertyName("gameInvitations")
    ArrayList<GamePlayersModel> gameInvitations;
    VenueInfoModel venueInfoModel;

    public VenueInfoModel getVenueInfoModel() {
        return venueInfoModel;
    }

    public void setVenueInfoModel(VenueInfoModel venueInfoModel) {
        this.venueInfoModel = venueInfoModel;
    }

    public ArrayList<GamePlayersModel> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(ArrayList<GamePlayersModel> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public ArrayList<GamePlayersModel> getPendingGameInvitations() {
        return gameInvitations;
    }

    public void setPendingGameInvitations(ArrayList<GamePlayersModel> pendingGameInvitations) {
        gameInvitations = pendingGameInvitations;
    }

    public String getGameCreatorUserId() {
        return gameCreatorUserId;
    }

    public void setGameCreatorUserId(String gameCreatorUserId) {
        this.gameCreatorUserId = gameCreatorUserId;
    }

    public String getGameCreatorUserName() {
        return gameCreatorUserName;
    }

    public void setGameCreatorUserName(String gameCreatorUserName) {
        this.gameCreatorUserName = gameCreatorUserName;
    }

    public String getGameSport() {
        return gameSport;
    }

    public void setGameSport(String gameSport) {
        this.gameSport = gameSport;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameTotalAmount() {
        return gameTotalAmount;
    }

    public void setGameTotalAmount(String gameTotalAmount) {
        this.gameTotalAmount = gameTotalAmount;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getGameNote() {
        return gameNote;
    }

    public void setGameNote(String gameNote) {
        this.gameNote = gameNote;
    }

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    public String getGameTotalNoOfplayers() {
        return gameTotalNoOfplayers;
    }

    public void setGameTotalNoOfplayers(String gameTotalNoOfplayers) {
        this.gameTotalNoOfplayers = gameTotalNoOfplayers;
    }

    public String getGameDate() {
        return gameDate;
    }

    public void setGameDate(String gameDate) {
        this.gameDate = gameDate;
    }
}
