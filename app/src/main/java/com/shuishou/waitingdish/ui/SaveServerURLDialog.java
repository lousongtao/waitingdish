package com.shuishou.waitingdish.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.shuishou.waitingdish.InstantValue;
import com.shuishou.waitingdish.R;
import com.shuishou.waitingdish.io.IOOperator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/21.
 */

class SaveServerURLDialog {

    private EditText txtServerURL;
    private EditText txtQueryFrequency;
    private LoginActivity loginActivity;

    private AlertDialog dlg;

    public SaveServerURLDialog(@NonNull LoginActivity loginActivity) {
        this.loginActivity = loginActivity;
        initUI();
    }

    private void initUI(){
        View view = LayoutInflater.from(loginActivity).inflate(R.layout.config_serverurl_layout, null);

        txtServerURL = (EditText) view.findViewById(R.id.txtServerURL);
        txtQueryFrequency = (EditText) view.findViewById(R.id.txtQueryFrequency);

        loadServerURL();

        AlertDialog.Builder builder = new AlertDialog.Builder(loginActivity);
        builder.setTitle("Configure Server URL")
                .setIcon(R.drawable.info)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .setView(view);
        dlg = builder.create();
        dlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //add listener for YES button
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doSaveURL();
                    }
                });
            }
        });
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);
    }

    private void loadServerURL(){
        String url = IOOperator.loadServerURL(InstantValue.FILE_SERVERURL);
        if (url != null)
            txtServerURL.setText(url);
        Map<String, Object> config = IOOperator.loadConfigInfo(InstantValue.FILE_CONFIGINFO);
        if (config != null){
            if(config.get(InstantValue.CONFIGINFO_QUERYFREQUENCY) != null){
                txtQueryFrequency.setText(config.get(InstantValue.CONFIGINFO_QUERYFREQUENCY).toString());
            }
        }
    }

    private void doSaveURL(){
        final String url = txtServerURL.getText().toString();
        if (url == null || url.length() == 0){
            Toast.makeText(loginActivity, "Please input server URL.", Toast.LENGTH_LONG).show();
            return;
        }
        final String fre = txtQueryFrequency.getText().toString();
        int ifre = 0;
        try{
            ifre = Integer.parseInt(fre);
        } catch (NumberFormatException e){
            Toast.makeText(loginActivity, "Please input an available positive number as frequency.", Toast.LENGTH_LONG).show();
            return;
        }
        if (ifre <= 0){
            Toast.makeText(loginActivity, "Please input an available positive number as frequency.", Toast.LENGTH_LONG).show();
            return;
        }
        IOOperator.saveServerURL(InstantValue.FILE_SERVERURL, url);

        Map<String, Object> mapConfig = new HashMap<>();
        mapConfig.put(InstantValue.CONFIGINFO_QUERYFREQUENCY, fre);
        IOOperator.saveConfigInfo(InstantValue.FILE_CONFIGINFO, mapConfig);

        InstantValue.URL_TOMCAT = url;
        InstantValue.QUERY_FREQUENCY = ifre;
        dlg.dismiss();
    }

    public void showDialog(){
        dlg.show();
    }

    public void dismiss(){
        dlg.dismiss();
    }
}
