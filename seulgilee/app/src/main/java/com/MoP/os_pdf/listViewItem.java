package com.MoP.os_pdf;

import android.graphics.drawable.Drawable;

public class listViewItem {
    private Drawable iconDrawable;
    private String titleStr;

    public void setIcon(Drawable icon) {
        iconDrawable = icon;
    }

    public void setTitle(String title) {
        titleStr = title;
    }

    public Drawable getIcon() {
        return this.iconDrawable;
    }

    public String getTitle() {
        return this.titleStr;
    }
}