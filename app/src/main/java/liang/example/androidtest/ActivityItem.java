package liang.example.androidtest;

import android.os.Parcel;
import android.os.Parcelable;

public class ActivityItem implements Parcelable {

    private String name;
    private String desc;
    private String author;
    private String created;
    private String updated;
    private Class<?> clazz;

    public ActivityItem() {
    }

    public ActivityItem(Parcel source) {
        this(source.readString(), source.readString(), source.readString(), source.readString(), source.readString(), (Class) source.readSerializable());
    }

    public ActivityItem(String name, String desc, String author, String created, String updated, Class<?> clazz) {
        this.name = name;
        this.desc = desc;
        this.author = author;
        this.created = created;
        this.updated = updated;
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.desc);
        dest.writeString(this.author);
        dest.writeString(this.created);
        dest.writeString(this.updated);
        dest.writeSerializable(this.clazz);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator<ActivityItem>() {
        @Override
        public ActivityItem createFromParcel(Parcel source) {
            return new ActivityItem(source);
        }

        @Override
        public ActivityItem[] newArray(int size) {
            return new ActivityItem[size];
        }
    };
}
