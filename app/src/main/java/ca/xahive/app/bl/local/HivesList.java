package ca.xahive.app.bl.local;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import ca.xahive.app.bl.objects.BaseModelObject;
import ca.xahive.app.bl.objects.HiveListItem;
import ca.xahive.app.bl.objects.ModelObject;
import ca.xahive.app.bl.utils.ModelUtils;


public class HivesList extends BaseModelObject implements ModelObject {

    public ArrayList<HiveListItem> hive;

    public ArrayList<HiveListItem> getHiveListItems() {
        return hive;
    }

    public void setHiveListItems(ArrayList<HiveListItem> hiveListItems) {
        this.hive = hiveListItems;
    }

    @Override
    public String getKeyPath() {
        return "hive";
    }

    @Override
    public void setValuesWithJSON(Object jsonObject) {
        setHiveListItems(ModelUtils.arrayListFromJSONArray((JSONArray) jsonObject, HiveListItem.class));
        setClean(getHiveListItems() != null);
    }

    @Override
    public JSONObject getJSONRepresentation() {
        return null;
    }

    @Override
    public String toString() {
        return "Hives list: " + this.hive.toString();
    }
}
