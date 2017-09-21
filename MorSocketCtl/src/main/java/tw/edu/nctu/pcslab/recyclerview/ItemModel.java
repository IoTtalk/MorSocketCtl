package tw.edu.nctu.pcslab.recyclerview;

import android.animation.TimeInterpolator;

import java.util.ArrayList;

import tw.edu.nctu.pcslab.sectionlistview.ListCell;

public class ItemModel {
    public final String description;
    public final int colorId1;
    public final int colorId2;
    public final TimeInterpolator interpolator;
    public ArrayList<ListCell> deviceList;
    public ItemModel(String description, int colorId1, int colorId2,
                     TimeInterpolator interpolator, ArrayList<ListCell> deviceList) {
        this.description = description;
        this.colorId1 = colorId1;
        this.colorId2 = colorId2;
        this.interpolator = interpolator;
        this.deviceList = deviceList;
    }
}