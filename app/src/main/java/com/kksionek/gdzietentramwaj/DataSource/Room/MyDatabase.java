package com.kksionek.gdzietentramwaj.DataSource.Room;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

@Database(entities = {FavoriteTram.class}, version = 2, exportSchema = false)
public abstract class MyDatabase extends RoomDatabase {
    public static Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase supportSQLiteDatabase) {
            supportSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `tmp` (`mFavorite` INTEGER NOT NULL, `mLineId` TEXT, PRIMARY KEY(`mLineId`))");
            supportSQLiteDatabase.execSQL("INSERT INTO `tmp` (`mLineId`, `mFavorite`) SELECT `mLineId`, `mFavorite` FROM `FavoriteTram`");
            supportSQLiteDatabase.execSQL("DROP TABLE `FavoriteTram`");
            supportSQLiteDatabase.execSQL("ALTER TABLE `tmp` RENAME TO `FavoriteTram`");
        }
    };

    public abstract TramDao tramDao();
}
