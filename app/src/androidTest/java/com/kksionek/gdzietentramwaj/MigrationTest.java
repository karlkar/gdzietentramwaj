package com.kksionek.gdzietentramwaj;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.arch.persistence.room.testing.MigrationTestHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.kksionek.gdzietentramwaj.base.dataSource.MyDatabase;

import org.junit.After;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
}
