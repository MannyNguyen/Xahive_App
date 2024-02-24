/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.xahive.app.bl.gcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import java.util.Vector;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.ContactNotificationSettings;
import ca.xahive.app.bl.local.LocalStorage;
import ca.xahive.app.ui.activities.TabBarManagerActivity;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NEW_MESSAGES_NOTIFICATION_ID = 1;
    public static final String TAB_EXTRA = "TAB_EXTRA";
    public static final String SHOW_BUZZES_TAB = "SHOW_BUZZES_TAB";
    public static final String SHOW_MESSAGES_TAB = "SHOW_MESSAGES_TAB";
    private static long SHORT_VIBRATION_LENGTH = 500;
    private static long MEDIUM_VIBRATION_LENGTH = 750;
    private static long LONG_VIBRATION_LENGTH = 1000;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            if(extras.getString("message") != null
                    && extras.getString("type") != null
                    && extras.getString("fromUserId") != null){
                createNotification(extras.getString("message"), Integer.parseInt(extras.getString("type")), Integer.parseInt(extras.getString("fromUserId")));
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void createNotification(String message, int type, int fromUserId) {
        Intent intent = new Intent(this, TabBarManagerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(TAB_EXTRA, type == NEW_MESSAGES_NOTIFICATION_ID ? SHOW_MESSAGES_TAB : SHOW_BUZZES_TAB);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri soundURI = Settings.System.DEFAULT_NOTIFICATION_URI;
        boolean shouldVibrate = true;
        long vibrationLength = MEDIUM_VIBRATION_LENGTH;
        int vibrationRepeat = 0;

        if(type == NEW_MESSAGES_NOTIFICATION_ID) {
            ContactNotificationSettings contactNotificationSettings = LocalStorage.getInstance().getContactNotificationSettingsForUserId(fromUserId);

            if (contactNotificationSettings != null) {
                if (contactNotificationSettings.isSoundEnabled()) {
                    if (contactNotificationSettings.isUsingDefaultSound()) {
                        soundURI = Settings.System.DEFAULT_NOTIFICATION_URI;
                    }else{
                        soundURI = Uri.parse(contactNotificationSettings.getSoundPath());
                    }
                }

                shouldVibrate = contactNotificationSettings.isVibrationEnabled();

                switch (contactNotificationSettings.getVibrationLength()) {
                    case 0: {
                        vibrationLength = SHORT_VIBRATION_LENGTH;
                        break;
                    }

                    case 1: {
                        vibrationLength = MEDIUM_VIBRATION_LENGTH;
                        break;
                    }

                    case 2: {
                        vibrationLength = LONG_VIBRATION_LENGTH;
                        break;
                    }

                    default: {
                        vibrationLength = MEDIUM_VIBRATION_LENGTH;
                        break;
                    }
                }

                vibrationRepeat = contactNotificationSettings.getVibrationRepeatCount();
            }
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("xahive")
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setSound(soundURI)
                .setPriority(Notification.PRIORITY_MAX) //set max notification priority so that notification is more likely to show in big text style
                        //big text style allows multiline messages and is ignored by pre-4.1 devices
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        if(shouldVibrate) {
            Vector<Long> vibrationPatternVector = new Vector<Long>();

            for (int i = 0; i < vibrationRepeat + 1; i++) {
                vibrationPatternVector.add(100L);
                vibrationPatternVector.add(vibrationLength);
            }

            long[] longArray = new long[vibrationPatternVector.size()];

            for (int i = 0; i < longArray.length; i++) {
                longArray[i] = vibrationPatternVector.get(i);
            }

            notificationBuilder.setVibrate(longArray);
        }

        Notification notification;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            notification = notificationBuilder.getNotification();
        } else {
            notification = notificationBuilder.build();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(type, notification);
    }
}
