package ca.xahive.app.bl.local;

import android.os.Parcel;
import android.os.Parcelable;

public class ParceledInteger implements Parcelable {
    private int integer;

    public ParceledInteger(int integer) {
        super();
        this.integer = integer;
    }

    public ParceledInteger(Parcel in) {
        super();
        this.integer = in.readInt();
    }

    public int getInteger() {
        return integer;
    }

    @Override
    public int describeContents() {
        return integer;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(integer);
    }

    public static final Creator CREATOR = new Creator() {
        public ParceledInteger createFromParcel(Parcel in) {
            return new ParceledInteger(in);
        }

        public ParceledInteger[] newArray(int size) {
            return new ParceledInteger[size];
        }
    };
}
