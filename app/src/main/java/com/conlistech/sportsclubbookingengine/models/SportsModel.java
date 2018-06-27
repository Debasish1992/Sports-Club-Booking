package com.conlistech.sportsclubbookingengine.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;

public class SportsModel {

    @ColumnInfo(name = "sport_name")
    String sportName;
    @ColumnInfo(name = "sport_id")
    String sportId;

    public SportsModel(String sportName, String sportId) {
        this.sportName = sportName;
        this.sportId = sportId;
    }

    @Ignore
    public SportsModel(){

    }

    public String getSportName() {
        return sportName;
    }

    public void setSportName(String sportName) {
        this.sportName = sportName;
    }

    public String getSportId() {
        return sportId;
    }

    public void setSportId(String sportId) {
        this.sportId = sportId;
    }
}
