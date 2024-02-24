package ca.xahive.app.bl.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Iversoft on 2014-08-26.
 */
public class ContactNotificationSettingsSQLiteHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "userNotificationSettings";
    public static final String TABLE_NAME = DATABASE_NAME;
    public static final int DATABASE_VERSION = 1;

    public ContactNotificationSettingsSQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sqlString = "CREATE TABLE " + TABLE_NAME
                + "("
                + "userID"
                + " INT PRIMARY KEY,"
                + "soundEnabled"
                + " INT NOT NULL,"
                + "usingDefaultSound"
                + " INT NOT NULL,"
                + "soundPath"
                + " VARCHAR(1024) NOT NULL,"
                + "vibrationEnabled"
                + " INT NOT NULL,"
                + "vibrationLength"
                + " INT NOT NULL,"
                + "vibrationRepeatCount"
                + " INT NOT NULL"
                + ")";

        sqLiteDatabase.execSQL(sqlString);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
