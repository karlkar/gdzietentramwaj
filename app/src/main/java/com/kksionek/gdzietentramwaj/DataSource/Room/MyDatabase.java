package com.kksionek.gdzietentramwaj.DataSource.Room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {FavoriteTram.class}, version = 1)
public abstract class MyDatabase extends RoomDatabase {
    public abstract TramDao tramDao();
}
