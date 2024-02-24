package ca.xahive.app.ui.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemReader;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ca.xahive.app.bl.api.queries.APIConnectionRequest;
import ca.xahive.app.bl.local.Attachment;
import ca.xahive.app.bl.local.ConversationModelItem;
import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.local.Parcelator;
import ca.xahive.app.bl.local.ParceledInteger;
import ca.xahive.app.bl.local.PasswordCacheContext;
import ca.xahive.app.bl.local.UserDefaults;
import ca.xahive.app.bl.objects.Conversation;
import ca.xahive.app.bl.objects.ConversationList;
import ca.xahive.app.bl.objects.Message;
import ca.xahive.app.bl.objects.ModelError;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.ModelObject;
import ca.xahive.app.bl.objects.ModelState;
import ca.xahive.app.bl.objects.PersonalMessage;
import ca.xahive.app.bl.objects.PersonalMessageList;
import ca.xahive.app.bl.objects.User;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.bl.objects.api_object.AttachmentRequest;
import ca.xahive.app.bl.objects.api_object.AttachmentRequestObject;
import ca.xahive.app.bl.objects.api_object.AttachmentResponse;
import ca.xahive.app.bl.objects.api_object.MessageObjectRequest;
import ca.xahive.app.bl.objects.api_object.MessagePostRequest;
import ca.xahive.app.bl.objects.api_object.PublicKeyOfUserResponse;
import ca.xahive.app.bl.objects.api_object.PublicKeyRequest;
import ca.xahive.app.bl.utils.AttachmentHandler;
import ca.xahive.app.bl.utils.ChatStarter;
import ca.xahive.app.bl.utils.Crypto;
import ca.xahive.app.bl.utils.CryptoHandler;
import ca.xahive.app.bl.utils.FileChooser;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.bl.utils.PollingTimer;
import ca.xahive.app.bl.utils.PollingTimerDelegate;
import ca.xahive.app.bl.utils.UserInfoCacheHelper;
import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.ui.adapters.ConversationAdapter;
import ca.xahive.app.ui.cells.MessageCellAttachEncryptionListener;
import ca.xahive.app.ui.dialogs.SimpleAlertDialog;
import ca.xahive.app.ui.views.ChatEditText;
import ca.xahive.app.ui.views.ChatField;
import ca.xahive.app.ui.views.LoadingView;
import ca.xahive.app.ui.views.NavigationBar;
import ca.xahive.app.webservice.CallBackDone;

/**
 * Created by Hoan on 10/20/2015.
 */
