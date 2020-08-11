package com.shuishou.waitingdish.ui;

import android.view.View;

public class DoneDishClickListener implements View.OnClickListener {
    public static final String IMAGEBUTTON_TAG_KEY_ACTION = "DONE";

    private MainActivity mainActivity;
    public DoneDishClickListener(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    public void onClick(View view) {

    }
}
