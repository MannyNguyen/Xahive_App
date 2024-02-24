package ca.xahive.app.bl.objects;

import org.json.JSONObject;

import java.util.Date;

import ca.xahive.app.ui.activities.myapp.R;
import ca.xahive.app.bl.utils.Helpers;
import ca.xahive.app.ui.activities.TabBarManagerActivity;

public class User extends BaseModelObject {
    private static final long CACHE_LIFETIME = 300;
    private int userId;
    private String email;
    private String alias;
    private int referrerId;
    private boolean isAnonymous;
    private Date populationDate;

    public User() {
        super();
    }

    public User(int userId) {
        this();
        this.userId = userId;
    }

    public Date getPopulationDate() {
        if (populationDate == null) {
            populationDate = new Date(0);
        }
        return populationDate;
    }

    public void setPopulationDate(Date populationDate) {
        this.populationDate = populationDate;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public boolean isOld() {
        Date currentTime = new Date();

        long ageInSeconds = (currentTime.getTime() - getPopulationDate().getTime()) / 1000L;

        return (ageInSeconds > CACHE_LIFETIME);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAlias() {
        if (this.isAnonymous()) {
            return String.format(TabBarManagerActivity.getContext().getResources().getString(R.string.anonymous_user_alias_format), this.userId);
        }
        else if (Helpers.stringIsNotNullAndMeetsMinLength(alias, 1)) {
            return String.format(TabBarManagerActivity.getContext().getResources().getString(R.string.regular_user_alias_format), this.alias, this.userId);
        }
        else {
            return String.format(TabBarManagerActivity.getContext().getResources().getString(R.string.unidentified_user_alias_format), this.userId);
        }
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getReferrerId() {
        return referrerId;
    }

    public void setReferrerId(int referrerId) {
        this.referrerId = referrerId;
    }

    @Override
    public void setValuesWithJSON(Object jsonObject) {
        JSONObject jsObj = (JSONObject) jsonObject;

        setUserId(jsObj.optInt("id", 0));
        setAlias(jsObj.optString("alias", null));
        setEmail(jsObj.optString("email", null));
        setReferrerId(jsObj.optInt("referrerId", 0));
        setAnonymous(jsObj.optBoolean("isAnonymous", false));

        setClean(getUserId() != 0);
        setPopulationDate(new Date());
    }

    @Override
    public JSONObject getJSONRepresentation() {
        return null;
    }

    @Override
    public String getKeyPath() {
        return "userInfo";
    }

    @Override
    public boolean equals(Object anObject){
        return (anObject != null && anObject.getClass().isInstance(this.getClass())
        && ((User)anObject).getUserId() == this.getUserId());
    }

    public int hash() {
        return this.getUserId();
    }
}
