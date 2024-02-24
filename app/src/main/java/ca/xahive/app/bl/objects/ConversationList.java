package ca.xahive.app.bl.objects;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import ca.xahive.app.bl.utils.ModelUtils;

public class ConversationList extends BaseModelObject  implements ModelObject {
    public ArrayList<Conversation> conversation;

    public ArrayList<Conversation> getConversations() {
        return conversation;
    }

    public void setConversations(ArrayList<Conversation> conversations) {
        this.conversation = conversations;
    }

    @Override
    public String getKeyPath() {
        return "conversation";
    }

    @Override
    public void setValuesWithJSON(Object jsonObject) {
        setConversations( ModelUtils.arrayListFromJSONArray((JSONArray) jsonObject, Conversation.class) );
        setClean(getConversations() != null);
    }

    @Override
    public JSONObject getJSONRepresentation() {
        return null;
    }

    @Override
    public String toString() {
        return "Conversation list: " + this.conversation.toString();
    }
}
