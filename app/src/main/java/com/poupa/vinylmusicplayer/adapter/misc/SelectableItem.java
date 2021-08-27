package com.poupa.vinylmusicplayer.adapter.misc;


import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;


public class SelectableItem<U extends Serializable> implements Parcelable {
    public final U item;
    public boolean visible;

    public SelectableItem(U item, boolean visible) {
        this.item = item;
        this.visible = visible;
    }

    private SelectableItem(Parcel source) {
        item = (U) source.readSerializable();
        visible = source.readInt() == 1;
    }

    @Override
    public int describeContents() { return 0; }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(item);
        dest.writeInt(visible ? 1 : 0);
    }

    public static final Parcelable.Creator<SelectableItem> CREATOR = new Parcelable.Creator<SelectableItem>() {
        public SelectableItem createFromParcel(Parcel source) { return new SelectableItem(source); }

        public SelectableItem[] newArray(int size) { return new SelectableItem[size]; }
    };

}
