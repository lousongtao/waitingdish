package com.shuishou.waitingdish.http;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shuishou.waitingdish.InstantValue;
import com.shuishou.waitingdish.R;
import com.shuishou.waitingdish.bean.HttpResult;
import com.shuishou.waitingdish.bean.UserData;
import com.shuishou.waitingdish.bean.WaitingDish;
import com.shuishou.waitingdish.ui.LoginActivity;
import com.shuishou.waitingdish.ui.MainActivity;
import com.shuishou.waitingdish.utils.CommonTool;
import com.yanzhenjie.nohttp.FileBinary;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;
import com.yanzhenjie.nohttp.rest.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/6/9.
 */

public class HttpOperator {

    private String logTag = "HttpOperation";

    private MainActivity mainActivity;
    private LoginActivity loginActivity;

//    private static final int WHAT_VALUE_WAITINGDISH = 1;
    private static final int WHAT_VALUE_LOGIN = 2;

    private Gson gson = new Gson();

    private OnResponseListener responseListener =  new OnResponseListener<JSONObject>() {
        @Override
        public void onStart(int what) {
        }

        @Override
        public void onSucceed(int what, Response<JSONObject> response) {
            switch (what){
//                case WHAT_VALUE_WAITINGDISH :
//                    doResponseWaitingDish(response);
//                    break;
                case WHAT_VALUE_LOGIN :
                    doResponseLogin(response);
                    break;
                default:
            }
        }

        @Override
        public void onFailed(int what, Response<JSONObject> response) {
            Log.e("Http failed", "what = "+ what + "\nresponse = "+ response.get());
            MainActivity.LOG.error("Response Listener On Faid. what = "+ what + "\nresponse = "+ response.get());
            switch (what){
//                case WHAT_VALUE_WAITINGDISH :
//                    CommonTool.popupWarnDialog(mainActivity, R.drawable.error, "WRONG", "Failed to load waiting dish data. Please restart app!");
//                    break;
                case WHAT_VALUE_LOGIN :
                    CommonTool.popupWarnDialog(loginActivity, R.drawable.error, "WRONG", "Failed to login. Please retry!");
                    break;
                default:
            }

        }

        @Override
        public void onFinish(int what) {
        }
    };

    private RequestQueue requestQueue = NoHttp.newRequestQueue();

    public HttpOperator(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public HttpOperator(LoginActivity loginActivity) {
        this.loginActivity = loginActivity;
    }

    private void doResponseLogin(Response<JSONObject> response){
        if (response.getException() != null){
            Log.e(logTag, "doResponseLogin: " + response.getException().getMessage() );
            MainActivity.LOG.error("doResponseLogin: " + response.getException().getMessage());
            sendErrorMessageToToast("doResponseLogin: " + response.getException().getMessage());
            return;
        }
        JSONObject loginResult = response.get();
        try {
            if ("ok".equals(loginResult.getString("result"))){
                UserData loginUser = new UserData();
                loginUser.setId(loginResult.getInt("userId"));
                loginUser.setName(loginResult.getString("userName"));
                loginActivity.loginSuccess(loginUser);
            } else {
                Log.e(logTag, "doResponseLogin: get FAIL while login" + loginResult.getString("result"));
                MainActivity.LOG.error("doResponseLogin: get FAIL while login"  + loginResult.getString("result"));
                sendErrorMessageToToast("doResponseLogin: get FAIL while login"  + loginResult.getString("result"));
            }
        } catch (JSONException e) {
            Log.e(logTag, "doResponseLogin: parse json: " + e.getMessage() );
            MainActivity.LOG.error("doResponseLogin: parse json:" + e.getMessage());
            sendErrorMessageToToast("doResponseLogin: parse json:" + e.getMessage());
        }
    }

//    private void doResponseWaitingDish(Response<JSONObject> response){
//        if (response.getException() != null){
//            Log.e(logTag, "doResponseWaitingDish: " + response.getException().getMessage() );
//            MainActivity.LOG.error("doResponseWaitingDish: " + response.getException().getMessage());
//            sendErrorMessageToToast("Http:doResponseWaitingDish: " + response.getException().getMessage());
//            return;
//        }
//        HttpResult<ArrayList<WaitingDish>> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<ArrayList<WaitingDish>>>(){}.getType());
//        if (result.success){
//            ArrayList<WaitingDish> wds = result.data;
//            mainActivity.setWaitingDish(wds);
//        } else {
//            Log.e(logTag, "doResponseWaitingDish: get FALSE for query confirm code");
//            MainActivity.LOG.error("doResponseWaitingDish: get FALSE for query confirm code");
//        }
//    }

    public ArrayList<WaitingDish> loadWaitingDish(){
        mainActivity.getProgressDlgHandler().sendMessage(CommonTool.buildMessage(MainActivity.PROGRESSDLGHANDLER_MSGWHAT_STARTLOADDATA,
                "start loading order data ..."));
        Request<JSONObject> request = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/indent/waitingdish");
//        requestQueue.add(WHAT_VALUE_WAITINGDISH, request, responseListener);
        Response<JSONObject> response = NoHttp.startRequestSync(request);
        if (response.getException() != null){
            Log.e(logTag, "doResponseWaitingDish: " + response.getException().getMessage() );
            MainActivity.LOG.error("doResponseWaitingDish: " + response.getException().getMessage());
            sendErrorMessageToToast("Http:doResponseWaitingDish: " + response.getException().getMessage());
            return null;
        }
        HttpResult<ArrayList<WaitingDish>> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<ArrayList<WaitingDish>>>(){}.getType());
        if (result.success){
            ArrayList<WaitingDish> wds = result.data;
//            mainActivity.setWaitingDish(wds);
            return wds;
        } else {
            Log.e(logTag, "doResponseWaitingDish: get FALSE for query confirm code");
            MainActivity.LOG.error("doResponseWaitingDish: get FALSE for query confirm code");
            return null;
        }
    }


