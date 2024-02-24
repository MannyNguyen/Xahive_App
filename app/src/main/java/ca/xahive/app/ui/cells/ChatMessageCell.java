package ca.xahive.app.ui.cells;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazonaws.util.Base64;

import org.spongycastle.crypto.AsymmetricBlockCipher;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.encodings.PKCS1Encoding;
import org.spongycastle.crypto.engines.RSAEngine;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.util.PrivateKeyFactory;
import org.spongycastle.crypto.util.PublicKeyFactory;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemReader;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Observable;
import java.util.Observer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ca.xahive.app.bl.utils.SimpleCrypto;
import ca.xahive.app.ui.activities.ChatView;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.local.Avatar;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.local.UserDefaults;
import ca.xahive.app.bl.local.XYDimension;
import ca.xahive.app.bl.objects.Message;
import ca.xahive.app.bl.objects.PersonalMessage;
import ca.xahive.app.bl.utils.Crypto;
import ca.xahive.app.bl.utils.CryptoHandler;
import ca.xahive.app.bl.utils.FontHelper;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.ui.views.HexagonImageView;

public class ChatMessageCell extends LinearLayout implements Observer {
    private Avatar avatar;
    private boolean shouldUpdateAvatar = true;

    private static float DEFAULT_LOCK_ICON_WIDTH = 20.0f;
    private static float DEFAULT_ATTACH_ICON_WIDTH = 14.0f;
    private static float ICON_HEIGHT = 20.0f;
    private float displayDensity = getContext().getResources().getDisplayMetrics().density * 0.75f;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            String decryptMessageTemp = (String)  msg.obj;

            if (decryptMessageTemp != null) {

                getMessageTextView().setText(decryptMessageTemp);
            } else {
                getMessageTextView().setText(getPersonalMessage().getReadableContent());
            }
            //getMessageTextView().setText( getPersonalMessage().getReadableContent() );
            // getMessageTextView().setText( decryptMessage(getPersonalMessage()) );


