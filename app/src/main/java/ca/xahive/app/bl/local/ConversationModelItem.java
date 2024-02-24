package ca.xahive.app.bl.local;

import android.util.Log;

import com.amazonaws.com.google.gson.GsonBuilder;
import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ca.xahive.app.bl.api.queries.APIConnectionRequest;
import ca.xahive.app.bl.objects.Conversation;
import ca.xahive.app.bl.objects.ConversationList;
import ca.xahive.app.bl.objects.ModelError;
import ca.xahive.app.bl.objects.ModelEvent;
import ca.xahive.app.bl.objects.ModelItem;
import ca.xahive.app.bl.objects.ModelObject;
import ca.xahive.app.bl.objects.ModelState;
import ca.xahive.app.bl.objects.PersonalMessage;
import ca.xahive.app.bl.objects.PersonalMessageList;
import ca.xahive.app.bl.objects.api_object.MessageObjectRequest;
import ca.xahive.app.bl.objects.api_object.MessagePostRequest;
import ca.xahive.app.bl.objects.api_object.RelationshipResponse;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.bl.utils.XADebug;
import ca.xahive.app.webservice.CallBackDone;


public class ConversationModelItem extends ModelItem {
    private Conversation conversation;
    //private ConversationRequest conversationGrabRequestForNewer;
    //private ConversationRequest conversationPostRequest;
    private Date oldestKnownDate;
    private Date newestKnownDate;
    private PersonalMessage sentMessage;

    public PersonalMessage getSentMessage() {
        return this.sentMessage;
    }

    public void setSentMessage(PersonalMessage sentMessage) {
        this.sentMessage = sentMessage;
    }

    private void getConversationGrabRequestForNewer() {
        //if (conversationGrabRequestForNewer == null) {
            callAPIRequestForNewer();

           /** conversationGrabRequestForNewer = new ConversationRequest(getConversationId());
            conversationGrabRequestForNewer.setOlderThanDate(null);
            conversationGrabRequestForNewer.setCallback(new APICallback() {
                @Override
                public void onStart() {
                    super.onStart();
                    setPendingIfError();
                }

                @Override
                public void onSuccess(ModelObject modelObject) {
                    super.onSuccess(modelObject);
                    processConversationBatch((PersonalMessageList) modelObject);
                }

                @Override
                public void onFail(ModelError error) {
                    super.onFail(error);
                    setError(error);
                }
            });
        }

        conversationGrabRequestForNewer.setConversationId(getConversationId());
        conversationGrabRequestForNewer.setNewerThanDate(getNewestKnownDate());

        return conversationGrabRequestForNewer;**/
       // }
    }
     /*8
    private ConversationRequest getConversationPostRequest() {
        if (conversationPostRequest == null) {
            conversationPostRequest = new ConversationRequest(getConversationId());
            conversationPostRequest.setCallback(new APICallback() {
                @Override
                public void onStart() {
                    super.onStart();
                    setPendingIfError();
                    sentMessage = null;
                }

                @Override
                public void onSuccess(ModelObject modelObject) {
                    super.onSuccess(modelObject);

                    PersonalMessageList returnedMessageList = (PersonalMessageList)modelObject;
                    sentMessage = returnedMessageList.getPersonalMessages().get(0);

                    if (getConversationId() == 0) {
                        getConversation().setConversationId(sentMessage.getConversationId());
                    }

                    loadNewerConversations();
                }

                @Override
                public void onFail(ModelError error) {
                    super.onFail(error);
                    setError(error);
                }
            });
        }

        conversationPostRequest.setConversationId(getConversationId());

        return conversationPostRequest;
    } **/

