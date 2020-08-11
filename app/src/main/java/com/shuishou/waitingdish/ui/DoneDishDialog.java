package com.shuishou.waitingdish.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.shuishou.waitingdish.InstantValue;
import com.shuishou.waitingdish.R;
import com.shuishou.waitingdish.bean.HttpResult;
import com.shuishou.waitingdish.bean.WaitingDish;
import com.shuishou.waitingdish.bean.WaitingIntentDetail;
import com.shuishou.waitingdish.http.HttpOperator;
import com.shuishou.waitingdish.utils.CommonTool;

import java.util.ArrayList;
import java.util.List;

public class DoneDishDialog implements View.OnClickListener {
    public static final int PROGRESSDLGHANDLER_MSGWHAT_SHOWPROGRESS = 1;
    public static final int PROGRESSDLGHANDLER_MSGWHAT_DISMISSDIALOG = 0;
    private final static int MESSAGEWHAT_SUCCESS=1;
    private final static int MESSAGEWHAT_ERRORTOAST=2;
    private final static int MESSAGEWHAT_ERRORDIALOG=3;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            dealHandlerMessage(msg);
            super.handleMessage(msg);
        }
    };

    private ProgressDialog progressDlg;
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
            }
        }
    };

    private HttpOperator httpOperator;
    private AlertDialog dlg;
    private MainActivity mainActivity;
    private WaitingDish waitingDish;
    private TableLayout deskLayout;
    private Button btnDoneDish;
    private Button btnClose;
    private List<DeskIcon> deskIcons = new ArrayList<>();

    public DoneDishDialog(MainActivity mainActivity, WaitingDish waitingDish){
        this.mainActivity = mainActivity;
        this.httpOperator = mainActivity.getHttpOperator();
        this.waitingDish = waitingDish;
        initUI();
    }

    private void initUI() {
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.donedish_layout, null);
        btnClose = (Button) view.findViewById(R.id.btn_closedlg);
        btnDoneDish = (Button) view.findViewById(R.id.btn_donedish);
        btnClose.setOnClickListener(this);
        btnDoneDish.setOnClickListener(this);
        deskLayout = (TableLayout) view.findViewById(R.id.layout_desks);
        initDeskData();
        buildDialog(view);
    }

    private void buildDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, AlertDialog.THEME_HOLO_LIGHT);
        builder.setView(view);
        dlg = builder.create();
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);
        Window window = dlg.getWindow();
        WindowManager.LayoutParams param = window.getAttributes();
        param.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        param.y = 0;
        window.setAttributes(param);
    }

    public void showDialog(){
        dlg.show();
    }

    private void initDeskData() {
        deskLayout.removeAllViews();
        int margin = 5;
        TableRow.LayoutParams trlp = new TableRow.LayoutParams();
        trlp.setMargins(margin, margin, 0, 0);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int rowAmount = 3; //show 4 desks in one row
        int deskIconLength = (int) Math.floor((displayMetrics.widthPixels - 200) / rowAmount);
        TableRow tr = null;
        for (int i = 0; i < waitingDish.getIndentDetails().size(); i++) {
            if (i % rowAmount == 0){
                tr = new TableRow(mainActivity);
                deskLayout.addView(tr);
            }
            WaitingIntentDetail detail = waitingDish.getIndentDetails().get(i);
            String title = detail.getDesk();
            if (detail.getAmount() > 1)
                title += "(" + detail.getAmount() + "份)";
            if (detail.getRequirement() != null && !detail.getRequirement().isEmpty())
                title += "(" + detail.getRequirement() + ")";
            DeskIcon di = new DeskIcon(mainActivity, title, detail.getDetailId());
            di.setWidth(deskIconLength);
            di.setHeight(150);
            deskIcons.add(di);
            tr.addView(di, trlp);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == btnDoneDish){
            doDoneDish();
        }else if (view == btnClose){
            dlg.cancel();
        }
    }

    private void doDoneDish() {
        final List<Integer> indentDetailIds = new ArrayList<>();
        for (DeskIcon di: deskIcons) {
            if (di.isSelected()){
                indentDetailIds.add(di.indentDetailId);
            }
        }
        if (indentDetailIds.isEmpty()){
            Toast.makeText(mainActivity, "No selected desks. Operate abort!", Toast.LENGTH_SHORT).show();
            return;
        }

        startProgressDialog("", "start posting data ... ");
        new Thread(){
            @Override
            public void run() {
                HttpResult<Integer> result = httpOperator.doneDish(indentDetailIds);
                progressDlgHandler.sendMessage(CommonTool.buildMessage(PROGRESSDLGHANDLER_MSGWHAT_DISMISSDIALOG, null));
                if (result.success){
                    handler.sendMessage(CommonTool.buildMessage(MESSAGEWHAT_SUCCESS, indentDetailIds));
                } else {
                    handler.sendMessage(CommonTool.buildMessage(MESSAGEWHAT_ERRORDIALOG,
                            "Something wrong happened while done dish! \n\nError message : " + result.result));
                }
            }
        }.start();
    }

    private void dealHandlerMessage(Message msg){
        switch (msg.what){
            case MESSAGEWHAT_ERRORTOAST :
                Toast.makeText(mainActivity, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                break;
            case MESSAGEWHAT_ERRORDIALOG:
                CommonTool.popupWarnDialog(mainActivity, R.drawable.error, "WRONG", msg.obj.toString());
                break;
            case MESSAGEWHAT_SUCCESS:
                dlg.dismiss();
                mainActivity.successDoneDish((List<Integer>)msg.obj);
                break;
        }
    }

    public void startProgressDialog(String title, String message){
        progressDlg = ProgressDialog.show(mainActivity, title, message);
        //启动progress dialog后, 同时启动一个线程来关闭该process dialog, 以防系统未正常结束, 导致此progress dialog长时间卡主. 设定时间为15秒(超过request的timeout时间)
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (progressDlg != null)
                    progressDlg.dismiss();
            }
        };
        Handler progressDlgCanceller = new Handler();
        progressDlgCanceller.postDelayed(r, 15000);
    }

    class DeskIcon extends android.support.v7.widget.AppCompatTextView implements View.OnClickListener {
        private String deskName;
        private int indentDetailId;
        private boolean isSelected = true;
        private int UNSELECT = Color.LTGRAY;
        private int SELECT = Color.GREEN;
        public DeskIcon(Context context, String deskName, int indentDetailId){
            super(context);
            this.deskName = deskName;
            this.indentDetailId = indentDetailId;
            initDeskUI();
        }

        private void initDeskUI(){
            setTextSize(45);
            setTextColor(Color.BLACK);
            setBackgroundColor(SELECT);
            setGravity(Gravity.CENTER);
            setText(deskName);
            setOnClickListener(this);
            setEllipsize(TextUtils.TruncateAt.END);
        }

        public void setSelected(boolean b){
            isSelected = b;
            if (b){
                this.setBackgroundColor(SELECT);
            } else {
                this.setBackgroundColor(UNSELECT);
            }
        }

        public boolean isSelected(){
            return isSelected;
        }

        public String getDeskName() {
            return deskName;
        }

        @Override
        public void onClick(View view) {
            setSelected(!isSelected);
        }
    }
}
