package ca.xahive.app.bl.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.Random;

import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.ui.activities.TabBarManagerActivity;


public class LocalStorage implements DecryptedMessageCacheInterface {
    private static LocalStorage _instance;
    private DecryptedMessagesSQLiteHelper decryptedMessagesSQLiteHelper;
    private SQLiteDatabase decryptedMessagesDB;
    private AvatarSQLiteHelper avatarSQLiteHelper;
    private SQLiteDatabase avatarDB;
    private ContactNotificationSettingsSQLiteHelper contactNotificationSettingsSQLiteHelper;
    private SQLiteDatabase contactNotificationSettingsDB;

    public static LocalStorage getInstance() {
        if (_instance == null) {
            _instance = new LocalStorage();
        }
        return _instance;
    }

    /* Local to prevent outside init. */
    private LocalStorage() {
        decryptedMessagesSQLiteHelper = new DecryptedMessagesSQLiteHelper(TabBarManagerActivity.getContext());
        decryptedMessagesDB = decryptedMessagesSQLiteHelper.getWritableDatabase();

        avatarSQLiteHelper = new AvatarSQLiteHelper(TabBarManagerActivity.getContext());
        avatarDB = avatarSQLiteHelper.getWritableDatabase();

        contactNotificationSettingsSQLiteHelper = new ContactNotificationSettingsSQLiteHelper(TabBarManagerActivity.getContext());
        contactNotificationSettingsDB = contactNotificationSettingsSQLiteHelper.getWritableDatabase();
    }

    @Override
    public boolean hasDecryptedTextForMessageId(int messageId) {
        return Helpers.stringIsNotNullAndMeetsMinLength(decryptedTextForMessageId(messageId), 1);
    }

    @Override
    public String decryptedTextForMessageId(int messageId) {
        Cursor results = decryptedMessagesDB.query(
                false,
                DecryptedMessagesSQLiteHelper.TABLE_NAME,
                new String[] { "content" },
                String.format("_id = %d", messageId),
                null,
                null,
                null,
                null,
                null
        );

        String decryptedMessageText = null;

        if ( results != null && results.moveToFirst() ) {
            decryptedMessageText = results.getString(0);
        }

        return decryptedMessageText;
    }

    @Override
    public void setDecryptedTextForMessageId(String text, int messageId) {
        if (hasDecryptedTextForMessageId(messageId)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put("_id", messageId);
        values.put("content", text);

        decryptedMessagesDB.insert(DecryptedMessagesSQLiteHelper.TABLE_NAME, null, values);
    }

    public void saveAvatarFilename(String filename, int userId) { // Write the file to the disk FIRST!
        deleteAvatarFilename(userId);

        ContentValues values = new ContentValues();
        values.put("_id", userId);
        values.put("localFilename", filename);

        avatarDB.insert(AvatarSQLiteHelper.TABLE_NAME, null, values);
    }

    public void deleteAvatarFilename(int userId) { // Call this before removing the file.
        avatarDB.delete(AvatarSQLiteHelper.TABLE_NAME, String.format("_id = %d", userId), null);
    }

    public String getAvatarFilenameForUserId(int userId) {
        Cursor results = avatarDB.query(
                false,
                AvatarSQLiteHelper.TABLE_NAME,
                new String[] { "localFilename" },
                String.format("_id = %d", userId),
                null,
                null,
                null,
                null,
                null
        );

        String filename = null;

        if ( results != null && results.moveToFirst() ) {
            filename = results.getString(0);
        }

        return filename;
    }

    public static File generateCacheFileWithString(String filename) {
        return new File(TabBarManagerActivity.getContext().getCacheDir(), filename);
    }

    public static File generateCacheFile() {
        File cacheFile;
        do {
            cacheFile = new File(TabBarManagerActivity.getContext().getCacheDir(), generateCacheFilename());
        } while (cacheFile.exists());

        return cacheFile;
    }

    public static String generateCacheFilename() {
        final String possibleChars = "0123456789abcdefghijklmnopqrstuvwxyz";
        final int sizeOfRandomString = 24;
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder();

        for (int i = 0 ; i < sizeOfRandomString ; ++i) {
            sb.append( possibleChars.charAt( random.nextInt( possibleChars.length() ) ) );
        }

        return sb.toString();
    }

    public void saveContactNotificationSettings(ContactNotificationSettings contactNotificationSettings){
        deleteContactNotificationSettingsForUserId(contactNotificationSettings.getUserId());

        ContentValues values = new ContentValues();
        values.put("userID", contactNotificationSettings.getUserId());
        values.put("soundEnabled", contactNotificationSettings.isSoundEnabled());
        values.put("usingDefaultSound", contactNotificationSettings.isUsingDefaultSound());
        values.put("soundPath", contactNotificationSettings.getSoundPath());
        values.put("vibrationEnabled", contactNotificationSettings.isVibrationEnabled());
        values.put("vibrationLength", contactNotificationSettings.getVibrationLength());
        values.put("vibrationRepeatCount", contactNotificationSettings.getVibrationRepeatCount());

        contactNotificationSettingsDB.insert(ContactNotificationSettingsSQLiteHelper.TABLE_NAME, null, values);
    }

    public void deleteContactNotificationSettingsForUserId(int userId){
        contactNotificationSettingsDB.delete(ContactNotificationSettingsSQLiteHelper.TABLE_NAME, String.format("userID = %d", userId), null);
    }

    public ContactNotificationSettings getContactNotificationSettingsForUserId(int userId) {
        Cursor results = contactNotificationSettingsDB.query(false,
                ContactNotificationSettingsSQLiteHelper.TABLE_NAME,
                new String[]{"userID, soundEnabled, usingDefaultSound, soundPath, vibrationEnabled, vibrationLength, vibrationRepeatCount"},
                String.format("userID = %d", userId),
                null,
                null,
                null,
                null,
                null);

        if (results != null && results.moveToFirst()) {
            ContactNotificationSettings contactNotificationSettings = new ContactNotificationSettings(
                    results.getInt(0),
                    results.getInt(1) == 1,
                    results.getInt(2) == 1,
                    results.getString(3),
                    results.getInt(4) == 1,
                    results.getInt(5),
                    results.getInt(6));
            return contactNotificationSettings;
        }

        return null;
    }

    public void clear() {
        // TODO implement
    }
}
