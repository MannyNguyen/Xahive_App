package ca.xahive.app.bl.objects;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.utils.ModelUtils;

public class Conversation extends BaseModelObject {
    public int conversationId;
    public int fromUserId;
    public int toUserId;
    public Date latestMessageDate;
   // public int unreadMessages;
    public String toDeviceId;
    public  String refKey;
    //public int  toHiveId;
   // public  String contentKey;
    public String content;
    //public  int personalMessageId;
    //public int messageId;
    //public Date date;
    public String date;
    public  boolean isEncrypted;
    //public int attachmentId;
    public String filename;
    //public long filesize;
    public boolean incoming;
   // public int unreadBlock;

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public int getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(int fromUserId) {
        this.fromUserId = fromUserId;
    }

    public int getToUserId() {
        return toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
    }

    public int getOtherUserId() {
        return (this.getToUserId() == Model.getInstance().getCurrentUser().getUserId())
                ? this.getFromUserId()
                : this.getToUserId();
    }

    public Date getLatestMessageDate() {
        return latestMessageDate;
    }

    public void setLatestMessageDate(Date latestMessageDate) {
        this.latestMessageDate = latestMessageDate;
    }

    @Override
    public String toString() {
        return this.getClass().toString() + ": " + conversationId;
    }

    @Override
    public void setValuesWithJSON(Object jsonObject) {

        JSONObject jsObj = (JSONObject)jsonObject;

        setConversationId(jsObj.optInt("conversationId", 0));
        setFromUserId(jsObj.optInt("fromUserId", 0));
        setToUserId(jsObj.optInt("toUserId", 0));
        setLatestMessageDate( ModelUtils.dateFromJSON(jsObj, "date") );

        setClean(getConversationId() != 0);
    }

    @Override
    public JSONObject getJSONRepresentation() {
        try {
            JSONObject jsObj = new JSONObject();

            jsObj.put("conversationId", getConversationId());
            jsObj.put("fromUserId", getFromUserId());
            jsObj.put("toUserId", getToUserId());
            jsObj.put("date", getLatestMessageDate());

            return jsObj;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public PersonalMessage messageWithReadableContent(String content){
        PersonalMessage message = new PersonalMessage();
        message.setToDeviceId(this.toDeviceId);
        message.setReadableContent(content);
        message.setConversationId(this.getConversationId());
        message.setToUserId(this.getOtherUserId());

        return message;
    }
   /**
    public ConversationRequest request() {
        ConversationRequest request = new ConversationRequest(this.getConversationId());
        return request;
    }**/
}
