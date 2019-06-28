package liang.example.recyclerviewtest;

import java.util.Locale;

public class ExampleItem {
    private String string;
    private int anInt;
    private double aDouble;
    private boolean aBoolean;

    public ExampleItem(String string, int anInt, double aDouble, boolean aBoolean) {
        this.string = string;
        this.anInt = anInt;
        this.aDouble = aDouble;
        this.aBoolean = aBoolean;
    }

    public ExampleItem() {
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public int getAnInt() {
        return anInt;
    }

    public void setAnInt(int anInt) {
        this.anInt = anInt;
    }

    public double getaDouble() {
        return aDouble;
    }

    public void setaDouble(double aDouble) {
        this.aDouble = aDouble;
    }

    public boolean isaBoolean() {
        return aBoolean;
    }

    public void setaBoolean(boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

    @Override
    public String toString() {
        return String.format(Locale.CHINA, "<ExampleItem{string: %s, anInt: %d, aDouble: %f, aBoolean: %s}>", string, anInt, aDouble, aBoolean ? "true" : "false");
    }
}
