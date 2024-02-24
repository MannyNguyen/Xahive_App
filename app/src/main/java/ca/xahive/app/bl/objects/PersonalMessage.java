package ca.xahive.app.bl.objects;

import org.json.JSONException;
import org.json.JSONObject;

import ca.xahive.app.bl.local.Model;
import ca.xahive.app.bl.utils.ModelUtils;

public class PersonalMessage extends Message {
    public int conversationId;
    public int personalMessageId;
    //public int toUserId;
    public boolean incoming;
    public int getConversationId() { return conversationId; }

    public void setConversationId(int conversationId) { this.conversationId = conversationId; }

    public int getPersonalMessageId() {
        return personalMessageId;
    }

    public void setPersonalMessageId(int personalMessageId) {
        this.personalMessageId = personalMessageId;
    }

    public int getToUserId() {
        return  toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public void setIncoming(boolean incoming) {
        this.incoming = incoming;
    }

    public int getOtherUserId() {
        int currUserId = Model.getInstance().getCurrentUser().getUserId();

        if (getFromUserId() == currUserId) {
            return getToUserId();
        }
        else if (getToUserId() == currUserId) {
            return getFromUserId();
        }
        else {
            return 0;
        }
    }

    @Override
    public void setValuesWithJSON(Object jsonObject) {
        JSONObject jsObj = (JSONObject)jsonObject;

        setConversationId( jsObj.optInt("conversationId"));
        setPersonalMessageId( jsObj.optInt("personalMessageId", 0) );
        setMessageId( jsObj.optInt("messageId", 0) );
        setFromUserId( jsObj.optInt("fromUserId", 0) );
        setToUserId( jsObj.optInt("toUserId", 0) );
        setDate( ModelUtils.dateFromJSON(jsObj, "date") );
        setContent( jsObj.optString("content", null) );
        setEncrypted( jsObj.optBoolean("isEncrypted", false) );
        setIncoming( jsObj.optBoolean("incoming", false) );
        setAttachmentId(jsObj.optInt("attachmentId", 0));
        setFilename(jsObj.optString("filename", null));
        setFilesize(jsObj.optDouble("filesize", 0.0));

        setClean(getMessageId() != 0);

    }
   public String  getToDeviceId()
   { if(toDeviceId!=null)
        return toDeviceId;
       else
         return  fromDeviceId;
   }
    @Override
    public JSONObject getJSONRepresentation() {

        try {
            JSONObject jsObj = new JSONObject();

            jsObj.put("conversationId", getConversationId());
            jsObj.put("personalMessageId", getPersonalMessageId());
            jsObj.put("messageId", getMessageId());
            jsObj.put("fromUserId", getFromUserId());
            jsObj.put("toUserId", getToUserId());
            jsObj.put("date", getDate());
            jsObj.put("content", getContent());
            jsObj.put("isEncrypted", isEncrypted());
            jsObj.put("incoming", isIncoming());
            jsObj.put("attachmentId", getAttachmentId());
            jsObj.put("filename", getFilename());
            jsObj.put("filesize", getFilesize());

            return jsObj;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getKeyPath() {
        return "message";
    }

    @Override
    public String toString() {
        return "Message:" + ": " + this.getDate().toString() + " content: " + this.getContent();
    }

    public PersonalMessage newMessageForConvoWithContent (String content) {
        PersonalMessage newMsg = new PersonalMessage();
        newMsg.setContent(content);
        newMsg.setConversationId(getConversationId());
        newMsg.setToUserId(getOtherUserId());
        return newMsg;
    }

    // Helpers
    /**
    public ConversationRequest conversationRequestForConversationListItem() {
        ConversationRequest request = new ConversationRequest(getConversationId());
        return request;
    }**/
}