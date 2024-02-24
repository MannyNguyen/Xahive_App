package ca.xahive.app.bl.objects.api_object;

/**
 * Created by prosoft on 10/27/15.
 */
public class DeleteHiveRequest {
    public String id;
    public String ownerId;
    public String encryptedPassword;

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
