package ca.xahive.app.bl.objects.api_object;

/**
 * Created by prosoft on 10/28/15.
 */
public class ChangeAliasHiveRequest {

    public String id;
    public String ownerId;
    public String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
