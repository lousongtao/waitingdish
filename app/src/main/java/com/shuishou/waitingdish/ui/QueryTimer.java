package com.shuishou.waitingdish.ui;

import android.os.Handler;
import android.os.Message;

import com.shuishou.waitingdish.InstantValue;
import com.shuishou.waitingdish.bean.WaitingDish;
import com.shuishou.waitingdish.http.HttpOperator;
import com.shuishou.waitingdish.utils.CommonTool;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class QueryTimer extends Timer {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(QueryTimer.class.getSimpleName());

    private Handler refreshHandler;
    private MainActivity mainActivity;

    private final static int MSGWHAT_REFRESH = 1;

    public QueryTimer(final MainActivity mainActivity){
        this.mainActivity = mainActivity;
        refreshHandler = new RefreshHander();
        schedule(new TimerTask() {
            @Override
            public void run() {
                doRefreshTimerAction();
            }
        }, 1, InstantValue.QUERY_FREQUENCY * 1000);
    }

    private void doRefreshTimerAction(){
        if(InstantValue.URL_TOMCAT == null || InstantValue.URL_TOMCAT.length() == 0)
            return;

        HttpOperator ho = mainActivity.getHttpOperator();
        ArrayList<WaitingDish> wds = ho.loadWaitingDish();
        refreshHandler.sendMessage(CommonTool.buildMessage(MSGWHAT_REFRESH, wds));
    }

    class RefreshHander extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSGWHAT_REFRESH) {
                mainActivity.setWaitingDish((ArrayList<WaitingDish>) msg.obj);
            }
        }

    }
}
