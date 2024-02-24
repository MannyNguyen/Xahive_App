package ca.xahive.app.bl.objects;

import org.json.JSONException;
import org.json.JSONObject;

public class UserRelationship extends BaseModelObject {
    public int userId;
    public int otherUserId;
    public boolean isContact;
    public boolean isBlockedBuzz;
    public boolean isBlockedMessages;
    public  String otherUserAlias;
    public  String otherUserAvatarUrl;


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(int otherUserId) {
        this.otherUserId = otherUserId;
    }

    public boolean isContact() {
        return isContact;
    }

    public void setContact(boolean isContact) {
        this.isContact = isContact;
    }

    public boolean isBlockedBuzz() {
        return isBlockedBuzz;
    }

    public void setBlockedBuzz(boolean isBlockedBuzz) {
        this.isBlockedBuzz = isBlockedBuzz;
    }

    public boolean isBlockedMessages() {
        return isBlockedMessages;
    }

    public void setBlockedMessages(boolean isBlockedMessages) {
        this.isBlockedMessages = isBlockedMessages;
    }

    @Override
    public String getKeyPath() {
        return "relatedUser";
    }

    @Override
    public void setValuesWithJSON(Object jsonObject) {
        JSONObject jsObj = (JSONObject) jsonObject;

        setUserId(jsObj.optInt("userId", 0));
        setOtherUserId(jsObj.optInt("otherUserId", 0));
        setContact(jsObj.optBoolean("isContact", false));
        setBlockedBuzz(jsObj.optBoolean("isBlockedBuzz", false));
        setBlockedMessages(jsObj.optBoolean("isBlockedMessages", false));

        setClean(getOtherUserId() != 0);
    }

    @Override
    public String toString() {
        return String.format("Relationship with %d: isContact: %d, isBlockedBuzz: %d, isBlockedMessages: %d",
                this.otherUserId, this.isContact?1:0, this.isBlockedBuzz?1:0, this.isBlockedMessages?1:0);
    }

    @Override
    public JSONObject getJSONRepresentation() {
        try {
            JSONObject jsObj = new JSONObject();
            jsObj.put("userId", getUserId());
            jsObj.put("otherUserId", getOtherUserId());
            jsObj.put("isContact", isContact());
            jsObj.put("isBlockedBuzz", isBlockedBuzz());
            jsObj.put("isBlockedMessages", isBlockedMessages());

            return jsObj;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    // helpers

    public boolean isBlockedAtAll() {
        return (isBlockedMessages() || isBlockedBuzz());
    }

    public static UserRelationship relationshipWithUserId(int userId) {
        UserRelationship relationship = new UserRelationship();
        relationship.setOtherUserId(userId);
        return relationship;
    }
}
