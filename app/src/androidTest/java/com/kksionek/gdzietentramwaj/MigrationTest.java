package com.kksionek.gdzietentramwaj;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.testing.MigrationTestHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.kksionek.gdzietentramwaj.DataSource.Room.FavoriteTram;
import com.kksionek.gdzietentramwaj.DataSource.Room.MyDatabase;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.kksionek.gdzietentramwaj.DataSource.Room.MyDatabase.MIGRATION_1_2;
import static com.kksionek.gdzietentramwaj.DataSource.Room.MyDatabase.MIGRATION_2_3;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MigrationTest {
    private static final String TEST_DB = "migration-test";

    @Rule
    public MigrationTestHelper mMigrationTestHelper;

    public MigrationTest() {
        mMigrationTestHelper = new MigrationTestHelper(
                InstrumentationRegistry.getInstrumentation(),
                MyDatabase.class.getCanonicalName(),
                new FrameworkSQLiteOpenHelperFactory());
    }

    private SqliteTestDbOpenHelper mSqliteTestDbHelper;

    @After
    public void tearDown() throws Exception {
        // Clear the database after every test
        mSqliteTestDbHelper = null;
    }

    @Test
    public void migrate1To3() throws Exception {
        mSqliteTestDbHelper = new SqliteTestDbOpenHelper(InstrumentationRegistry.getTargetContext(),
                                                         TEST_DB);
        SupportSQLiteDatabase db = mMigrationTestHelper.createDatabase(TEST_DB, 1);

        // db has schema version 1. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.
        SqliteDatabaseTestHelper.insertLine("211", true, mSqliteTestDbHelper);
        SqliteDatabaseTestHelper.insertLine("2", false, mSqliteTestDbHelper);

        // Prepare for the next version.
        db.close();

        mMigrationTestHelper.runMigrationsAndValidate(
                TEST_DB,
                3,
                true,
                MIGRATION_1_2,
                MIGRATION_2_3);

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
        MyDatabase migratedDb = getMigratedRoomDatabase();
        List<FavoriteTram> favoriteTrams = getValue(migratedDb.tramDao().getAllFavTrams());
        assertEquals(2, favoriteTrams.size());
        assertTrue(listHasTramOne(favoriteTrams));
        assertTrue(listHasTramTwo(favoriteTrams));
    }

    @Test
    public void migrate2To3() throws Exception {
        mSqliteTestDbHelper = new SqliteTestDbOpenHelper(InstrumentationRegistry.getTargetContext(),
                                                         TEST_DB);
        SupportSQLiteDatabase db = mMigrationTestHelper.createDatabase(TEST_DB, 2);

        // db has schema version 1. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.
        SqliteDatabaseTestHelper.insertLine("211", true, mSqliteTestDbHelper);
        SqliteDatabaseTestHelper.insertLine("2", false, mSqliteTestDbHelper);

        // Prepare for the next version.
        db.close();

        mMigrationTestHelper.runMigrationsAndValidate(
                TEST_DB,
                3,
                true,
                MIGRATION_2_3);

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
        MyDatabase migratedDb = getMigratedRoomDatabase();
        List<FavoriteTram> favoriteTrams = getValue(migratedDb.tramDao().getAllFavTrams());
        assertEquals(2, favoriteTrams.size());
        assertTrue(listHasTramOne(favoriteTrams));
        assertTrue(listHasTramTwo(favoriteTrams));
    }

    private boolean listHasTramOne(List<FavoriteTram> favoriteTrams) {
        for (FavoriteTram favoriteTram : favoriteTrams) {
            if (favoriteTram.isFavorite() && favoriteTram.getLineId().equals("211"))
                return true;
        }
        return false;
    }

    private boolean listHasTramTwo(List<FavoriteTram> favoriteTrams) {
        for (FavoriteTram favoriteTram : favoriteTrams) {
            if (!favoriteTram.isFavorite() && favoriteTram.getLineId().equals("2"))
                return true;
        }
        return false;
    }

    private MyDatabase getMigratedRoomDatabase() {
        MyDatabase database = Room.databaseBuilder(InstrumentationRegistry.getTargetContext(),
                                                      MyDatabase.class, TEST_DB)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build();
        // close the database and release any stream resources when the test finishes
        mMigrationTestHelper.closeWhenFinished(database);
        return database;
    }

    public static <T> T getValue(final LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(@Nullable T o) {
                data[0] = o;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        liveData.observeForever(observer);
        latch.await(2, TimeUnit.SECONDS);
        //noinspection unchecked
        return (T) data[0];
    }

    private class SqliteTestDbOpenHelper extends SQLiteOpenHelper {

        public SqliteTestDbOpenHelper(Context context, String databaseName) {
            super(context, databaseName, null, 3);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }

    private static class SqliteDatabaseTestHelper {
        public static void insertLine(String lineid, Boolean favorite, SqliteTestDbOpenHelper helper) {
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("mLineId", lineid);
            values.put("mFavorite", favorite);

            db.insertWithOnConflict("FavoriteTram", null, values,
                                    SQLiteDatabase.CONFLICT_REPLACE);

            db.close();
        }
    }
}
