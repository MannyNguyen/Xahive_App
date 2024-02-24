package ca.xahive.app.bl.objects;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import ca.xahive.app.bl.utils.ModelUtils;


public class PersonalMessageList extends BaseModelObject {
    private ArrayList<PersonalMessage> message;

    public ArrayList<PersonalMessage> getPersonalMessages() {
        return message;
    }

    public void setPersonalMessages(ArrayList<PersonalMessage> personalMessages) {
        this.message = personalMessages;
    }


    @Override
    public String getKeyPath() {
        return "message";
    }

    @Override
    public void setValuesWithJSON(Object jsonObject) {
        setPersonalMessages(ModelUtils.arrayListFromJSONArray((JSONArray) jsonObject, PersonalMessage.class));
        setClean(getPersonalMessages() != null);
    }

    @Override
    public JSONObject getJSONRepresentation() {
        return null;
    }

    @Override
    public String toString() {
        return "Personal Message List: " + this.message.toString();
    }
}