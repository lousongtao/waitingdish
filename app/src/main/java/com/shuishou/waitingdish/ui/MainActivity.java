package com.shuishou.waitingdish.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.shuishou.waitingdish.InstantValue;
import com.shuishou.waitingdish.R;
import com.shuishou.waitingdish.bean.UserData;
import com.shuishou.waitingdish.bean.WaitingDish;
import com.shuishou.waitingdish.bean.WaitingIntentDetail;
import com.shuishou.waitingdish.http.HttpOperator;
import com.shuishou.waitingdish.io.IOOperator;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NoHttp;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MainActivity.class.getSimpleName());
    private String TAG_UPLOADERRORLOG = "uploaderrorlog";
    private String TAG_EXITSYSTEM = "exitsystem";
    private String TAG_UPGRADEAPP = "upgradeapp";
    private HttpOperator httpOperator;
    private ArrayList<WaitingDish> waitingDishList = new ArrayList<>();
    private UserData loginUser;

    private RecyclerWaitingDishAdapter recyclerWaitingDishAdapter;

    public static final int PROGRESSDLGHANDLER_MSGWHAT_STARTLOADDATA = 3;
    public static final int PROGRESSDLGHANDLER_MSGWHAT_DOWNFINISH = 2;
    public static final int PROGRESSDLGHANDLER_MSGWHAT_SHOWPROGRESS = 1;
    public static final int PROGRESSDLGHANDLER_MSGWHAT_DISMISSDIALOG = 0;
    private ProgressDialog progressDlg;

    private QueryTimer queryTimer;

    public Handler getToastHandler(){
        return toastHandler;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginUser = (UserData)getIntent().getExtras().getSerializable(LoginActivity.INTENTEXTRA_LOGINUSER);
        setContentView(R.layout.activity_main);

        TextView tvUploadErrorLog = (TextView)findViewById(R.id.drawermenu_uploaderrorlog);
        TextView tvExit = (TextView)findViewById(R.id.drawermenu_exit);
        TextView tvUpgradeAPP = (TextView) findViewById(R.id.drawermenu_upgradeapp);

        RecyclerView lvWaitingDish = (RecyclerView) findViewById(R.id.list_waiting_dish);
        lvWaitingDish.setLayoutManager(new LinearLayoutManager(this));
        recyclerWaitingDishAdapter = new RecyclerWaitingDishAdapter(this, R.layout.waiting_dish_cell, waitingDishList);
        lvWaitingDish.setAdapter(recyclerWaitingDishAdapter);




        tvUploadErrorLog.setTag(TAG_UPLOADERRORLOG);
        tvExit.setTag(TAG_EXITSYSTEM);
        tvUpgradeAPP.setTag(TAG_UPGRADEAPP);
        tvUploadErrorLog.setOnClickListener(this);
        tvExit.setOnClickListener(this);
        tvUpgradeAPP.setOnClickListener(this);

        //init tool class, NoHttp
        NoHttp.initialize(this);
        Logger.setDebug(true);
        Logger.setTag("waitingdish:nohttp");

        InstantValue.URL_TOMCAT = IOOperator.loadServerURL(InstantValue.FILE_SERVERURL);
        httpOperator = new HttpOperator(this);

        queryTimer = new QueryTimer(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        queryTimer.cancel();
    }

    public void setWaitingDish(ArrayList<WaitingDish> wds) {
        waitingDishList.clear();//cannot use "waitingDishList = wds", I don't know real ready, but it does not work if so
        if (wds != null)
            waitingDishList.addAll(wds);
        recyclerWaitingDishAdapter.notifyDataSetChanged();
    }

    /**
     * 上菜成功后, 清除dish对应的桌号, 如果这道菜已经全部上桌, 清除这个记录.
     * @param indentDetailIds
     */
    public void successDoneDish(List<Integer> indentDetailIds) {
        for (int wdi = waitingDishList.size() - 1; wdi >= 0; wdi--) {
            WaitingDish wd = waitingDishList.get(wdi);
            for (int i = wd.getIndentDetails().size() - 1; i>= 0; i--) {
                WaitingIntentDetail detail = wd.getIndentDetails().get(i);
                if (indentDetailIds.contains(detail.getDetailId())){
                    wd.getIndentDetails().remove(i);
                    if (wd.getIndentDetails().isEmpty()){
                        waitingDishList.remove(wdi);
                    }
                }
            }
        }
        recyclerWaitingDishAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (TAG_UPLOADERRORLOG.equals(v.getTag())){
            IOOperator.onUploadErrorLog(this);
        } else if (TAG_EXITSYSTEM.equals(v.getTag())){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm")
                    .setIcon(R.drawable.info)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.finish();
                        }
                    })
                    .setNegativeButton("No", null);
            builder.create().show();
        } else if (TAG_UPGRADEAPP.equals(v.getTag())){
            UpgradeAppDialog dlg = new UpgradeAppDialog(this);
            dlg.showDialog();
        }
    }

    public void startProgressDialog(String title, String message){
        progressDlg = ProgressDialog.show(this, title, message);
    }

    public HttpOperator getHttpOperator(){
        return httpOperator;
    }

    public Handler getProgressDlgHandler(){
        return progressDlgHandler;
    }

    private Handler progressDlgHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == PROGRESSDLGHANDLER_MSGWHAT_DISMISSDIALOG) {
                if (progressDlg != null)
                    progressDlg.dismiss();
            } else if (msg.what == PROGRESSDLGHANDLER_MSGWHAT_SHOWPROGRESS){
                if (progressDlg != null){
                    progressDlg.setMessage(msg.obj != null ? msg.obj.toString() : InstantValue.NULLSTRING);
                }
            } else if (msg.what == PROGRESSDLGHANDLER_MSGWHAT_DOWNFINISH){
                if (progressDlg != null){
                    progressDlg.setMessage(msg.obj != null ? msg.obj.toString() : InstantValue.NULLSTRING);
                }
            } else if (msg.what == PROGRESSDLGHANDLER_MSGWHAT_STARTLOADDATA){
                if (progressDlg != null){
                    progressDlg.setMessage(msg.obj != null ? msg.obj.toString() : InstantValue.NULLSTRING);
                }
            }
        }
    };
    public static final int TOASTHANDLERWHAT_ERRORMESSAGE = 0;
    private Handler toastHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TOASTHANDLERWHAT_ERRORMESSAGE){
                Toast.makeText(MainActivity.this,msg.obj != null ? msg.obj.toString() : InstantValue.NULLSTRING, Toast.LENGTH_LONG).show();
            }
        }
    };


}
