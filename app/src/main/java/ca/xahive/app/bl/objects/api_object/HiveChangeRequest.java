package ca.xahive.app.bl.objects.api_object;

/**
 * Created by trantung on 10/24/15.
 */
public class HiveChangeRequest {
    private String deviceId;
    public void setDeviceId(String deviceId)
    {
      this.deviceId = deviceId;
    }
    public void setHiveId(String hiveId)
    {
       this. hiveId  = hiveId;
    }
    public void setUserId(String userId)
    {
        this.userId =  userId;
    }
    public String getDeviceId()
    {
        return  deviceId;
    }
    public String getHiveId()
    {
        return  hiveId;
    }
    public String getUserId()
    {
        return  userId;
    }
    private  String hiveId;
    private String userId;
}