    private void sendErrorMessageToToast(String sMsg){
        if (mainActivity != null)
            mainActivity.getToastHandler().sendMessage(CommonTool.buildMessage(MainActivity.TOASTHANDLERWHAT_ERRORMESSAGE,sMsg));
    }

    public void uploadErrorLog(File file, String machineCode){
        int key = 0;// the key of filelist;
        UploadErrorLogListener listener = new UploadErrorLogListener(mainActivity);
        Request<JSONObject> request = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/common/uploaderrorlog", RequestMethod.POST);
        FileBinary bin1 = new FileBinary(file);
        request.add("logfile", bin1);
        request.add("machineCode", machineCode);
        listener.addFiletoList(key, file.getAbsolutePath());
        requestQueue.add(key, request, listener);
    }

    public void login(String name, String password){
        Request<JSONObject> request = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/login");
        request.add("username", name);
        request.add("password", password);
        requestQueue.add(WHAT_VALUE_LOGIN, request, responseListener);
    }

    /**
     * 得到服务端可用的apk文件列表
     * @return
     */
    public ArrayList<String> getUpgradeApkFiles(){
        Request<JSONObject> request = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/common/checkupgradeapk", RequestMethod.GET);
        Response<JSONObject> response = NoHttp.startRequestSync(request);
        if (response.getException() != null){
            Log.e(logTag, "chechMenuVersion: There are Exception to getUpgradeApkFiles\n"+ response.getException().getMessage() );
            MainActivity.LOG.error("chechMenuVersion: There are Exception to getUpgradeApkFiles\n"+ response.getException().getMessage() );
            sendErrorMessageToToast("Http:getUpgradeApkFiles: " + response.getException().getMessage());
            return null;
        }
        HttpResult<ArrayList<String>> result = gson.fromJson(response.get().toString(), new TypeToken<HttpResult<ArrayList<String>>>(){}.getType());
        if (result.success){
            return result.data;
        } else {
            Log.e(logTag, "get false from server while Check upgrade apk");
            MainActivity.LOG.error("get false from server while Check upgrade apk");
            sendErrorMessageToToast("get false from server while Check upgrade apk");
            return null;
        }
    }

    public HttpResult<Integer> doneDish(List<Integer> indentDetailIds) {
        Request request = NoHttp.createJsonObjectRequest(InstantValue.URL_TOMCAT + "/indent/waitingdishdone", RequestMethod.POST);
        Map<String, List<Integer>> map = new HashMap<>();
        map.put("indentDetailIds", indentDetailIds);
        request.add(map);
        Response<JSONObject> response = NoHttp.startRequestSync(request);
        if (response.getException() != null){
            HttpResult<Integer> result = new HttpResult<>();
            result.result = response.getException().getMessage();
            return result;
        }
        if (response.get() == null) {
            Log.e(logTag, "Error occur while done dish. response.get() is null.");
            MainActivity.LOG.error("Error occur while done dish. response.get() is null.");
            HttpResult<Integer> result = new HttpResult<>();
            result.result = "Error occur while done dish. response.get() is null";
            return result;
        }
        return gson.fromJson(response.get().toString(), new TypeToken<HttpResult<Integer>>(){}.getType());
    }
}
