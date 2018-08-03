package com.conlistech.sportsclubbookingengine.utils;

import android.app.Application;

public class CustomFontApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/OpenSans_Regular.ttf");
        FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/OpenSans_Regular.ttf");
        FontsOverride.setDefaultFont(this, "SERIF", "fonts/OpenSans_Regular.ttf");
        FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/OpenSans_Regular.ttf");
    }
}