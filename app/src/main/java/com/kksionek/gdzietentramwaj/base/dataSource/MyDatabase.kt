package com.kksionek.gdzietentramwaj.base.dataSource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FavoriteTram::class], version = 5)
abstract class MyDatabase : RoomDatabase() {

    abstract fun tramDao(): TramDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(supportSQLiteDatabase: SupportSQLiteDatabase) {
                supportSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `tmp` (`mFavorite` INTEGER NOT NULL, `mLineId` TEXT, PRIMARY KEY(`mLineId`))")
                supportSQLiteDatabase.execSQL("INSERT INTO `tmp` (`mLineId`, `mFavorite`) SELECT `mLineId`, `mFavorite` FROM `FavoriteTram`")
                supportSQLiteDatabase.execSQL("DROP TABLE `FavoriteTram`")
                supportSQLiteDatabase.execSQL("ALTER TABLE `tmp` RENAME TO `FavoriteTram`")
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(supportSQLiteDatabase: SupportSQLiteDatabase) {
                supportSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `tmp` (`mFavorite` INTEGER NOT NULL, `mLineId` TEXT NOT NULL, PRIMARY KEY(`mLineId`))")
                supportSQLiteDatabase.execSQL("INSERT INTO `tmp` (`mLineId`, `mFavorite`) SELECT `mLineId`, `mFavorite` FROM `FavoriteTram`")
                supportSQLiteDatabase.execSQL("DROP TABLE `FavoriteTram`")
                supportSQLiteDatabase.execSQL("ALTER TABLE `tmp` RENAME TO `FavoriteTram`")
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(supportSQLiteDatabase: SupportSQLiteDatabase) {
                supportSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `tmp` (`isFavorite` INTEGER NOT NULL, `lineId` TEXT NOT NULL, PRIMARY KEY(`lineId`))")
                supportSQLiteDatabase.execSQL("INSERT INTO `tmp` (`lineId`, `isFavorite`) SELECT `mLineId`, `mFavorite` FROM `FavoriteTram`")
                supportSQLiteDatabase.execSQL("DROP TABLE `FavoriteTram`")
                supportSQLiteDatabase.execSQL("ALTER TABLE `tmp` RENAME TO `FavoriteTram`")
            }
        }

        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(supportSQLiteDatabase: SupportSQLiteDatabase) {
                supportSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `tmp` (`isFavorite` INTEGER NOT NULL, `lineId` TEXT NOT NULL, `cityId` INTEGER NOT NULL, PRIMARY KEY(`lineId`))")
                supportSQLiteDatabase.execSQL("INSERT INTO `tmp` (`lineId`, `isFavorite`, `cityId`) SELECT `lineId`, `isFavorite`, 1 FROM `FavoriteTram`")
                supportSQLiteDatabase.execSQL("DROP TABLE `FavoriteTram`")
                supportSQLiteDatabase.execSQL("ALTER TABLE `tmp` RENAME TO `FavoriteTram`")
            }
        }
    }
}
