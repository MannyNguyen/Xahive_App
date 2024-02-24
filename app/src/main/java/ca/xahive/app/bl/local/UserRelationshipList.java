package ca.xahive.app.bl.local;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import ca.xahive.app.bl.objects.BaseModelObject;
import ca.xahive.app.bl.objects.ModelObject;
import ca.xahive.app.bl.objects.UserRelationship;
import ca.xahive.app.bl.utils.ModelUtils;


public class UserRelationshipList extends BaseModelObject implements ModelObject {
    private ArrayList<UserRelationship> userRelationships;

    public ArrayList<UserRelationship> getUserRelationships() {
        if (userRelationships == null) {
            userRelationships = new ArrayList<UserRelationship>();
        }
        return userRelationships;
    }

    public void setUserRelationships(ArrayList<UserRelationship> userRelationships) {
        this.userRelationships = userRelationships;
    }

    @Override
    public String getKeyPath() {
        return "relatedUser";
    }

    @Override
    public void setValuesWithJSON(Object jsonObject) {
        setUserRelationships(ModelUtils.arrayListFromJSONArray((JSONArray) jsonObject, UserRelationship.class));
        setClean(getUserRelationships() != null);
    }

    @Override
    public JSONObject getJSONRepresentation() {
        return null;
    }

    @Override
    public String toString() {
        return "User relationships list: " + this.userRelationships.toString();
    }
}
