package com.example.ken.pushdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PushDemo extends Activity {
    protected final String PROJECT_ID = "1001801144258";
    protected final String PUSH_SERVER = "https://www.gristlebone.com/School/push_demo/push_demo.php";

    TextView deviceIDTextView;
    TextView returnMessageTextView;
    Context context;
    GoogleCloudMessaging gcm;
    StoreRegistrationToServer store;
    GetRegistration gr;
    String registration_id;
    SharedPreferences share;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_demo);
        share = getSharedPreferences("saved_prefs", MODE_PRIVATE);
        deviceIDTextView = (TextView) findViewById(R.id.reg_id);
        returnMessageTextView = (TextView) findViewById(R.id.received);

        context = getApplicationContext();
        store = new StoreRegistrationToServer();
        registration_id = "this";

        gr = new GetRegistration();
        Button reg_button = (Button) findViewById(R.id.reg_button);
        gcm = GoogleCloudMessaging.getInstance(this);
//        if(share.contains("reg_id")){
//            registration_id = share.getString("reg_id","none");
//            deviceIDTextView.setText(registration_id);
//            reg_button.setVisibility(View.GONE);
//        }
//        else
//        {
            reg_button.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if(intent.hasExtra("message")){
            Log.d("OnResume", "onResume(), = " + intent.getStringExtra("message"));
            returnMessageTextView.setText(intent.getStringExtra("message"));
        }


    }

    @Override
    protected void onNewIntent( Intent intent ) {
        Log.d( "newIntent", "onNewIntent(), intent = " + intent );
//        super.onNewIntent( intent );
        this.setIntent( intent );
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String somestring = intent.getStringExtra( "message" );

        Log.d( "onStart", "onStart(), somestring = " + somestring );
    }

    public void getRegistration(View view){
        gr.execute();
    }

    public void updateRegID(){
        registration_id = gr.reg;
        store.reg_id = registration_id;
        store.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.push_demo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void postReceivedMessage(String message){
        returnMessageTextView.setText(message);
    }

    public class GetRegistration extends AsyncTask{
            String reg;
            @Override
            protected Object doInBackground(Object[] params) {
                gcm = GoogleCloudMessaging.getInstance(context);
                try{
                    reg = gcm.register(PROJECT_ID);
                }
                catch(IOException ex){
                    Log.i("Error:",ex.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                updateRegID();
            }
    }
    public void updateActivity(){
        deviceIDTextView.setText(registration_id);
        SharedPreferences.Editor edit = share.edit();
        edit.putString("reg_id",(String)registration_id);
        edit.commit();
    }
    public void sendMessage(View view){
        TextView messageTextView = (TextView) findViewById(R.id.test_message);
        new AsyncTask(){
            @Override
            protected Object doInBackground(Object[] params) {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(PUSH_SERVER);
                String message = "This message sent from Android Device";
                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("message", message));
                    nameValuePairs.add(new BasicNameValuePair("device",registration_id));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httppost);

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
                return null;
            }
        }.execute();

    }

    public class StoreRegistrationToServer extends AsyncTask {

            String reg_id;

            @Override
            protected Object doInBackground(Object[] params) {
                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(PUSH_SERVER);

                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("registration_id", reg_id));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httppost);

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
                catch (Exception ex){
                    Log.d("ERROR",ex.getMessage());
                }


                return null;
            }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            updateActivity();
        }
    }

}
