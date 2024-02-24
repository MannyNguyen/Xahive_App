package ca.xahive.app.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.spongycastle.crypto.AsymmetricBlockCipher;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.encodings.PKCS1Encoding;
import org.spongycastle.crypto.engines.RSAEngine;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.util.PublicKeyFactory;
import org.spongycastle.util.Encodable;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Encoder;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemReader;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import IAPUtils.Base64DecoderException;
import ca.xahive.app.bl.utils.RSAPublicKeyFromOpenSSL_PKCS1_PEM;
import ca.xahive.app.bl.utils.SimpleCrypto;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.api.queries.APIConnectionRequest;
import ca.xahive.app.bl.local.Attachment;
import ca.xahive.app.bl.local.ConversationModelItem;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.local.Parcelator;
import ca.xahive.app.bl.local.ParceledInteger;
import ca.xahive.app.bl.local.PasswordCacheContext;
import ca.xahive.app.bl.local.UserDefaults;
import ca.xahive.app.bl.local.UserRelationshipList;
import ca.xahive.app.bl.objects.Conversation;
import ca.xahive.app.bl.objects.Message;
import ca.xahive.app.bl.objects.ModelError;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.ModelObject;
import ca.xahive.app.bl.objects.ModelState;
import ca.xahive.app.bl.objects.PersonalMessage;
import ca.xahive.app.bl.objects.PersonalMessageList;
import ca.xahive.app.bl.objects.User;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.bl.objects.api_object.MessagePostRequest;
import ca.xahive.app.bl.objects.api_object.PublicKeyOfUserResponse;
import ca.xahive.app.bl.objects.api_object.RelationshipResponse;
import ca.xahive.app.bl.utils.AttachmentHandler;
import ca.xahive.app.bl.utils.Crypto;
import ca.xahive.app.bl.utils.CryptoHandler;
import ca.xahive.app.bl.utils.FileChooser;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.bl.utils.PollingTimer;
import ca.xahive.app.bl.utils.PollingTimerDelegate;
import ca.xahive.app.bl.utils.UserInfoCacheHelper;
import ca.xahive.app.ui.adapters.ConversationAdapter;
import ca.xahive.app.ui.cells.MessageCellAttachEncryptionListener;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;
import ca.xahive.app.ui.views.ChatEditText;
import ca.xahive.app.ui.views.ChatField;
import ca.xahive.app.ui.views.NavigationBar;
import ca.xahive.app.webservice.CallBackDone;


public class ChatView extends BaseActivity implements MessageCellAttachEncryptionListener, CryptoHandler.EncryptionDelegate, CryptoHandler.DecryptionDelegate, PollingTimerDelegate {
    private static int SELECT_PICTURE = 1;
    private static int SELECT_FILE = 3;
    private static int SELECT_VIDEO = 2;

    private PollingTimer timer;
    private boolean isObservingConversationModel;
    private Conversation convo;
    private ConversationModelItem convoModel;
    private ConversationAdapter convoAdapter;
    private ArrayList<PersonalMessage> conversationMessages;
    private Attachment attachment = null;
    private File attachmentFile = null;
    private String messageContent = null;

        
    private AttachmentHandler attachmentHandler;
    private CryptoHandler cryptoHandler;
    private ProgressDialog sendingMessageDialog;

    private String encryptionPassword = null;


    PersonalMessage selectedPersonalMessage = null;
    User selectedPersonalMessageFromUser = null;
    UserRelationship selectedPersonalMessageRelationship = null;
    private Message messageToDecrypt;

    public User getSelectedPersonalMessageFromUser() {
        return selectedPersonalMessageFromUser;
    }

    public void setSelectedPersonalMessageFromUser(User selectedPersonalMessageFromUser) {
        this.selectedPersonalMessageFromUser = selectedPersonalMessageFromUser;
    }

    public UserRelationship getSelectedPersonalMessageRelationship() {
        return selectedPersonalMessageRelationship;
    }

