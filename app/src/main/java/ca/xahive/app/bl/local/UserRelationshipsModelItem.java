package ca.xahive.app.bl.local;

import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;

import ca.xahive.app.bl.objects.ModelItem;
import ca.xahive.app.bl.objects.ModelObject;
import ca.xahive.app.bl.objects.UserRelationship;


public class UserRelationshipsModelItem extends ModelItem {
    private SparseArray<UserRelationship> relationships;
    private ArrayList<UserRelationship> blockedUsersList;
    private ArrayList<UserRelationship> contactsList;
    private ArrayList<UserRelationship> chatUsersList;

    public void setData(ModelObject data) {
        blockedUsersList = null;
        contactsList = null;
        chatUsersList = null;

        getRelationships().clear();

        ArrayList<UserRelationship> incomingRelationships = ((UserRelationshipList)data).getUserRelationships();

        for (UserRelationship aRelationship : incomingRelationships) {

            getRelationships().put(aRelationship.getOtherUserId(), aRelationship);
        }

        super.setData(data);

    }
    public void addContact(UserRelationship data) {
        UserRelationship userRelationship = (UserRelationship) data;
        getRelationships().put(userRelationship.getOtherUserId(),userRelationship);
       // super.setData((ModelObject)relationships);
    }

    private ArrayList<UserRelationship> getAllRelationships() {
        ArrayList<UserRelationship> relationships = ((UserRelationshipList)getData()).getUserRelationships();

        if (relationships == null) {
            relationships = new ArrayList<UserRelationship>();
        }

        return relationships;
    }

    private SparseArray<UserRelationship> getRelationships() {
        if (relationships == null) {
            relationships = new SparseArray<UserRelationship>();
        }
        return relationships;
    }

    public ArrayList<UserRelationship> getBlockedUsersList() {
       // if (blockedUsersList == null) {
            blockedUsersList = new ArrayList<UserRelationship>();

            for (UserRelationship aRelationship : getAllRelationships()) {
                if (aRelationship.isBlockedAtAll()) {
                    blockedUsersList.add(aRelationship);
                }
            }
        //}
        return blockedUsersList;
    }

    public ArrayList<UserRelationship> getContactsList() {

        contactsList = new ArrayList<UserRelationship>();
        if (contactsList == null) {


        }
        for (UserRelationship aRelationship : getAllRelationships()) {
            if (aRelationship.isContact()) {
                contactsList.add(aRelationship);
            }
        }
        return contactsList;
    }

    public ArrayList<UserRelationship> getChatUsersList() {
        if (chatUsersList == null) {
            chatUsersList = new ArrayList<UserRelationship>();

            for (UserRelationship aRelationship : getContactsList()) {
                if (!aRelationship.isBlockedMessages()) {
                    chatUsersList.add(aRelationship);
                }
            }
        }
        return chatUsersList;
    }

    public UserRelationship getRelationshipForUserId(int userId) {
        UserRelationship relationship = getRelationships().get(userId);
         if (relationship == null) {
            relationship = UserRelationship.relationshipWithUserId(userId);
            relationship.setUserId(Model.getInstance().getCurrentUser().getUserId()); // Pointless
        }

        return relationship;
    }
}