       private void callAPIgetConversationPostRequest(PersonalMessage message)
       {
           CallBackDone callBackDone = new CallBackDone();
           callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
               @Override
               public void setMyTaskComplete(JSONObject result) {
                   if (result != null) {
                       Gson gson = new com.google.gson.GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
                       PersonalMessageList returnedMessageList = gson.fromJson(result.toString(), PersonalMessageList.class);

                       sentMessage = returnedMessageList.getPersonalMessages().get(0);
                       Log.v("sentMessage122",String.valueOf(sentMessage.getConversationId()));

                       notifyObserversOfEvent(ModelEvent.CREATE_HIVE);

                        /*8
                       if (getConversationId() == 0) {

                           if(sentMessage.getConversationId()!=0) {
                               conversation = new Conversation();
                               conversation.setConversationId(sentMessage.getConversationId());
                               conversation.setToUserId(sentMessage);
                               setConversationId(sentMessage.getConversationId());
                               //setConversation(sentMessage);
                               //getConversation().setConversationId(sentMessage.getConversationId());
                           }
                           else {
                                if(returnedMessageList.getPersonalMessages().size()>0) {
                                    getConversation().setConversationId(returnedMessageList.getPersonalMessages().get(0).conversationId);
                                }

                           }
                       }**/

                       callAPIRequestForNewer();
                   }
               }

               @Override
               public void onStart() {
                   //getUserRelationships().setPendingIfError();
               }

               @Override
               public void onSuccess(ModelObject modelObject) {

                   loadNewerConversations();
                   //getUserRelationships().setData(modelObject);
                   // grabInitialHiveSettings();

               }

               @Override
               public void onFail(ModelError error) {
                   setError(error);
                   //getUserRelationships().setError(error);
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
           messageObjectRequest.isEncrypted = true ;
           messageObjectRequest.toUserId = message.toUserId;
           messageObjectRequest.toHiveId = 1;
           messageObjectRequest.toDeviceId = message.toDeviceId;

           messageObjectRequest.refKey = message.refKey;
           messagePostRequest .message = messageObjectRequest;
          Gson gson = new Gson();
           APIConnectionRequest.API_PostConversationItem(callBackDone, getURLString(), messagePostRequest, Model.getInstance().getCurrentUser().getToken());
         // Log.v("API_PostConversationItem", getURLString());

       }
       public void clearListMessage()
       {

       }
       private void processConversationBatch(PersonalMessageList batch) {
        ArrayList<PersonalMessage> newMessages = batch.getPersonalMessages();
        ArrayList<PersonalMessage> existingMessages = null;

        if (getData() != null) {
            existingMessages = ((PersonalMessageList)getData()).getPersonalMessages();
        }

        if (existingMessages == null) {
            existingMessages = new ArrayList<PersonalMessage>();
        }

        if (newMessages.size() == 0 && getState() == ModelState.CURRENT) {
            return; // Nothing to update.
        }
           boolean isADD = true;
        for(int i= existingMessages.size()-1;i>=0;i--)
        {
            if(existingMessages.get(i).getContent().equals(newMessages.get(0).getContent()))
            {
                isADD = false;
            }
            if(!isADD)
                break;
        }
           if(isADD) {
               for (PersonalMessage aMessage : newMessages) {
                   existingMessages.add(aMessage);
               }
           }

        Collections.sort(existingMessages, new Comparator<PersonalMessage>() {
            @Override
            public int compare(PersonalMessage personalMessage, PersonalMessage personalMessage2) {
                return personalMessage.getDate().compareTo(personalMessage2.getDate());
            }
        });

        if (existingMessages.size() > 0) {
            setOldestKnownDate(existingMessages.get(0).getDate());
            setNewestKnownDate(existingMessages.get(existingMessages.size() - 1).getDate());
        }

        PersonalMessageList personalMessageList = new PersonalMessageList();
        personalMessageList.setPersonalMessages(existingMessages);
        personalMessageList.setClean(true);

        setData(personalMessageList);
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public int getConversationId() {
        return (conversation != null) ? conversation.getConversationId() : 0;
    }

    private Date getOldestKnownDate() {
        if (oldestKnownDate == null) {
            oldestKnownDate = new Date(Helpers.FOREVER_ALREADY);
        }
        return oldestKnownDate;
    }

    private void setOldestKnownDate(Date oldestKnownDate) {
        this.oldestKnownDate = oldestKnownDate;
    }

    private Date getNewestKnownDate() {
        if (newestKnownDate == null) {
            newestKnownDate = new Date(Helpers.AGES_AGO);
        }
        return newestKnownDate;
    }

    private void setNewestKnownDate(Date newestKnownDate) {
        this.newestKnownDate = newestKnownDate;
    }

    public void loadNewerConversations() {
        this.getConversationGrabRequestForNewer();
        //this.getConversationGrabRequestForNewer().grab();
    }
    public void callAPIRequestForNewer()
    {
        CallBackDone callBackDone = new CallBackDone();
        callBackDone.setMyTaskCompleteListener(new CallBackDone.OnTaskComplete() {
            @Override
            public void setMyTaskComplete(JSONObject result) {
                if (result != null) {

                    Gson gson = new com.google.gson.GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
                     PersonalMessageList response = gson.fromJson(result.toString(), PersonalMessageList.class);

                    processConversationBatch(response);
                    Log.v("apiiii", result.toString());
                    notifyObserversOfEvent(ModelEvent.CREATE_HIVE);
                }
            }

            @Override
            public void onStart() {

                //getUserRelationships().setPendingIfError();
            }

            @Override
            public void onSuccess(ModelObject modelObject) {
                processConversationBatch((PersonalMessageList) modelObject);
            }

            @Override
            public void onFail(ModelError error) {
                Log.v("testlog", "error" + error.getCode());
                setError(error);
            }

            @Override
            public void onComplete() {

            }
        });
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token", Model.getInstance().getCurrentUser().getToken()));
        params.add(new BasicNameValuePair("client", "www"));
        APIConnectionRequest.API_GetConversationItem(callBackDone, getURLString(), params);

        setConversationId(getConversationId());
        setNewerThanDate(getNewestKnownDate());

    }
    public String getURLString() {
        long newerThanDateSecondsSinceUnixEpoch = Helpers.timestampFromDate(getNewerThanDate());
        long olderThanDateSecondsSinceUnixEpoch = Helpers.timestampFromDate(getOlderThanDate());
         /**
        Log.v("privateConversation222", String.format("privateConversation/%d/%d/%d",
                        getConversationId(),
                        newerThanDateSecondsSinceUnixEpoch,
                        olderThanDateSecondsSinceUnixEpoch)
        );**/
        Calendar lastSeenDate = Calendar.getInstance();
        Date currentDate =    lastSeenDate.getTime();
        setNewestKnownDate(currentDate);
        // updateLastSeenConversationDate();
        if(getConversationId()!=0) {

            return String.format("privateConversation/%d/%d/%d",
                    getConversationId(),
                    newerThanDateSecondsSinceUnixEpoch,
                    olderThanDateSecondsSinceUnixEpoch);
        }
        else if(sentMessage!=null){

            return String.format("privateConversation/%d/%d/%d",
                    sentMessage.getConversationId(),
                    newerThanDateSecondsSinceUnixEpoch,
                    olderThanDateSecondsSinceUnixEpoch);
        }
        else {
            return String.format("privateConversation/%d/%d/%d",
                    getConversationId(),
                    newerThanDateSecondsSinceUnixEpoch,
                    olderThanDateSecondsSinceUnixEpoch);

        }
    }

    public void notifyObserversOfEvent(ModelEvent evt) {
        setChanged();
        XADebug.d("Model notifying observers: " + evt);
        notifyObservers(evt);
    }

    public void updateLastSeenConversationDate() {
        Date lastSeenDate = new Date();


        //if (conversation > 0) {
            lastSeenDate =conversation.getLatestMessageDate();
            if (newerThanDate != null) {
                Long timestampFromDate = Helpers.timestampFromDate(newerThanDate);

                if (timestampFromDate > 86400L) {
                    timestampFromDate = timestampFromDate - 86400L;
                }

                lastSeenDate.setTime(timestampFromDate);
            }


        setLastSeenConversationDate(lastSeenDate);
    }
    public Date getLastSeenConversationDate() {
        if (newerThanDate != null) {
            return newerThanDate;
        } else {
            newerThanDate = new Date();
            Long oneDayAgo = Helpers.timestampFromDate(newerThanDate) - 86400000;
            newerThanDate.setTime(oneDayAgo);
            return newerThanDate;
        }
    }

    public void setLastSeenConversationDate(Date lastSeenConversationDate) {
        this.newerThanDate = lastSeenConversationDate;
    }
    private int conversationId;
    private Date olderThanDate;
    private Date newerThanDate;

    public Date getOlderThanDate() {
        if (olderThanDate != null) {
            return olderThanDate;
        }
        else {
            Date distantFutureDate = new Date(Helpers.FOREVER_ALREADY);
            return distantFutureDate;
        }
    }

    public void setOlderThanDate(Date olderThanDate) {
        this.olderThanDate = olderThanDate;
    }

    public Date getNewerThanDate() {
        if (newerThanDate != null) {
            return newerThanDate;
        }
        else {
            Date distantPastDate = new Date(Helpers.AGES_AGO);
            return distantPastDate;
        }
    }

    public void setNewerThanDate(Date newerThanDate) {
        this.newerThanDate = newerThanDate;
    }


    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public void sendMessage(PersonalMessage message) {
        this.callAPIgetConversationPostRequest(message);
        //this.getConversationPostRequest().post(message);
    }
}
