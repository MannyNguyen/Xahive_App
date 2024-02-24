package ca.xahive.app.bl.local;

import android.util.SparseArray;

import ca.xahive.app.bl.utils.XADebug;

public class Parcelator {
    public static final String PARCELATOR_KEY = "Parcelator";

    private SparseArray<Object> parcelMap;
    private int localInteger;

    private SparseArray<Object> getParcelMap() {
        if (parcelMap == null) {
            parcelMap = new SparseArray<Object>();
        }
        return parcelMap;
    }

    private int getLocalInteger() {
        return ++localInteger;
    }

    public ParceledInteger createParcelForObject(Object object) {
        ParceledInteger parceledInteger = new ParceledInteger(getLocalInteger());
        int newInteger = parceledInteger.getInteger();
        getParcelMap().put(newInteger, object);


        return parceledInteger;
    }

    public Object getObjectForParcel(ParceledInteger parceledInteger) {
        int integer = parceledInteger.getInteger();
        Object result = getParcelMap().get(integer);

        if (result != null) {
            getParcelMap().remove(parceledInteger.getInteger());
        }
        else {
            XADebug.d(String.format("Asked for a parcel that isn't there: %d", integer));
        }

        return result;
    }
}
