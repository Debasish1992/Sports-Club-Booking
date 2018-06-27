package com.conlistech.sportsclubbookingengine.interfaces;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.conlistech.sportsclubbookingengine.models.SportsModel;


import java.util.List;

/*@Dao
public interface SportsDao {

    @Query("SELECT * FROM sports")
    List<SportsModel> getAll();

    @Query("SELECT sport_name FROM sports WHERE sportId IN (:sportId)")
    List<SportsModel> loadAllByIds(String sportId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SportsEntity sportsModels);

    @Delete
    void delete(SportsEntity sportsModels);

}*/