    public void setSelectedPersonalMessageRelationship(UserRelationship selectedPersonalMessageRelationship) {
        this.selectedPersonalMessageRelationship = selectedPersonalMessageRelationship;
    }

    public PersonalMessage getSelectedPersonalMessage() {
        return selectedPersonalMessage;
    }

    public void setSelectedPersonalMessage(PersonalMessage selectedPersonalMessage) {
        this.selectedPersonalMessage = selectedPersonalMessage;
    }

    public ConversationModelItem getConvoModel() {
        if (convoModel == null) {
            if (convo != null) {
                convoModel = Model.getInstance().getConversationModelForConversation(getConvo());
            }
            else {
                convoModel = new ConversationModelItem();
            }
        }
        return convoModel;
    }

    public void setAttachmentFile(File attachmentFile) {
        this.attachmentFile = attachmentFile;
        getChatField().getAttachButton().setSelected(attachmentFile != null);
    }

    public void setEncryptionPassword(String encryptionPassword) {
        this.encryptionPassword = encryptionPassword;

        getChatField().getLockButton().setSelected(this.encryptionPassword != null);
    }

    public void attachButtonPressed(final Message message) {
        //2014-07-12 Davoodinator
        //First check if we already downloaded it - and try and get it from the OS - if that fails then continue as-is
//        File file = new File((Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())+"/"+message.getFilename());
        File file =  Helpers.getDownloadedFileNamed(getBaseContext(), message.getFilename());
        if(file.exists()) {
            // We will do the following if the file already exists.
            AttachmentHandler.showDownloadCompleted(ChatView.this, message.getFilename());
            //otherwise do what was originally intended... download (and decrypt) the file!
        } else {
            getAttachmentHandler().downloadAttachment(this, message, new Runnable() {
                @Override
                public void run() {
                    if (message.getEncryptionState() == Message.EncryptionState.XAHMessageStateDecrypted) {
                        getCryptoHandler().decryptAttachment(message.getAttachmentId(), message.getFilename(), message.getFilesize(),"", getApplicationContext());
                    } else {
                        AttachmentHandler.showDownloadCompleted(ChatView.this, message.getFilename());
                    }
                }
            });
        }
    }

    public void lockButtonPressed(Message message) {
         messageToDecrypt = message;
        getCryptoHandler().decryptMessage(message);
    }


    private CryptoHandler getCryptoHandler() {
        cryptoHandler = new CryptoHandler(this, this, this);
        return cryptoHandler;
    }

    private AttachmentHandler getAttachmentHandler() {
        attachmentHandler = new AttachmentHandler();
        return attachmentHandler;
    }

    private ProgressDialog getSendingMessageDialog() {

        if(sendingMessageDialog == null) {
            sendingMessageDialog = SimpleAlertDialog.createBusyIndicator(this, getString(R.string.sending_message));
        }

        return sendingMessageDialog;

    }

    private Conversation getConvo() {
        return convo;
    }

    private void setConvo(Conversation convo) {
        int oldUserId = (this.convo != null) ? this.convo.getOtherUserId() : 0;
        int newUserId = (convo != null) ? convo.getOtherUserId() : 0;

        this.convo = convo;

        UserInfoCacheHelper.changeUserObservation(this, oldUserId, newUserId);

        updateTitle();
    }

    protected ChatField getChatField() {
        return (ChatField)findViewById(R.id.chatField);
    }

    public ListView getChatListView() {
        return (ListView)findViewById(R.id.chatListView);
    }

    protected NavigationBar getNavigationBar(){
        return (NavigationBar)findViewById(R.id.navBar);
    }

    public ConversationAdapter getConvoAdapter() {
        if (convoAdapter == null) {
            convoAdapter = new ConversationAdapter(
                    this,
                    R.layout.xa_chat_message_cell,
                    getConversationMessages()
            );
            getChatListView().setAdapter(convoAdapter);
        }
        return convoAdapter;
    }

