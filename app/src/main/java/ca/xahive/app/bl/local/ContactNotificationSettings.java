package ca.xahive.app.bl.local;

/**
 * Created by Iversoft on 2014-08-26.
 */
public class ContactNotificationSettings {
    private int userId;
    private boolean soundEnabled;
    private boolean usingDefaultSound;
    private String soundPath;
    private boolean vibrationEnabled;
    private int vibrationLength;
    private int vibrationRepeatCount;

    public boolean isVibrationEnabled() {
        return vibrationEnabled;
    }

    public void setVibrationEnabled(boolean vibrationEnabled) {
        this.vibrationEnabled = vibrationEnabled;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public boolean isUsingDefaultSound() {
        return usingDefaultSound;
    }

    public void setUsingDefaultSound(boolean usingDefaultSound) {
        this.usingDefaultSound = usingDefaultSound;
    }

    public String getSoundPath() {
        return soundPath;
    }

    public void setSoundPath(String soundPath) {
        this.soundPath = soundPath;
    }

    public int getVibrationLength() {
        return vibrationLength;
    }

    public void setVibrationLength(int vibrationLength) {
        this.vibrationLength = vibrationLength;
    }

    public int getVibrationRepeatCount() {
        return vibrationRepeatCount;
    }

    public void setVibrationRepeatCount(int vibrationRepeatCount) {
        this.vibrationRepeatCount = vibrationRepeatCount;
    }

    public ContactNotificationSettings(int userId, boolean soundEnabled, boolean usingDefaultSound, String soundPath, boolean vibrationEnabled, int vibrationLength, int vibrationRepeatCount){
        setUserId(userId);
        setSoundEnabled(soundEnabled);
        setUsingDefaultSound(usingDefaultSound);
        setSoundPath(soundPath);
        setVibrationEnabled(vibrationEnabled);
        setVibrationLength(vibrationLength);
        setVibrationRepeatCount(vibrationRepeatCount);
    }
}
