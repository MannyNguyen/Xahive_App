package ca.xahive.app.bl.local;


import ca.xahive.app.bl.objects.HiveBasedSettings;
import ca.xahive.app.bl.objects.ModelError;
import ca.xahive.app.bl.objects.ModelItem;
import ca.xahive.app.bl.objects.ModelObject;
import ca.xahive.app.bl.objects.ModelState;

public class HiveSettingsModelItem extends ModelItem {
    public void setData(ModelObject data) {
        this.data = data;

        if (data == null) {
            setError(new ModelError(ModelError.INTERNAL_ERROR, null));
        }
        else {
            setState(ModelState.CURRENT);
        }
    }

    public HiveBasedSettings getHiveSettingsData() {
        HiveBasedSettings hiveSettings = (HiveBasedSettings)getData();
        return hiveSettings;
    }
}
