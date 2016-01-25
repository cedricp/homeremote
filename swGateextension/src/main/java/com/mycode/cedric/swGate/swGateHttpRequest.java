package com.mycode.cedric.swGate;

/**
 * Created by cedric on 5/26/15.
 */

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 */
public class swGateHttpRequest extends AsyncTask<String, Void, String>
{
    private onHttpRequestComplete listener;
    private String result;

    public swGateHttpRequest(onHttpRequestComplete listener){
        this.listener = listener;
    }

    public interface onHttpRequestComplete {
        void onHttpRequestComplete(String s);
    }

    @Override
    protected String doInBackground(String... urls) {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 4000);
        HttpResponse response = null;
        HttpClient client = new DefaultHttpClient();
        HttpGet httpget= new HttpGet(urls[0]);

        try {
            response = client.execute(httpget);
            HttpEntity resEntityGet = response.getEntity();
            if (response != null) {
                // do something with the response
                String resp = EntityUtils.toString(resEntityGet);
                this.result = resp;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            this.result = "NOK";
        }
        return this.result;

    }

    @Override
    protected void onPostExecute(String s){
        listener.onHttpRequestComplete(s);
    }

}