    public ArrayList<PersonalMessage> getConversationMessages() {
        if (conversationMessages == null) {
            conversationMessages = new ArrayList<PersonalMessage>();
        }
        return conversationMessages;
    }

    public void setConversationMessages(ArrayList<PersonalMessage> conversationMessages) {
        getConversationMessages().clear();

        if (conversationMessages != null) {
            getConversationMessages().addAll(conversationMessages);
        }

        performApplyAllToMessages();
        getConvoAdapter().notifyDataSetChanged();

    }

    private void performApplyAllToMessages() {
        ArrayList<PersonalMessage> msgs = getConversationMessages();

        if (UserDefaults.getApplyPasswordToAll()) {

            for (PersonalMessage msg : msgs) {
                if (msg.getEncryptionState() == Message.EncryptionState.XAHMessageStateEncrypted) {
                    String decryptedContent = msg.decryptedContentWithContext(PasswordCacheContext.PRIVATE);
                    if (Helpers.stringIsNotNullAndMeetsMinLength(decryptedContent, 0)) {
                        Model.getInstance().getDecryptedMessageStore().setDecryptedTextForMessageId(decryptedContent, msg.getMessageId());
                    }
                }

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_view);
        setupChatFieldHandling();

        setupListViewOnClickListener();

        ParceledInteger parceledInteger = getIntent().getParcelableExtra(Parcelator.PARCELATOR_KEY);
        setConvo((Conversation) Model.getInstance().getParcelator().getObjectForParcel(parceledInteger));
        callAPIGetPublicKeyOfUser(String.valueOf(getConvo().getOtherUserId()),false);
    }

    private void setupListViewOnClickListener() {
        getChatListView().setClickable(true);
        getChatListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                PersonalMessage selectedPersonalMessage = (PersonalMessage) getChatListView().getItemAtPosition(position);

                if (selectedPersonalMessage.getFromUserId() == Model.getInstance().getCurrentUser().getUserId()) {
                    return;
                }

                setSelectedPersonalMessage(selectedPersonalMessage);
                setSelectedPersonalMessageFromUser(Model.getInstance().getUserInfoCache().userWithId(selectedPersonalMessage.getFromUserId()));

                setSelectedPersonalMessageRelationship(Model.getInstance().getUserRelationships().getRelationshipForUserId(getSelectedPersonalMessageFromUser().getUserId()));

                registerForContextMenu(getChatListView());
                openContextMenu(getChatListView());
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if(v.getId() == getChatField().getAttachButton().getId()){
            getMenuInflater().inflate(R.menu.attach_context_menu, menu);
            menu.setHeaderTitle(getString(R.string.attach_menu_title));
        }else if(v.getId() == getChatListView().getId()) {
            getMenuInflater().inflate(R.menu.chat_message_cell_context_menu, menu);

            menu.findItem(R.id.addToContactsItem).setVisible(false);
            menu.findItem(R.id.blockUserItem).setVisible(false);
            menu.findItem(R.id.shareMessageItem).setVisible(false);
            menu.findItem(R.id.reportMessageItem).setVisible(false);

            if (getSelectedPersonalMessageRelationship() != null) {
                if (getSelectedPersonalMessageRelationship().isContact()) {
                    menu.findItem(R.id.blockUserItem).setVisible(true);
                } else {
                    menu.findItem(R.id.addToContactsItem).setVisible(true);
                    menu.findItem(R.id.blockUserItem).setVisible(true);
                }
            } else {
                menu.findItem(R.id.addToContactsItem).setVisible(true);
                menu.findItem(R.id.blockUserItem).setVisible(true);
            }

            if (selectedPersonalMessage.getEncryptionState() != Message.EncryptionState.XAHMessageStateEncrypted) {
                menu.findItem(R.id.shareMessageItem).setVisible(true);
            }

            menu.findItem(R.id.reportMessageItem).setVisible(true);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        if(item.getGroupId() == R.id.attachContextMenuGroup){
            switch (item.getItemId()) {
                case R.id.option_photo:
                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, SELECT_PICTURE);
                    return true;
                case R.id.option_video:
                    Intent ii = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(ii, SELECT_VIDEO);
                    return true;
                case R.id.option_file:
                    Intent intent = new Intent(this, FileChooser.class);
                    startActivityForResult(intent, SELECT_FILE);
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }else if(item.getGroupId() == R.id.chatMessageCellContextMenuGroup){
            switch (item.getItemId()){
                case R.id.addToContactsItem:{
                    onAddToContactsItem();
                    break;
                }

                case R.id.blockUserItem:{
                    onBlockUserItem();
                    break;
                }

                case R.id.shareMessageItem:{
                    onShareMessageItem();
                    break;
                }

                case R.id.reportMessageItem:{
                    onReportMessageItem();
                    break;
                }

                default:
                    return super.onContextItemSelected(item);
            }
        }

        return super.onContextItemSelected(item);
    }

    private void onAddToContactsItem(){
        getSelectedPersonalMessageRelationship().setContact(true);
        Model.getInstance().updateUserRelationship(getSelectedPersonalMessageRelationship());
    }

    private void onBlockUserItem(){
        getSelectedPersonalMessageRelationship().setBlockedMessages(true);
        Model.getInstance().updateUserRelationship(getSelectedPersonalMessageRelationship());
    }

    private void onShareMessageItem(){
        String htmlStringBuilderString = getString(R.string.share_email_body);

        String htmlString = String.format(htmlStringBuilderString,
                getString(R.string.message_share_buzz_label),
                getSelectedPersonalMessage().getReadableContent(),
                getSelectedPersonalMessageFromUser().getAlias(),
                getSelectedPersonalMessage().getDate().toString(),
                "",
                "");

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/html");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.xahive_message));
        emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(htmlString));
        startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email_chooser_title)));
    }

    private void onReportMessageItem(){
        Model.getInstance().reportMessage(getSelectedPersonalMessage());
        SimpleAlertDialog.showMessageWithOkButton(this, getString(R.string.success), getString(R.string.message_reported), null);
    }

    private void handleAttachmentUpload() {
        Helpers.rotateSavedImage(attachmentFile);

        boolean isEncrypted = Helpers.stringIsNotNullAndMeetsMinLength(encryptionPassword, 1);

      //  if (isEncrypted && !getCryptoHandler().encryptAttachment(attachmentFile, encryptionPassword)) {
            // TODO notify user of failure.
        //    return;
       // }
        Attachment tempAttachment = new Attachment(attachmentFile.getName(), attachmentFile.length());
         /**
        AttachmentRequest attachmentRequest = new AttachmentRequest();

        attachmentRequest.setCallback(new APICallback() {
            @Override
            public void onSuccess(ModelObject modelObject) {
                attachment = (Attachment) modelObject;

                getAttachmentHandler().uploadAttachment(
                        ChatView.this,
                        attachmentFile,
                        attachment.getAttachmentKey(),
                        new Runnable() {
                            @Override
                            public void run() {
                                sendMessage();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                AttachmentHandler.showUploadFailedDialog(ChatView.this);
                                attachment = null;
                                setAttachmentFile(null);
                            }
                        }
                );
            }

            @Override
            public void onFail(ModelError modelError) {
                SimpleAlertDialog.showErrorWithOkButton(ChatView.this, modelError, null);
                attachment = null;
                setAttachmentFile(null);
            }

        });

        attachmentRequest.post(tempAttachment); **/
        callAPIAttachmentUploat(tempAttachment);

    }
     private void callAPIAttachmentUploat(Attachment tempAttachment)
     {

     }
     private String toDeviceID ="";
     public static String publicKeyAsString ="";
    private PublicKey publicKey = null;

    public PublicKey getPublicKey()
    {
        return  publicKey;
    }
    private void callAPIGetPublicKeyOfUser(String idUser,final  boolean isSendMessage)
    {

        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {

                if (result != null) {
                    Gson gson = new Gson();
                    PublicKeyOfUserResponse userResponse = gson.fromJson(result.toString(), PublicKeyOfUserResponse.class);
                    try {
                        if (userResponse.pk.size() > 0) {
                            toDeviceID = userResponse.pk.get(0).deviceId;
                            publicKeyAsString = userResponse.pk.get(0).publicKey;
                            publicKey = Helpers.getPublicKeyFromPemFormat(publicKeyAsString, false);
                            if (isSendMessage) {
                                sendMessage(userResponse.pk.get(0).deviceId, userResponse.pk.get(0).publicKey);
                            }
                        }
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (InvalidKeySpecException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {

                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(ModelObject modelObject) {

            }

            @Override
            public void onFail(ModelError error) {
                Log.v("testlog", "error" + error.getCode());

            }

            @Override
            public void onComplete() {

            }
        });
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token", Model.getInstance().getCurrentUser().getToken()));
        params.add(new BasicNameValuePair("client", "www"));
        APIConnectionRequest.API_GetUserPublicKey(callBackDone,"publicKey/"+ idUser, params, Model.getInstance().getCurrentUser().getToken());

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       File file = null;
       if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
           String path = Helpers.getRealPathFromURI(data.getData());
           file = new File(path);
       } else if (requestCode == SELECT_VIDEO && resultCode == RESULT_OK) {
           String path = Helpers.getRealPathFromURI(data.getData());
           file = new File(path);
       } else if (requestCode == SELECT_FILE && resultCode == RESULT_OK) {
           if (data.hasExtra(FileChooser.SELECT_FILE_INTENT_KEY)) {
               String path = data.getExtras().getString(FileChooser.SELECT_FILE_INTENT_KEY);

               try {

                   file = new File(path);

               } catch (Exception e) {

                   AttachmentHandler.showUploadFailedDialog(this);

                   attachment = null;
                   setAttachmentFile(null);
                   return;

               }
           }
       }
        if(file != null) {
            double fileSize = file.length();
             if (Helpers.fileWithinSizeLimitsForPersonalMessage(fileSize)) {
                setAttachmentFile(file);
            } else {
                AttachmentHandler.showExceedsFileSizeDialog(this, false);
                attachment = null;
                setAttachmentFile(null);
            }
        } else {
            attachment = null;
            setAttachmentFile(null);
        }
    }

    private void setupChatFieldHandling() {

        getChatField().getChatEditText().setOnBackButtonListener(new ChatEditText.OnBackButtonListener() {
            @Override
            public boolean OnEditTextBackButton() {
                finish();
                return true;
            }
        });

        getChatField().getChatEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    messageContent = Helpers.extractStringFromTextView(v);
                    submitMessagePressed();
                    return true;
                }
                return false;
            }
        });

        getChatField().getLockButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCryptoHandler().promptForEncryptionPassword(encryptionPassword);
            }
        });

        getChatField().getAttachButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAddAttachment();
            }
        });
    }

    private void handleAddAttachment() {

        Runnable onRemove = new Runnable() {
            @Override
            public void run() {
                attachment = null;
                setAttachmentFile(null);
            }
        };

        if(this.attachmentFile != null) {
            SimpleAlertDialog.showMessageWithCancelAndAcceptButtons(this,
                    getString(R.string.remove_attachment),
                    null,
                    getString(R.string.cancel),
                    getString(R.string.remove),
                    null,
                    onRemove
            );
        } else {
            showAttachContextMenu();
        }
    }

    private void showAttachContextMenu () {
        registerForContextMenu(getChatField().getAttachButton());
        openContextMenu(getChatField().getAttachButton());
    }

    @Override
    protected void setIsVisible(boolean isVisible) {
        super.setIsVisible(isVisible);

        if (isVisible) {
            observeConversationModel(true);
        }
        else {
            observeConversationModel(false);
        }
    }

    @Override
    public void timerDidFire(PollingTimer timer) {
        getConvoModel().loadNewerConversations();
    }

    private void observeConversationModel(boolean observe) {
        if (isObservingConversationModel == observe) {
            return;
        }

        isObservingConversationModel = observe;

        if (this.convo != null && observe) {
            getConvoModel().addObserver(this, true);
            getConvoModel().loadNewerConversations();

            timer = new PollingTimer(this);
            timer.start(false);
        }
        else {
            getConvoModel().deleteObserver(this);

            if (timer != null) {
                timer.stop();
                timer = null;
            }
        }
    }

    public void update(Observable observable, Object o) {
        if (observable == getConvoModel()) {
            if(getConvoModel().getSentMessage() != null) {
                getSendingMessageDialog().dismiss();
                onMessageSent();
            } else if (getConvoModel().getError() != null) {
                getSendingMessageDialog().dismiss();

                SimpleAlertDialog.showErrorWithOkButton(
                        this,
                        new ModelError(ModelError.SEND_MSG_ERROR),
                        null
                );

                clearMessage();
            }

            onConvoMessagesUpdated();
        }
        else if (observable == Model.getInstance().getUserInfoCache()) {
            updateTitle();
        }
    }

    private void updateTitle() {
        if(Model.getInstance().getUserInfoCache()!=null && this.convo!=null)
           getNavigationBar().setTitle( Model.getInstance().getUserInfoCache().userWithId( this.convo.getOtherUserId() ).getAlias() );
    }

    private void onConvoMessagesUpdated() {
        PersonalMessageList messageList = (PersonalMessageList)getConvoModel().getData();

        if (messageList != null) {
            ArrayList<PersonalMessage> messages = messageList.getPersonalMessages();
            setConversationMessages(messages);
        }
    }

    private void submitMessagePressed() {
        boolean messageValid = Helpers.stringIsNotNullAndMeetsMinLength(messageContent, 1);

        if(messageValid) {
            if (attachmentFile != null) {
                handleAttachmentUpload();
            } else {
                if(publicKey==null)
                 callAPIGetPublicKeyOfUser(String.valueOf(getConvo().getOtherUserId()),true);
                else
                    try {
                        sendMessage(toDeviceID,publicKeyAsString);
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    }

            }
        }else{
            SimpleAlertDialog.showMessageWithOkButton(this, getResources().getString(R.string.error), getString(R.string.chat_view_please_enter_message), null);
        }
    }

    private void sendMessage (String toDeviceId,String publicKeyOfUser ) throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        if (!Helpers.stringIsNotNullAndMeetsMinLength(messageContent, 1)) {
            return;
        }

        getSendingMessageDialog().show();

        final PersonalMessage newMessage = getConvo().messageWithReadableContent(messageContent);
        //newMessage.setToUserId("");
        newMessage.setEncrypted(Helpers.stringIsNotNullAndMeetsMinLength(encryptionPassword, 1));

        //newMessage.setToUserId(5199);
        if (attachment != null && attachment.getAttachmentId() > 0) {
            newMessage.setAttachmentId(attachment.getAttachmentId());
        } else {
            newMessage.setAttachmentId(-1);
        }
        UUID uuid = UUID.randomUUID();


        String contentKey = "";
        String refKey = "";
        String secret = uuid.toString();

        if (cryptoHandler == null)
            getCryptoHandler();

        refKey = Crypto.RSAEncrypt(secret, UserDefaults.getPublicKeyWithHiveAsString(Model.getInstance().getCurrentUser().getUserId(),"1"));
        contentKey = Crypto.RSAEncrypt(secret, publicKeyOfUser);
        String encryptedContent = cryptoHandler.encryptMessageWithPassword(messageContent, secret);
         try {
            newMessage.contentKey = contentKey;
            newMessage.refKey = refKey;

        } catch (Exception e) {
            e.printStackTrace();
        }

        newMessage.setToDeviceId(toDeviceId);

        newMessage.setContent(encryptedContent);
        if (newMessage.isEncrypted()) {
            if (encryptedContent != null) {
                getConvoModel().sendMessage(newMessage);
            }

        } else {
            newMessage.isEncrypted = true;

            getConvoModel().sendMessage(newMessage);
        }

    }
    public  String encryptWithText(String publicKeyOfUser, String textMessage)
    {

        AsymmetricKeyParameter publicKeyTemp = null;
        final Reader reader = new StringReader(publicKeyOfUser);
        PemReader pemReader = new PemReader(reader);
        PemObject pemObject= null;
        try {
            pemObject = pemReader.readPemObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            pemReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            publicKeyTemp  =  (AsymmetricKeyParameter) PublicKeyFactory.createKey(pemObject.getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        AsymmetricBlockCipher e = new RSAEngine();
        e = new PKCS1Encoding(e);
        e.init(true, publicKeyTemp);

        int i = 0;
        int len = e.getInputBlockSize();
        byte[] messageBytes = new byte[0];
        try {
            messageBytes = Base64.encode(textMessage.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String value = "";
        while (i < messageBytes.length)
        {
            if (i + len > messageBytes.length)
                len = messageBytes.length - i;

            byte[] hexEncodedCipher = new byte[0];
            try {
                hexEncodedCipher = e.processBlock(messageBytes, i, len);

                value = value + new String(hexEncodedCipher, "ISO-8859-1");
            } catch (InvalidCipherTextException e1) {
                e1.printStackTrace();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            i += e.getInputBlockSize();
        }
        try {
            return new String(value.getBytes(),"utf-8");
        } catch (UnsupportedEncodingException e1) {
            return  "";
        }
    }
    public static String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
    private void onMessageSent() {

        if(Helpers.stringIsNotNullAndMeetsMinLength(encryptionPassword, 1)) {
            if (UserDefaults.getDecryptSave()) {
                Model.getInstance().getDecryptedMessageStore().setDecryptedTextForMessageId(messageContent, getConvoModel().getSentMessage().getMessageId());
            }

            if (getConvoModel().getSentMessage().getAttachmentId() > 0) {
                Model.getInstance().getPasswordCache().setPasswordForIdentifierInContext(messageContent, getConvoModel().getSentMessage().getAttachmentId(), PasswordCacheContext.ATTACHMENT);
            }
        }
        clearMessage();
        getConvoModel().setSentMessage(null);

    }

    private void clearMessage(){
        getChatField().getChatEditText().setText("");
        messageContent = null;
        attachment = null;
        setEncryptionPassword(null);
        setAttachmentFile(null);
    }

    @Override
    public void onResume(){
        super.onResume();

        Bundle savedAttachment = Model.getInstance().getShareBundle();

        if (savedAttachment != null) {
            Model.getInstance().setShareBundle(null); // Clear out to avoid looping.
            File attachFile = Helpers.fileFromShareBundle(this, savedAttachment);
            if (attachFile != null) {
                setAttachmentFile(attachFile);
            }
            else {
                SimpleAlertDialog.showMessageWithOkButton(this, getString(R.string.error), getString(R.string.attach_file_error), null);
            }
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void finish() {
        if (this.convo != null) {
            Model.getInstance().getUserInfoCache().removeObserverForUserId(this, this.convo.getOtherUserId());
        }

        Model.getInstance().getConversationList().setState(ModelState.STALE);

        super.finish();
    }
    
    @Override
    public void encryptionPasswordSet(String encryptionPassword) {
        setEncryptionPassword(encryptionPassword);
    }

    @Override
    public void attachmentFileUpdated(File attachmentFile) {
        setAttachmentFile(attachmentFile);

        if(attachmentFile == null) {
            attachment = null;
        }
    }

    @Override
    public void messageDecryptionComplete(boolean success) {
        if(success) {
            performApplyAllToMessages();
            getConvoAdapter().notifyDataSetChanged();
        }

        if (messageToDecrypt != null) {
            int idx = getConversationMessages().indexOf(messageToDecrypt);
            if (idx >= 0) {
                getChatListView().smoothScrollToPosition(idx);
            }
            messageToDecrypt = null;
        }
    }
}