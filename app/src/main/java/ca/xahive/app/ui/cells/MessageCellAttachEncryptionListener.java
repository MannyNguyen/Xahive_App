package ca.xahive.app.ui.cells;


import ca.xahive.app.bl.objects.Message;

public interface MessageCellAttachEncryptionListener {
   public abstract void attachButtonPressed(Message message);
   public abstract void lockButtonPressed(Message message);
}