public class MessagesChatNewFragment extends BaseFragment implements MessageCellAttachEncryptionListener,
        CryptoHandler.EncryptionDelegate, CryptoHandler.DecryptionDelegate, PollingTimerDelegate {


    private static int SELECT_PICTURE = 1;
    private static int SELECT_FILE = 3;
    private static int SELECT_VIDEO = 2;

    private PollingTimer timer;
    private boolean isNewConvesation = false;
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


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MessagesChatNewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MessagesChatNewFragment newInstance(String param1, String param2) {
        MessagesChatNewFragment fragment = new MessagesChatNewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MessagesChatNewFragment() {
    }


    public static final String ID_CONTACT = "ID_CONTACT";
    public static int idContactToSend = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // mParam1 = getArguments().getString(ARG_PARAM1);
            //Model.getInstance().getConversationList().addObserver(this, true);
            ParceledInteger parceledInteger = getArguments().getParcelable(Parcelator.PARCELATOR_KEY);
            if (parceledInteger != null) {
                setConvo((Conversation) Model.getInstance().getParcelator().getObjectForParcel(parceledInteger));
                callAPIGetPublicKeyOfUser(String.valueOf(getConvo().getOtherUserId()), false);
            } else {
                isNewConvesation = true;
                if (getArguments().containsKey(ID_CONTACT)) {
                    idContactToSend = getArguments().getInt(ID_CONTACT);

                }
            }

        }
    }

    private RelativeLayout getGroupAddNewMessage() {
        return (RelativeLayout) rootView.findViewById(R.id.addContactToSendMessage);
    }

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
            } else {
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
        //File file = new File((Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())+"/"+message.getFilename());

        //File file =  Helpers.getDownloadedFileNamed(getContext(),message.getFilename());
        Boolean isExits = Helpers.checkFileDownloadExits(message.getFilename());
        String path = UserDefaults.getAttachmentID(String.valueOf(message.getAttachmentId()), message.getFilename());
        if (isExits) {
            // We will do the following if the file already exists.
            AttachmentHandler.showDownloadCompleted(getActivity(), message.getFilename());
            //otherwise do what was originally intended... download (and decrypt) the file!
        } else if (Helpers.isExistFile(path)) {
            AttachmentHandler.showFileLocalWithPath(getActivity(), path);
        } else {
            getAttachmentHandler().downloadAttachment(getActivity(), message, new Runnable() {
                @Override
                public void run() {
                    // EncryptionState.XAHMessageStateDecrypte
                    if (message.getEncryptionState() == Message.EncryptionState.XAHMessageStateEncrypted) {
                        if (message.fromUserId != Model.getInstance().getCurrentUser().getUserId()) {
                            String contentKeyTemp = null;
                            try {
                                contentKeyTemp = Crypto.RSADecrypt(message.contentKey, UserDefaults.getPrivateKeyWithHiveAsString(Model.getInstance().getCurrentUser().getUserId(), "1"));

                            } catch (Exception e) {

                            }
                            getCryptoHandler().decryptAttachment(message.getAttachmentId(), message.getFilename(), message.getFilesize(), contentKeyTemp, getContext());
                        } else {
                            String contentKeyTemp = null;
                            try {
                                contentKeyTemp = Crypto.RSADecrypt(message.refKey, UserDefaults.getPrivateKeyWithHiveAsString(Model.getInstance().getCurrentUser().getUserId(), "1"));

                            } catch (Exception e) {

                            }
                            getCryptoHandler().decryptAttachment(message.getAttachmentId(), message.getFilename(), message.getFilesize(), contentKeyTemp, getContext());

                        }
                    } else {

                        AttachmentHandler.showDownloadCompleted(getActivity(), message.getFilename());
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
        cryptoHandler = new CryptoHandler(this, this, getActivity());
        return cryptoHandler;
    }

    private AttachmentHandler getAttachmentHandler() {
        attachmentHandler = new AttachmentHandler();
        return attachmentHandler;
    }

    private ProgressDialog getSendingMessageDialog() {
        if (sendingMessageDialog == null) {
            sendingMessageDialog = SimpleAlertDialog.createBusyIndicator(getActivity(), getString(R.string.sending_message));
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
        return (ChatField) rootView.findViewById(R.id.chatField);
    }

    public ListView getChatListView() {
        return (ListView) rootView.findViewById(R.id.chatListView);
    }

    protected NavigationBar getNavigationBar() {
        return (NavigationBar) ((MessagesListMainFragment) getParentFragment()).getNavigationBar();
    }

    public ConversationAdapter getConvoAdapter() {
        if (convoAdapter == null) {
            convoAdapter = new ConversationAdapter(
                    getActivity(),
                    R.layout.xa_chat_message_cell,
                    getConversationMessages()
            );
            getChatListView().setAdapter(convoAdapter);
            convoAdapter.notifyDataSetChanged();

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

        getLoadingView().setVisibility(View.GONE);
        getChatListView().setVisibility(View.VISIBLE);
        if (conversationMessages != null) {
            if (isNewConvesation) {
                isNewConvesation = false;
                if (getConversationMessages().size() > 0) {
                    if (getConversationMessages().get(0).getContent().equals(conversationMessages.get(0).getContent())) {
                        conversationMessages = new ArrayList<PersonalMessage>();
                    }
                }
            }
            if (conversationMessages.size() == 1) {
                /**
                 for(int i= getConversationMessages().size()-1;i>=0;i--)
                 {
                 if(getConversationMessages().get(i).getContent().equals(conversationMessages.get(0).getContent()))
                 {
                 return;
                 }
                 }**/
            }

            getConversationMessages().addAll(conversationMessages);

        }

        performApplyAllToMessages();
        getConvoAdapter().notifyDataSetChanged();

    }

    private AutoCompleteTextView getInputIDNewMessage() {
        return (AutoCompleteTextView) rootView.findViewById(R.id.control_add_Input);
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

    View rootView;

    protected LoadingView getLoadingView() {
        return (LoadingView) rootView.findViewById(R.id.listHandlerFragmentLoadingView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_chat_messages, container, false);
        setupChatFieldHandling();
        if (!isNewConvesation) {
            getLoadingView().setVisibility(View.VISIBLE);
            getChatListView().setVisibility(View.GONE);
        }
        setupListViewOnClickListener();
        //observeConversationModel(true);
        if (convo != null) {

            getGroupAddNewMessage().setVisibility(View.GONE);
        } else {
            getGroupAddNewMessage().setVisibility(View.VISIBLE);
            getInputIDNewMessage().setFocusable(true);
            getInputIDNewMessage().requestFocus();
            Helpers.showSoftKeyboardForEditText(getInputIDNewMessage());
        }
        getAddButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAddNewUserToChat = true;
                if (getInputIDNewMessage().getText().toString().length() > 2) {

                    ConversationList conversationList = (ConversationList) Model.getInstance().getConversationList().getData();
                    for (int i = 0; i < conversationList.conversation.size(); i++) {
                        Conversation conversation = conversationList.conversation.get(i);

                        if (String.valueOf(conversation.getOtherUserId()).equals(getInputIDNewMessage().getText().toString())) {
                            ParceledInteger parceledInteger = ChatStarter.parceledInteger(conversation);
                            ((MessagesListMainFragment) getParentFragment()).setLayoutMessage(parceledInteger);
                            return;
                        }
                    }
                    callAPIGetPublicKeyOfUser(getInputIDNewMessage().getText().toString(), false);
                }
            }
        });
        if (idContactToSend > 0) {
            getInputIDNewMessage().setText(String.valueOf(idContactToSend));
            isAddNewUserToChat = true;
            if (getInputIDNewMessage().getText().toString().length() > 3) {
                callAPIGetPublicKeyOfUser(getInputIDNewMessage().getText().toString(), false);
            }
            idContactToSend = -1;
        }
        return rootView;
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
                getActivity().openContextMenu(getChatListView());
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == getChatField().getAttachButton().getId()) {
            getActivity().getMenuInflater().inflate(R.menu.attach_context_menu, menu);
            menu.setHeaderTitle(getString(R.string.attach_menu_title));
        } else if (v.getId() == getChatListView().getId()) {
            getActivity().getMenuInflater().inflate(R.menu.chat_message_cell_context_menu, menu);

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
        if (item.getGroupId() == R.id.attachContextMenuGroup) {
            switch (item.getItemId()) {
                case R.id.option_photo:
                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    getActivity().startActivityForResult(i, SELECT_PICTURE);
                    return true;
                case R.id.option_video:
                    Intent ii = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    getActivity().startActivityForResult(ii, SELECT_VIDEO);
                    return true;
                case R.id.option_file:
                    Intent intent = new Intent(getActivity(), FileChooser.class);
                    getActivity().startActivityForResult(intent, SELECT_FILE);
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        } else if (item.getGroupId() == R.id.chatMessageCellContextMenuGroup) {
            switch (item.getItemId()) {
                case R.id.addToContactsItem: {
                    onAddToContactsItem();
                    break;
                }

                case R.id.blockUserItem: {
                    onBlockUserItem();
                    break;
                }

                case R.id.shareMessageItem: {
                    onShareMessageItem();
                    break;
                }

                case R.id.reportMessageItem: {
                    onReportMessageItem();
                    break;
                }

                default:
                    return super.onContextItemSelected(item);
            }
        }

        return super.onContextItemSelected(item);
    }

    private void onAddToContactsItem() {
        getSelectedPersonalMessageRelationship().setContact(true);
        Model.getInstance().updateUserRelationship(getSelectedPersonalMessageRelationship());
    }

    private void onBlockUserItem() {
        getSelectedPersonalMessageRelationship().setBlockedMessages(true);
        Model.getInstance().updateUserRelationship(getSelectedPersonalMessageRelationship());
    }

    private void onShareMessageItem() {
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

    private void onReportMessageItem() {
        Model.getInstance().reportMessage(getSelectedPersonalMessage());
        SimpleAlertDialog.showMessageWithOkButton(getActivity(), getString(R.string.success), getString(R.string.message_reported), null);
    }

    private void handleAttachmentUpload() {
        Helpers.rotateSavedImage(attachmentFile);
        final UUID uuid = UUID.randomUUID();
        final String secret = uuid.toString();

        boolean isEncrypted = Helpers.stringIsNotNullAndMeetsMinLength(encryptionPassword, 1);
        final String filePath = attachmentFile.getAbsolutePath();
        final String fileName = attachmentFile.getName();
        final double length = attachmentFile.length();
        if (!getCryptoHandler().encryptAttachment(attachmentFile, secret, getActivity().getApplicationContext())) {
            // TODO notify user of failure.
            return;
        }
        final PersonalMessage newMessage = new PersonalMessage();
        // final String contentKey = "";
        // final  String refKey = "";
        final String refKey;
        final String contentKey;
        try {
            refKey = Crypto.RSAEncrypt(secret, UserDefaults.getPublicKeyWithHiveAsString(Model.getInstance().getCurrentUser().getUserId(), "1"));
            contentKey = Crypto.RSAEncrypt(secret, publicKeyAsString);
            Attachment tempAttachment = new Attachment(fileName, length);


            callAPIAttachmentUpload(tempAttachment, new Runnable() {
                @Override
                public void run() {
                    if (cryptoHandler == null)
                        getCryptoHandler();
                    //attachmentFile =  Helpers.createFileWithDataAndFilename(Helpers.fileToByteArray(attachmentFile),
                    //       attachment.getAttachmentKey(),getContext())  ;

                    String encryptedContent = cryptoHandler.encryptMessageWithPassword(messageContent, secret);
                    newMessage.contentKey = contentKey;
                    newMessage.refKey = refKey;
                    newMessage.setFilesize(length);
                    newMessage.setFilename(fileName);
                    newMessage.setToDeviceId(toDeviceID);
                    newMessage.setContent(encryptedContent);
                    UserDefaults.saveAttachmentID(String.valueOf(attachment.getAttachmentId()), filePath);
                    //if(getCryptoHandler().encryptAttachment(attachmentFile, secret,attachment.getAttachmentKey(), getActivity().getApplicationContext())) {
                    getAttachmentHandler().uploadAttachment(
                            getActivity(),
                            attachmentFile,
                            attachment.getAttachmentKey(),

                            // "b4NRs+/Ziu6fehZ88Vno4XObu/4pE13+4Re/uO5l",
                            new Runnable() {
                                @Override
                                public void run() {
                                    try {

                                        sendMessage(newMessage);
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
                            },
                            new Runnable() {
                                @Override
                                public void run() {
                                    AttachmentHandler.showUploadFailedDialog(getActivity());
                                    attachment = null;
                                    setAttachmentFile(null);
                                }
                            }
                    );
                    // }


                }
            });

        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private void callAPIAttachmentUpload(Attachment tempAttachment, final Runnable runnable) {
        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {

                if (result != null) {
                    Gson gson = new Gson();
                    AttachmentResponse attachmentResponse = gson.fromJson(result.toString(), AttachmentResponse.class);
                    if (attachmentResponse != null) {
                        if (attachmentResponse.attachment == null) {
                            return;
                        }
                        if (attachmentResponse.attachment.size() > 0) {
                            attachment = new Attachment();
                            Log.v("callAPIAttachment", String.valueOf(attachmentResponse.attachment.get(0).attachmentId));
                            attachment.setAttachmentId(attachmentResponse.attachment.get(0).attachmentId);
                            runnable.run();
                        }
                    }
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

            }

            @Override
            public void onComplete() {

            }
        });

        AttachmentRequest attachmentRequest = new AttachmentRequest();
        AttachmentRequestObject attachmentRequestObject = new AttachmentRequestObject();
        attachmentRequestObject.filename = tempAttachment.getFilename();
        attachmentRequestObject.filesize = tempAttachment.getFilesize();
        attachmentRequest.attachment = attachmentRequestObject;
        APIConnectionRequest.API_AttachmentUpload(callBackDone, attachmentRequest, Model.getInstance().getCurrentUser().getToken());
    }

    private String toDeviceID = "";
    public static String publicKeyAsString = "";
    private PublicKey publicKey = null;
    private boolean isAddNewUserToChat = false;

    public PublicKey getPublicKey() {
        return publicKey;
    }

    private void callAPIGetPublicKeyOfUser(final String idUser, final boolean isSendMessage) {
        if (isAddNewUserToChat) {
            getBusyIndicator(R.string.checking_user).show();
        }
        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {

                if (result != null) {
                    if (isAddNewUserToChat) {

                        getBusyIndicator().dismiss();
                    }
                    Gson gson = new Gson();
                    PublicKeyOfUserResponse userResponse = gson.fromJson(result.toString(), PublicKeyOfUserResponse.class);
                    try {
                        if (userResponse.pk.size() > 0) {
                            toDeviceID = userResponse.pk.get(0).deviceId;
                            publicKeyAsString = userResponse.pk.get(0).publicKey;
                            publicKey = Helpers.getPublicKeyFromPemFormat(publicKeyAsString, false);
                            if (isSendMessage) {
                                sendMessage(userResponse.pk.get(0).deviceId, userResponse.pk.get(0).publicKey);
                            } else if (isAddNewUserToChat) {
                                isAddNewUserToChat = false;
                                convo = new Conversation();
                                convo.setToUserId(Integer.parseInt(idUser));
                                convo.toDeviceId = toDeviceID;
                                getGroupAddNewMessage().setEnabled(false);
                                getInputIDNewMessage().setEnabled(false);

                                /**
                                 * show keyboard on chat view
                                 */
                                getChatField().getChatEditText().setFocusable(true);
                                getChatField().getChatEditText().requestFocus();
                                Helpers.showSoftKeyboardForEditText(getChatField().getChatEditText());
                                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                            }
                        } else {

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
        APIConnectionRequest.API_GetUserPublicKey(callBackDone, "publicKey/" + idUser, params, Model.getInstance().getCurrentUser().getToken());

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Toast.makeText(getActivity(), "Fragment Got it: ", Toast.LENGTH_SHORT).show();

        File file = null;
        if (requestCode == SELECT_PICTURE && resultCode == getActivity().RESULT_OK) {
            String path = Helpers.getRealPathFromURI(data.getData());
            file = new File(path);
        } else if (requestCode == SELECT_VIDEO && resultCode == getActivity().RESULT_OK) {
            String path = Helpers.getRealPathFromURI(data.getData());
            file = new File(path);
        } else if (requestCode == SELECT_FILE && resultCode == getActivity().RESULT_OK) {
            if (data.hasExtra(FileChooser.SELECT_FILE_INTENT_KEY)) {
                String path = data.getExtras().getString(FileChooser.SELECT_FILE_INTENT_KEY);

                try {

                    file = new File(path);

                } catch (Exception e) {

                    AttachmentHandler.showUploadFailedDialog(getActivity());

                    attachment = null;
                    setAttachmentFile(null);
                    return;

                }
            }
        }
        if (file != null) {
            double fileSize = file.length();
            if (Helpers.fileWithinSizeLimitsForPersonalMessage(fileSize)) {
                setAttachmentFile(file);
            } else {
                AttachmentHandler.showExceedsFileSizeDialog(getActivity(), false);
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

        if (this.attachmentFile != null) {
            SimpleAlertDialog.showMessageWithCancelAndAcceptButtons(getActivity(),
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

    private void showAttachContextMenu() {
        registerForContextMenu(getChatField().getAttachButton());
        getActivity().openContextMenu(getChatField().getAttachButton());
    }

    /**
     * @Override protected void setIsVisible(boolean isVisible) {
     * //super.setIsVisible(isVisible);
     * <p/>
     * if (isVisible) {
     * observeConversationModel(true);
     * }
     * else {
     * observeConversationModel(false);
     * }
     * }
     **/

    @Override
    public void timerDidFire(PollingTimer timer) {
        //  ConversationList conversationList = (ConversationList) Model.getInstance().getConversationList().getData();
        /**
         if(isNewConvesation) {
         for (int i = conversationList.getConversations().size() - 1; i >= 0; i--) {
         if (conversationList.getConversations().get(i).getConversationId() == getConvo().getConversationId()) {
         setConvo(conversationList.getConversations().get(i));
         }

         }
         }**/
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
        } else {
            getConvoModel().deleteObserver(this);

            if (timer != null) {
                timer.stop();
                timer = null;
            }
        }
    }

    @Override
    protected void onModelUpdated(ModelEvent evt) {
        super.onModelUpdated(evt);

    }

    @Override
    public void update(Observable observable, Object o) {
        if (getSendingMessageDialog().isShowing()) {
            clearMessage();
        }

        getSendingMessageDialog().dismiss();

        //  clearMessage();
        if (observable == getConvoModel()) {
            if (getConvoModel().getSentMessage() != null) {
                clearMessage();

                // getSendingMessageDialog().dismiss();
                convo.setConversationId(getConvoModel().getSentMessage().getConversationId());
                onMessageSent();

                //clearMessage();
            } else if (getConvoModel().getError() != null) {
                clearMessage();
                SimpleAlertDialog.showErrorWithOkButton(
                        getActivity(),
                        new ModelError(ModelError.SEND_MSG_ERROR),
                        null
                );

                //clearMessage();
            }
            onConvoMessagesUpdated();
        } else if (observable == Model.getInstance().getUserInfoCache()) {

            updateTitle();
        }
    }

    private void createNewMessage(PersonalMessage message) {
        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    Gson gson = new com.google.gson.GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
                    PersonalMessageList returnedMessageList = gson.fromJson(result.toString(), PersonalMessageList.class);
                    //getConvoModel().setSentMessage(returnedMessageList.getPersonalMessages().get(0));
                    getSendingMessageDialog().dismiss();
                    //setConversationMessages(returnedMessageList.getPersonalMessages());

                    Conversation convo = new Conversation();
                    convo.setToUserId(returnedMessageList.getPersonalMessages().get(0).getToUserId());
                    convo.setConversationId(returnedMessageList.getPersonalMessages().get(0).getConversationId());
                    convo.setFromUserId(returnedMessageList.getPersonalMessages().get(0).getFromUserId());

                    setConvo(convo);
                    getConvoModel().setConversation(convo);
                    //observeConversationModel(true);
                    clearMessage();
                    //getConvoModel().setConversationId();
                    getConvoModel().callAPIRequestForNewer();
                    getMessageListAPI();
                    //Log.v("string",getConvoModel().getURLString());
                }
            }

            @Override
            public void onStart() {
                //getUserRelationships().setPendingIfError();
            }

            @Override
            public void onSuccess(ModelObject modelObject) {


            }

            @Override
            public void onFail(ModelError error) {
            }

            @Override
            public void onComplete() {

            }
        });
        MessagePostRequest messagePostRequest = new MessagePostRequest();
        MessageObjectRequest messageObjectRequest = new MessageObjectRequest();
        messageObjectRequest.attachmentId = message.getAttachmentId();
        messageObjectRequest.content = message.getContent();
        messageObjectRequest.contentKey = message.contentKey;
        messageObjectRequest.fromDeviceId = Model.getInstance().getDeviceID();
        messageObjectRequest.isEncrypted = true;
        messageObjectRequest.toUserId = message.toUserId;
        messageObjectRequest.toHiveId = 1;
        messageObjectRequest.toDeviceId = message.toDeviceId;

        messageObjectRequest.refKey = message.refKey;
        messagePostRequest.message = messageObjectRequest;
        APIConnectionRequest.API_PostConversationItem(callBackDone, getConvoModel().getURLString(), messagePostRequest, Model.getInstance().getCurrentUser().getToken());

    }

    private void getMessageListAPI() {
        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {
                    Gson gson = new Gson();

                    ConversationList conversationList = gson.fromJson(result.toString(), ConversationList.class);
                    if (conversationList != null) {
                        //ConversationList conversationList = (ConversationList) Model.getInstance().getConversationList().getData();

                        for (int i = conversationList.getConversations().size() - 1; i >= 0; i--) {
                            if (conversationList.getConversations().get(i).getConversationId() == getConvo().getConversationId()) {
                                setConvo(conversationList.getConversations().get(i));
                            }
                        }
                    }
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
                //super.onFail(error);
            }

            @Override
            public void onComplete() {

            }
        });

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token", Model.getInstance().getCurrentUser().getToken()));
        params.add(new BasicNameValuePair("client", "www"));
        APIConnectionRequest.API_GetMessageList(callBackDone, params, Model.getInstance().getCurrentUser().getToken());

    }

    private void updateTitle() {
        if (Model.getInstance().getUserInfoCache() != null && this.convo != null) {
            //getNavigationBar().setTitle(String.valueOf(getConvo().getOtherUserId()));
            getNavigationBar().setTitle(Model.getInstance().getUserInfoCache().userWithId(this.convo.getOtherUserId()).getAlias());
        } else {
            getNavigationBar().setTitle(getActivity().getString(R.string.new_message));
        }
    }

    private void onConvoMessagesUpdated() {
        PersonalMessageList messageList = (PersonalMessageList) getConvoModel().getData();

        if (messageList != null) {
            ArrayList<PersonalMessage> messages = messageList.getPersonalMessages();
            setConversationMessages(messages);
        }
    }

    private void submitMessagePressed() {
        boolean messageValid = Helpers.stringIsNotNullAndMeetsMinLength(messageContent, 1);
        if (getConvo() == null)
            return;
        if (messageValid) {
            Helpers.hideSoftKeyboardForEditText(getChatField().getChatEditText());
            if (attachmentFile != null) {

                handleAttachmentUpload();
            } else {
                if (publicKey == null)
                    callAPIGetPublicKeyOfUser(String.valueOf(getConvo().getOtherUserId()), true);
                else
                    try {
                        sendMessage(toDeviceID, publicKeyAsString);
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
        } else {
            SimpleAlertDialog.showMessageWithOkButton(getActivity(), getResources().getString(R.string.error), getString(R.string.chat_view_please_enter_message), null);
        }
    }

    private Button getAddButton() {
        return (Button) rootView.findViewById(R.id.control_add);
    }

    private void sendMessage(String toDeviceId, String publicKeyOfUser) throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        if (!Helpers.stringIsNotNullAndMeetsMinLength(messageContent, 1)) {
            return;
        }
        if (publicKeyOfUser == null) {
            SimpleAlertDialog.showErrorWithOkButton(
                    getActivity(),
                    new ModelError(ModelError.SEND_MSG_ERROR),
                    null
            );
            return;
        }

        //if(getConvo().getConversationId()>0) {
        getSendingMessageDialog().show();
        // }

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

        refKey = Crypto.RSAEncrypt(secret, UserDefaults.getPublicKeyWithHiveAsString(Model.getInstance().getCurrentUser().getUserId(), "1"));
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
        if (getConvo().getConversationId() == 0) {
            //getConvoModel().addObserver(this, true);

            createNewMessage(newMessage);
        } else {

            if (newMessage.isEncrypted()) {
                if (encryptedContent != null) {

                    getConvoModel().sendMessage(newMessage);
                }

            } else {
                newMessage.isEncrypted = true;
                getConvoModel().sendMessage(newMessage);
            }
        }

    }


    private void sendMessage(final PersonalMessage newMessage) throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        if (!Helpers.stringIsNotNullAndMeetsMinLength(messageContent, 1)) {
            return;
        }
        //Log.v("Sendddssss" , attachment.getAttachmentKey());
        if (publicKeyAsString == null) {
            SimpleAlertDialog.showErrorWithOkButton(
                    getActivity(),
                    new ModelError(ModelError.SEND_MSG_ERROR),
                    null
            );
            return;
        }

        getSendingMessageDialog().show();
        if (newMessage.toUserId == 0) {
            newMessage.setToUserId(getConvo().getOtherUserId());
        }
        newMessage.isEncrypted = true;

        if (attachment != null && attachment.getAttachmentId() > 0) {
            newMessage.setAttachmentId(attachment.getAttachmentId());
        } else {
            newMessage.setAttachmentId(-1);
        }

        if (getConvoModel().getConversationId() == 0 || isNewConvesation) {
            createNewMessage(newMessage);
        } else {

            getConvoModel().sendMessage(newMessage);

        }

    }

    private void onMessageSent() {

        if (Helpers.stringIsNotNullAndMeetsMinLength(encryptionPassword, 1)) {
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

    private void clearMessage() {
        getChatField().getChatEditText().setText("");
        messageContent = null;
        attachment = null;
        setEncryptionPassword(null);
        setAttachmentFile(null);
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle savedAttachment = Model.getInstance().getShareBundle();
        observeConversationModel(true);

        if (savedAttachment != null) {
            Model.getInstance().setShareBundle(null); // Clear out to avoid looping.
            File attachFile = Helpers.fileFromShareBundle(getActivity(), savedAttachment);
            if (attachFile != null) {
                setAttachmentFile(attachFile);
            } else {
                SimpleAlertDialog.showMessageWithOkButton(getActivity(), getString(R.string.error), getString(R.string.attach_file_error), null);
            }
        }
        /*
        setupChatFieldHandling();

        setupListViewOnClickListener();

         ParceledInteger parceledInteger =getActivity(). getIntent().getParcelableExtra(Parcelator.PARCELATOR_KEY);
        setConvo((Conversation) Model.getInstance().getParcelator().getObjectForParcel(parceledInteger));
        callAPIGetPublicKeyOfUser(String.valueOf(getConvo().getOtherUserId()), false);
        **/

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void finish() {
        if (this.convo != null) {
            Model.getInstance().getUserInfoCache().removeObserverForUserId(this, this.convo.getOtherUserId());
        }

        Model.getInstance().getConversationList().setState(ModelState.STALE);

        //super.finish();
    }

    @Override
    public void encryptionPasswordSet(String encryptionPassword) {
        setEncryptionPassword(encryptionPassword);
    }

    @Override
    public void attachmentFileUpdated(File attachmentFile) {
        setAttachmentFile(attachmentFile);

        if (attachmentFile == null) {
            attachment = null;
        }
    }

    @Override
    public void onPause() {
        //timer.stop();
        //timer = null;

        //Model.getInstance().getConversationList().deleteObserver(this);
        //Model.getInstance().getConversationList().setState(ModelState.STALE);
        super.onPause();
        observeConversationModel(false);
    }

    @Override
    public void messageDecryptionComplete(boolean success) {
        if (success) {
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