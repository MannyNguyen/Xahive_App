package ca.xahive.app.bl.objects.api_object;

/**
 * Created by trantung on 10/26/15.
 */
public class MessageObjectRequest {
    public String content;
    public int toUserId;
    public boolean isEncrypted;
    public int attachmentId;
    public  int toHiveId;
    public  String toDeviceId;
    public  String fromDeviceId;
    public String contentKey ;
    public String refKey;
}
