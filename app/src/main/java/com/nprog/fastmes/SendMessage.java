package com.nprog.fastmes;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiMessage;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Lenovo on 20.03.2018.
 */

public class SendMessage extends Activity {
    Task0 t;
    ArrayList<Message> messages = new ArrayList<>();
    Long id ;
    EditText text;
    Button send;
    ListView listView;
    MessageAdapter adapter;
    String TopName = "";
    int done = 0;
    int u = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog);
        messages = CustomAdapter.messageArrayList;
        id = new Long(0);
        id = Long.parseLong(getIntent().getStringExtra("id"));
        TopName = getIntent().getStringExtra("user_name");
        TextView t1 = (TextView) findViewById(R.id.UserName);
        t1.setText(TopName);
        Collections.reverse(messages);
        text = (EditText) findViewById(R.id.text);
        listView = (ListView) findViewById(R.id.listMsg);
        adapter = new MessageAdapter(this, messages);
        listView.setAdapter(adapter);
        listView.setSelection(listView.getCount() - 1);
        //listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VKRequest request = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_ID, id, VKApiConst.MESSAGE, text.getText().toString()));
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        text.setText("");
                    }
                });
                //update screen
            }
        });

        t = new Task0();
        t.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(u == 1) {
            t = new Task0();
            t.execute();
            u = 0;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (u == 0) {
            t.cancel(true);
            u = 1;
        }
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        if(u == 1) {
            t = new Task0();
            t.execute();
            u = 0;
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (u == 0) {
            t.cancel(true);
            u = 1;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (u == 0) {
            t.cancel(true);
            u = 1;
        }
    }
    class Task0 extends AsyncTask<Void,Void,Void>{

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Collections.reverse(messages);
            if(messages.size()>0)
            adapter.updateUI(messages);
            done = 1;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                if(this.isCancelled()){
                    break;
                }
                try {
                    messages.clear();
                    VKRequest request1 = new VKRequest("messages.getHistory", VKParameters.from(VKApiConst.USER_ID, id, VKApiConst.COUNT, 100));
                    request1.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            try {
                                JSONArray array = response.json.getJSONObject("response").getJSONArray("items");
                                VKApiMessage[] msg = new VKApiMessage[array.length()];
                                for (int i = 0; i < array.length(); i++) {
                                    VKApiMessage mes = new VKApiMessage(array.getJSONObject(i));
                                    msg[i] = mes;
                                }
                                for (VKApiMessage mess : msg) {
                                    Message obj = new Message();
                                    if (mess.out) {
                                        obj.out = "+";
                                    } else {
                                        obj.out = "-";
                                    }
                                    obj.text = mess.body;
                                    obj.date = String.valueOf(mess.date);
                                    obj.read = mess.read_state;
                                    messages.add(obj);
                                }
                                publishProgress();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(VKError error) {
                            super.onError(error);
                        }
                    });
                    while (done == 0) {
                        Thread.sleep(500);
                    }
                    done = 0;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}