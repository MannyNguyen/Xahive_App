package ca.xahive.app.bl.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DecryptedMessagesSQLiteHelper extends SQLiteOpenHelper {
    private static final String ON_CREATE_STRING =
            "CREATE TABLE decryptedMessages ( \n" +
            "    _id     INT              PRIMARY KEY\n" +
            "                             NOT NULL\n" +
            "                             UNIQUE,\n" +
            "    content VARCHAR( 2056 )  NOT NULL \n" +
            ");\n";

    public static final String DATABASE_NAME = "decryptedMessages";
    public static final String TABLE_NAME = DATABASE_NAME;
    public static final int DATABASE_VERSION = 1;

    public DecryptedMessagesSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(ON_CREATE_STRING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