            switch (getPersonalMessage().getEncryptionState()) {
                case XAHMessageStateNotEncrypted:
                    hideLockIcon();
                    updateAttachIcon();
                    updateBackgroundForNotEncrypted();
                    break;
                case XAHMessageStateEncrypted:
                    showLockIcon();

                    showLockIconForEncrypted();
                    if (getPersonalMessage().attachmentId == 0) {
                        hideAttachIcon();

                    }
                    //hideLockIcon();

                    updateBackgroundForEncrypted();
                    break;
                case XAHMessageStateDecrypted:
                    showLockIconForDecrypted();
                    updateAttachIcon();
                    updateBackgroundForNotEncrypted();
                    break;
            }
            if (getPersonalMessage().attachmentId > 0) {
                updateAttachIcon();

                getAttachButton().setVisibility(VISIBLE);
            } else {
                getAttachButton().setVisibility(GONE);
            }
        }
    };

    private PersonalMessage personalMessage;
    public MessageCellAttachEncryptionListener attachEncryptionListener;

    public void setPersonalMessage(PersonalMessage personalMessage) {
        this.personalMessage = personalMessage;

        if (this.personalMessage != null) {
            setAvatar(Avatar.avatarWithId(this.personalMessage.getFromUserId()));
        } else {
            setAvatar(null);
        }

        messageChanged();
    }

    public PersonalMessage getPersonalMessage() {
        return personalMessage;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        // Avoid updating when the avatar is the same as previous.
        if (this.avatar == avatar) {
            return;
        }

        if (this.avatar != null) {
            this.avatar.deleteObserver(this);
        }

        this.avatar = avatar;

        if (this.avatar != null) {
            this.avatar.addObserver(this, true);
        }
    }

    private HexagonImageView getMessageAvatarView() {
        return (HexagonImageView) findViewById(R.id.avatarImageView);
    }

    private TextView getTimestampTextView() {
        return (TextView) findViewById(R.id.timestamp);
    }

    private TextView getMessageTextView() {
        return (TextView) findViewById(R.id.messageText);
    }

    private ImageButton getLockButton() {
        return (ImageButton) findViewById(R.id.msgLockIcon);
    }

    private ImageButton getAttachButton() {
        return (ImageButton) findViewById(R.id.msgAttachIcon);
    }

    private LinearLayout getMessageLayout() {
        return (LinearLayout) findViewById(R.id.messageLayout);
    }

    public ChatMessageCell(Context context) {
        super(context);

        try {
            attachEncryptionListener = (MessageCellAttachEncryptionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement MessageCellAttachEncryptionListener");
        }
    }

    public ChatMessageCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void messageChanged() {
        if (getPersonalMessage() == null) {
            return;
        }
        getTimestampTextView().setText(Helpers.humanReadableDate(getPersonalMessage().getDate()));
        new Thread(run).start();
        setupClickListeners();

    }

    private void setupClickListeners() {

        getLockButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                lockPressed();
            }
        });

        getAttachButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                attachPressed();
            }
        });

    }

    private void lockPressed() {
        if (getPersonalMessage().getEncryptionState() == Message.EncryptionState.XAHMessageStateEncrypted)
            attachEncryptionListener.lockButtonPressed(getPersonalMessage());
    }

    private void attachPressed() {

        Log.v("attachPressed", String.valueOf(getPersonalMessage().getAttachmentId()));
        if (getPersonalMessage().getAttachmentId() > 0)
            attachEncryptionListener.attachButtonPressed(getPersonalMessage());
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {

            String decryptMessageTemp = null;
            try {
                decryptMessageTemp = decryptMessage(getPersonalMessage());
            } catch (Exception ex) {

            }

            android.os.Message msg = handler.obtainMessage(1, decryptMessageTemp);
            handler.sendMessage(msg);
        }
    };

    private void configureView() {



    }

    private String decryptMessage(PersonalMessage message) {
        String readableContent = "";
        if (message.fromUserId == Model.getInstance().getCurrentUser().getUserId()) {
            String refKeyTemp = null;
            try {
                refKeyTemp = "";
                refKeyTemp = Crypto.RSADecrypt(message.refKey, UserDefaults.getPrivateKeyWithHiveAsString(Model.getInstance().getCurrentUser().getUserId(), "1"));
            } catch (Exception e) {
                // Log.v("printStackTrace", e.getMessage());
            }

            if (refKeyTemp != null && message.content != null) {
                readableContent = CryptoHandler.decryptionMessageWithPassword(message.content, refKeyTemp);
            } else {
                readableContent = Model.getInstance().getDecryptedMessageStore().decryptedTextForMessageId(message.messageId);
            }

        } else {

            String contentKeyTemp = null;
            try {
                contentKeyTemp = "";
                /**if(ChatView.publicKeyAsString.equals("")) {
                 contentKeyTemp = decrypt("", message.contentKey);
                 }
                 else {**/
                contentKeyTemp = Crypto.RSADecrypt(message.contentKey, UserDefaults.getPrivateKeyWithHiveAsString(Model.getInstance().getCurrentUser().getUserId(), "1"));
                // }
            } catch (Exception e) {
            }

            if (contentKeyTemp != null && message.content != null) {
                readableContent = CryptoHandler.decryptionMessageWithPassword(message.content, contentKeyTemp);
            } else {
                readableContent = Model.getInstance().getDecryptedMessageStore().decryptedTextForMessageId(message.messageId);
            }
        }
        return readableContent;
    }

    private void updateAvatar() {
        if (shouldUpdateAvatar && getAvatar() != null) {
            getMessageAvatarView().setBitmapToClip(getAvatar().getBitmapWithDimensions(new XYDimension(40, 40)));
        }
    }

    /**
     * private String decrypt (String privateKeyFilename, String encryptedData) throws IOException, InvalidCipherTextException {
     * <p/>
     * String outputData = null;
     * <p/>
     * Log.v("contentKeyTemp",privateKeyFilename)  ;
     * <p/>
     * final Reader reader = new StringReader(UserDefaults.getPrivateKeyWithHiveAsString("1"));
     * PemReader pemReader = new PemReader(reader);
     * PemObject pemObject= null;
     * try {
     * pemObject = pemReader.readPemObject();
     * } catch (IOException e) {
     * e.printStackTrace();
     * }
     * try {
     * pemReader.close();
     * } catch (IOException e) {
     * e.printStackTrace();
     * }
     * AsymmetricKeyParameter privateKey = null;
     * try {
     * privateKey  =  (AsymmetricKeyParameter) PrivateKeyFactory.createKey(UserDefaults.getPrivateKeyWithHiveAsString("1").getBytes("utf-8"));
     * } catch (IOException e) {
     * e.printStackTrace();
     * }
     * <p/>
     * Log.v("contentKeyTemp","1")  ;
     * <p/>
     * AsymmetricBlockCipher e = new RSAEngine();
     * e = new PKCS1Encoding(e);
     * e.init(false, privateKey);
     * <p/>
     * Log.v("contentKeyTemp", "2")  ;
     * byte[] messageBytes = hexStringToByteArray(encryptedData);
     * byte[] hexEncodedCipher = e.processBlock(messageBytes, 0, messageBytes.length);
     * <p/>
     * Log.v("contentKeyTemp","3")  ;
     * System.out.println(new String(hexEncodedCipher));
     * outputData = new String(hexEncodedCipher);
     * <p/>
     * <p/>
     * <p/>
     * return outputData;
     * }
     **/
    public static String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private void updateAttachIcon() {
        //initialize the image variable
        Drawable attachImg = null;
        //get the file name
        if (getPersonalMessage().getFilename() == null)
            return;

        Boolean isExits = Helpers.checkFileDownloadExits(getPersonalMessage().getFilename());
        String whatFileMime = Helpers.getDownloadedFileMime(getPersonalMessage().getFilename());
        Integer weAreImage = 0;

        //make sure the file exists AND that it is a drawable AND that is not encrypted
        if (isExits && getPersonalMessage().isEncrypted() && (whatFileMime.contains("image/") || whatFileMime.contains("video/"))) {
            File file = Helpers.getDownloadedFileNamed(getContext(), getPersonalMessage().getFilename());
            Log.i("file","" + file);
            if (whatFileMime.contains("image/")) {
                weAreImage = 1; //for the layout stuff after these ifs
                //there is a problem here, if the file is over a certain size it creates an error and sometimes crashes the app.
                //android has limited the heap size to 16 megs FFS
                //solution 1 -> we could add the following to the <application > tag in the manifest
                //android:largeHeap="true"
                //if we do that then we might be
                //          *causing the app to be more susceptible to closing
                //          *we aren't solving even larger file issues
                //          *we are not making the app faster
                //          *we only get 64 or 128 megs depending on device and that is just a band-aid fora later, even larger heap fill situation
                // solution 2 -> caching them into native memory: http://stackoverflow.com/questions/17900732/how-to-cache-bitmaps-into-native-memory
                //          *well still have the problem of large files
                //          *this will make things slower again
                // solution 3 -> reduce the resolution of each image as it comes into the screen -> this is the best solution IMHO
                //          *we should do this in the api and then download a thumbnail in the app
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap bMap = null;
                if (whatFileMime.contains("image/gif")) {
                    bMap = BitmapFactory.decodeFile(file.getAbsolutePath());
                } else {
                    bMap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                }
                attachImg = new BitmapDrawable(getResources(), bMap);
            } else if (whatFileMime.contains("video/")) {
                //now we get a thumbnail for a video!
                weAreImage = 1; //for the layout stuff after these ifs
                Bitmap bMap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
                attachImg = new BitmapDrawable(getResources(), bMap);
            }
            //} else if (!file.exists() && whatFileMime.contains("image/")) {
            //so the file doesn't exist but we know it is an image file
            //lets download it!  set an option so that it doesn't auto open the file once downloaded
            //TODO: make auto downloading optional for images / videos / everything in SETTINGS before fully implementing this
        } else {
            attachImg = getPersonalMessage().isIncoming() ? getResources().getDrawable(R.drawable.mess_clip_grey) : getResources().getDrawable(R.drawable.mess_clip_green);
        }

        //TODO: maybe add try/catch here.. a cmyk profile image will be broken possibly

        if (Helpers.isStoneAgeAPI()) {
            getAttachButton().setBackgroundDrawable(attachImg);
        } else {
            getAttachButton().setBackground(attachImg);
        }

        //once again check for valid file and mimetype
        if (weAreImage == 1) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getAttachButton().getLayoutParams();
            params.width = (getPersonalMessage().getAttachmentId() > 0) ? (int) (DEFAULT_ATTACH_ICON_WIDTH * displayDensity * 4) : 0;
            params.height = (int) (ICON_HEIGHT * displayDensity * 4);
            getAttachButton().setLayoutParams(params);
        } else {
           RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getAttachButton().getLayoutParams();
            params.width = (getPersonalMessage().getAttachmentId() > 0) ? (int) (DEFAULT_ATTACH_ICON_WIDTH * displayDensity * 4) : 0;
            params.height = (int) (ICON_HEIGHT * displayDensity * 4);
            getAttachButton().setLayoutParams(params);
        }
    }

    private void hideAttachIcon() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getAttachButton().getLayoutParams();
        params.width = 0;
        params.height = (int) (ICON_HEIGHT * displayDensity);
        getAttachButton().setLayoutParams(params);
    }

    private void hideLockIcon() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLockButton().getLayoutParams();
        params.width = 0;
        params.height = (int) (ICON_HEIGHT * displayDensity);
        getLockButton().setLayoutParams(params);
    }

    private void showLockIcon() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLockButton().getLayoutParams();
        params.width = (int) (DEFAULT_LOCK_ICON_WIDTH * displayDensity);
        params.height = (int) (ICON_HEIGHT * displayDensity);
        getLockButton().setLayoutParams(params);
    }

    private void showLockIconForEncrypted() {
        showLockIcon();

        Drawable lockImg = getPersonalMessage().isIncoming() ? getResources().getDrawable(R.drawable.mess_ico_lock_green) : getResources().getDrawable(R.drawable.mess_ico_lock_grey);

        if (Helpers.isStoneAgeAPI()) {
            getLockButton().setBackgroundDrawable(lockImg);
        } else {
            getLockButton().setBackground(lockImg);
        }
    }

    private void showLockIconForDecrypted() {
        showLockIcon();

        Drawable lockImg = getPersonalMessage().isIncoming() ? getResources().getDrawable(R.drawable.mess_ico_unlock_green) : getResources().getDrawable(R.drawable.mess_ico_unlock_grey);

        if (Helpers.isStoneAgeAPI()) {
            getLockButton().setBackgroundDrawable(lockImg);
        } else {
            getLockButton().setBackground(lockImg);
        }
    }

    private void updateBackgroundForNotEncrypted() {
        Drawable backgroudColor = getPersonalMessage().isIncoming() ? getResources().getDrawable(R.drawable.chat_message_cell_border_dark) : getResources().getDrawable(R.drawable.chat_message_cell_border_grey);
        if (!getPersonalMessage().isIncoming()) {
            getMessageAvatarView().setVisibility(INVISIBLE);
        } else {

            getMessageAvatarView().setVisibility(VISIBLE);
        }
        //int backgroudColor = getPersonalMessage().isIncoming() ? getResources().getColor(R.color.xa_light_green) : getResources().getColor(R.color.xa_very_light_grey);
        if (getPersonalMessage().isIncoming())
            getMessageLayout().setLayoutDirection(LAYOUT_DIRECTION_LTR);
        if (getPersonalMessage().isIncoming()) {
            /**
             getMessageLayout().setGravity(FOCUS_RIGHT);
             getMessageLayout().setLayoutDirection(LAYOUT_DIRECTION_RTL);
             getMessageLayout().setScaleY(-1f);
             getMessageLayout().setScaleX(-1);
             getMessageLayout().setRotationY(180);
             **/
        }
        ;

        getMessageLayout().setBackgroundDrawable(backgroudColor);

        //getMessageLayout().setBackgroundColor(backgroudColor);
    }

    private void updateBackgroundForEncrypted() {
        // int backgroudColor = getPersonalMessage().isIncoming() ? getResources().getColor(R.color.xa_dark_green) : getResources().getColor(R.color.xa_medium_grey);
        Drawable backgroudColor = getPersonalMessage().isIncoming() ? getResources().getDrawable(R.drawable.chat_message_cell_border_dark) : getResources().getDrawable(R.drawable.chat_message_cell_border_grey);
        if (!getPersonalMessage().isIncoming()) {
            getMessageAvatarView().setVisibility(INVISIBLE);
        } else {

            getMessageAvatarView().setVisibility(VISIBLE);
        }

        getMessageLayout().setBackgroundDrawable(backgroudColor);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        shouldUpdateAvatar = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        shouldUpdateAvatar = false;
    }

    @Override
    public void update(Observable observable, Object o) {
        updateAvatar();
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        FontHelper.getInstance(getContext()).setCustomFont(getTimestampTextView(), FontHelper.CustomFontEnum.XAHDetailLabelFont);
        FontHelper.getInstance(getContext()).setCustomFont(getMessageTextView(), FontHelper.CustomFontEnum.XAHTextViewFont);
    }
}
