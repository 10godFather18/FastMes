package com.nprog.fastmes;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKBatchRequest;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiChat;
import com.vk.sdk.api.model.VKApiDialog;
import com.vk.sdk.api.model.VKApiGetDialogResponse;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.dialogs.VKCaptchaDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    ArrayList<Dialog> chats = new ArrayList<>();
    int u = 0;
    CustomAdapter adapter;
    Task1 t1;
    int done = 0;
    String FirstName = "Author";
    String SecondName = "Author";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        if (VKSdk.isLoggedIn()) {
            VKRequest r = new VKRequest("account.getProfileInfo");
            r.executeWithListener(new VKRequest.VKRequestListener()
            {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);

                    String status = "";

                    try {
                        JSONObject jsonObject = response.json.getJSONObject("response");
                        FirstName = jsonObject.getString("first_name");
                        SecondName  = jsonObject.getString("last_name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
            listView = (ListView) findViewById(R.id.listView);
            VKRequest req = VKApi.messages().getDialogs(VKParameters.from(VKApiConst.COUNT, 11));
            req.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    JSONArray array = null;
                    try {
                        array = response.json.getJSONObject("response").getJSONArray("items");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < array.length(); i++) {
                        int chat_id = 0;
                        int flag = 0;
                        try {
                            chat_id = array.getJSONObject(i).getJSONObject("message").getInt("chat_id");
                            flag = 1;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        int unread = 0;
                        int flag1 = 0;
                        try {
                            unread = array.getJSONObject(i).getInt("unread");
                            flag1 = 1;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (flag == 1) {
                            MultiChat obj = new MultiChat();
                            try {
                                if(flag1==1){
                                    obj.unread_messages = unread;
                                }
                                else{
                                    obj.unread_messages = 0;
                                }
                                obj.chat_id = array.getJSONObject(i).getJSONObject("message").getInt("chat_id");
                                obj.chat = true;
                                obj.title = array.getJSONObject(i).getJSONObject("message").getString("title");
                                obj.message_author = array.getJSONObject(i).getJSONObject("message").getInt("user_id");
                                obj.message_text = array.getJSONObject(i).getJSONObject("message").getString("body");
                                obj.message_date = array.getJSONObject(i).getJSONObject("message").getLong("date");
                                obj.message_unread = array.getJSONObject(i).getJSONObject("message").getInt("read_state"); //1-read, 0-unread
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            chats.add(obj);
                        } else {
                            Chat obj = new Chat();
                            try {
                                if(flag1==1){
                                    obj.unread_messages = unread;
                                }
                                else{
                                    obj.unread_messages = 0;
                                }
                                obj.user_id = array.getJSONObject(i).getJSONObject("message").getInt("user_id");
                                obj.chat = false;
                                obj.message_author = array.getJSONObject(i).getJSONObject("message").getInt("user_id");
                                obj.message_text = array.getJSONObject(i).getJSONObject("message").getString("body");
                                obj.message_date = array.getJSONObject(i).getJSONObject("message").getLong("date");
                                obj.message_unread = array.getJSONObject(i).getJSONObject("message").getInt("read_state"); //1-read, 0-unread
                                obj.out = array.getJSONObject(i).getJSONObject("message").getInt("out"); // 0 - not your, 1 - your
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            chats.add(obj);
                        }
                    }
                    //Log.e("Tag",String.valueOf(response.json));
                    String str = "";
                    for (int i = 0; i < chats.size(); i++) {
                        if (chats.get(i).chat == false) {
                            str += ((Chat) chats.get(i)).user_id + ",";
                        }
                    }
                    str = str.substring(0, str.length() - 2);
                    VKRequest reqt = VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS, str));
                    reqt.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            try {
                                JSONArray array = response.json.getJSONArray("response");
                                int iter = 0;
                                for (int i = 0; i < chats.size(); i++) {
                                    if (chats.get(i).chat == false) {
                                        if(iter<array.length()) {
                                            VKApiUser user = new VKApiUser(array.getJSONObject(iter));
                                            if(chats.get(i).out == 0){
                                                chats.get(i).message_author_name = user.first_name + " " +user.last_name.substring(0,1)+ ".";
                                            }
                                            else{
                                                chats.get(i).message_author_name = FirstName+ " " +SecondName.substring(0,1)+ ".";
                                            }
                                            ((Chat) chats.get(i)).user_name = user.first_name + " " + user.last_name; //TODO: DELETED
                                            ((Chat) chats.get(i)).online = user.online;
                                            ((Chat) chats.get(i)).mobile_online = user.online_mobile;
                                            iter++;
                                        }
                                    }
                                }
                                adapter = new CustomAdapter(MainActivity.this, chats);
                                listView.setAdapter(adapter);
                                t1 = new Task1();
                                t1.execute();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(VKError error) {
                            super.onError(error);
                            Log.e("TAG", "ERROR");
                        }
                    });
                }

                @Override
                public void onError(VKError error) {
                    super.onError(error);
                    Log.e("TAG", "FATAL_ERROR");
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.Logout:
                VKSdk.logout();
                //other logout too;
                Toast.makeText(MainActivity.this, "Вы вышли из всех соц.сетей", Toast.LENGTH_LONG).show();
                break;
            case R.id.Add:
                startActivity(new Intent(MainActivity.this, AddingActivity.class));
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (u == 1) {
            t1 = new Task1();
            t1.execute();
            u = 0;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (u == 1) {
            t1 = new Task1();
            t1.execute();
            u = 0;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (u == 0) {
            t1.cancel(true);
            u = 1;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (u == 0) {
            t1.cancel(true);
            u = 1;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (u == 0) {
            t1.cancel(true);
            u = 1;
        }
    }

    class Task1 extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if(chats.size()>0)
            adapter.updateUI(chats);
            done = 1;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                if (this.isCancelled()) {
                    break;
                }
                try {
                    if (VKSdk.isLoggedIn()) {
                        chats.clear();
                        VKRequest req = VKApi.messages().getDialogs(VKParameters.from(VKApiConst.COUNT, 11));
                        req.executeWithListener(new VKRequest.VKRequestListener() {
                            @Override
                            public void onComplete(VKResponse response) {
                                super.onComplete(response);
                                JSONArray array = null;
                                try {
                                    array = response.json.getJSONObject("response").getJSONArray("items");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                for (int i = 0; i < array.length(); i++) {
                                    int chat_id = 0;
                                    int flag = 0;
                                    try {
                                        chat_id = array.getJSONObject(i).getJSONObject("message").getInt("chat_id");
                                        flag = 1;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    int unread = 0;
                                    int flag1 = 0;
                                    try {
                                        unread = array.getJSONObject(i).getInt("unread");
                                        flag1 = 1;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    if (flag == 1) {
                                        MultiChat obj = new MultiChat();
                                        try {
                                            if(flag1==1){
                                                obj.unread_messages = unread;
                                            }
                                            else{
                                                obj.unread_messages = 0;
                                            }
                                            obj.chat_id = array.getJSONObject(i).getJSONObject("message").getInt("chat_id");
                                            obj.chat = true;
                                            obj.title = array.getJSONObject(i).getJSONObject("message").getString("title");
                                            obj.message_author = array.getJSONObject(i).getJSONObject("message").getInt("user_id");
                                            obj.message_text = array.getJSONObject(i).getJSONObject("message").getString("body");
                                            obj.message_date = array.getJSONObject(i).getJSONObject("message").getLong("date");
                                            obj.message_unread = array.getJSONObject(i).getJSONObject("message").getInt("read_state"); //1-read, 0-unread
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        chats.add(obj);
                                    } else {
                                        Chat obj = new Chat();
                                        try {
                                            if(flag1==1){
                                                obj.unread_messages = unread;
                                            }
                                            else{
                                                obj.unread_messages = 0;
                                            }
                                            obj.user_id = array.getJSONObject(i).getJSONObject("message").getInt("user_id");
                                            obj.chat = false;
                                            obj.message_author = array.getJSONObject(i).getJSONObject("message").getInt("user_id");
                                            obj.message_text = array.getJSONObject(i).getJSONObject("message").getString("body");
                                            obj.message_date = array.getJSONObject(i).getJSONObject("message").getLong("date");
                                            obj.message_unread = array.getJSONObject(i).getJSONObject("message").getInt("read_state"); //1-read, 0-unread
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        chats.add(obj);
                                    }
                                }
                                String str = "";
                                int o = 0;
                                for (int i = 0; i < chats.size(); i++) {
                                    if (chats.get(i).chat==false) {
                                        str += ((Chat) chats.get(i)).user_id + ",";
                                    }
                                }
                                str = str.substring(0, str.length() - 2);
                                VKRequest reqt = VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS, str));
                                reqt.executeWithListener(new VKRequest.VKRequestListener() {
                                    @Override
                                    public void onComplete(VKResponse response) {
                                        super.onComplete(response);
                                        try {
                                            JSONArray array = response.json.getJSONArray("response");
                                            int iter = 0;
                                            for (int i = 0; i < chats.size(); i++) {
                                                if (!chats.get(i).chat) {
                                                    if (iter < array.length()) {
                                                        VKApiUser user = new VKApiUser(array.getJSONObject(iter));
                                                        if(chats.get(i).out == 0){
                                                            chats.get(i).message_author_name = user.first_name + " " +user.last_name.substring(0,1)+ ".";
                                                        }
                                                        else{
                                                            chats.get(i).message_author_name = FirstName+ " " +SecondName.substring(0,1)+ ".";
                                                        }
                                                        iter++;
                                                        ((Chat) chats.get(i)).user_name = user.first_name + " " + user.last_name;    //TODO: MISTAKE!!!
                                                        ((Chat) chats.get(i)).online = user.online;
                                                        ((Chat) chats.get(i)).mobile_online = user.online_mobile;
                                                    }
                                                }
                                            }
                                            publishProgress();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onError(VKError error) {
                                        super.onError(error);
                                        Log.e("TAG", "ERROR");
                                    }
                                });
                            }

                            @Override
                            public void onError(VKError error) {
                                super.onError(error);
                                Log.e("TAG", "FATAL_ERROR");
                            }
                        });
                    } else {
                        break;
                    }
                    while (done == 0) {
                        Thread.sleep(500);
                    }
                    done = 0;
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}