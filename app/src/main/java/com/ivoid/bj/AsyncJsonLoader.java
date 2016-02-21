package com.ivoid.bj;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AsyncJsonLoader extends AsyncTask<String, Integer, JSONObject> {

    private boolean isInProgress = false;
    private String dialogMessage;
    private ProgressDialog mDialog = null;
    private Context mContext = null;

    public interface AsyncCallback {
        boolean postExecute(JSONObject result);
        boolean postError();
    }

    private AsyncCallback mAsyncCallback = null;

    public AsyncJsonLoader(Context context, AsyncCallback _asyncCallback) {
        mContext = context;
        mAsyncCallback = _asyncCallback;
        dialogMessage = "Loading";
    }
    public AsyncJsonLoader(Context context, AsyncCallback _asyncCallback, String message) {
        mContext = context;
        mAsyncCallback = _asyncCallback;
        dialogMessage = message;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        isInProgress = true;
        showDialog();
    }

    @Override
    protected void onProgressUpdate(Integer... _progress) {
        super.onProgressUpdate(_progress);
    }

    @Override
    protected void onPostExecute(JSONObject _result) {
        super.onPostExecute(_result);
        dismissDialog();
        isInProgress = false;
        if (_result == null) {
            if (!mAsyncCallback.postError()) {
                showConnectError();
            }
            return;
        }
        if(!mAsyncCallback.postExecute(_result)){
            if (!mAsyncCallback.postError()) {
                showConnectError();
            }
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected JSONObject doInBackground(String... _uri) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse;
        try {
            if(_uri[0].equals("GET")) {
                HttpGet httpGet = new HttpGet(_uri[1]);
                httpResponse = httpClient.execute(httpGet);
            }else if(_uri[0].equals("POST")){
                HttpPost httpPost = new HttpPost(_uri[1]);
                ArrayList <NameValuePair> params = new ArrayList<NameValuePair>();
                for(int i = 2; i < _uri.length; i+=2){
                    params.add( new BasicNameValuePair(_uri[i], _uri[i+1]));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
                httpResponse = httpClient.execute(httpPost);
            }else{
                HttpGet httpGet = new HttpGet(_uri[0]);
                httpResponse = httpClient.execute(httpGet);
            }
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                httpResponse.getEntity().writeTo(outputStream);
                outputStream.close();
                return new JSONObject(outputStream.toString());
            } else {
                httpResponse.getEntity().getContent().close();
                throw new IOException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ダイアログ表示
    public void showDialog() {
        if(mDialog == null && dialogMessage!= null) {
            mDialog = new ProgressDialog(mContext);
            mDialog.setMessage(dialogMessage);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setCancelable(false);
            mDialog.show();
        }
    }

    // ロード中ダイアログ非表示
    public void dismissDialog() {
        if (mDialog !=  null) {
            mDialog.dismiss();
        }
        mDialog = null;
    }

    // エラーメッセージ表示
    private void showConnectError() {
        Toast toast = Toast.makeText(mContext, "Connection error has occurred.\n" +
                "Please check your Internet connection.", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public synchronized boolean isInProcess() { return isInProgress; }
}
