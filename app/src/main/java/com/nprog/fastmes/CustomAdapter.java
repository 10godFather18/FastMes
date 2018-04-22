package com.nprog.fastmes;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiUsers;
import com.vk.sdk.api.model.VKApiDialog;
import com.vk.sdk.api.model.VKApiGetDialogResponse;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

/**
 * Created by Lenovo on 20.03.2018.
 */

public class CustomAdapter extends BaseAdapter{
    private Context context;
    private SetData setData;
    public ArrayList<Dialog> dialogs = new ArrayList<>();
    public static ArrayList<Message> messageArrayList = new ArrayList<>();

    public CustomAdapter(Context context, ArrayList<Dialog> dialogs) {
        this.context = context;
        this.dialogs = dialogs;
    }
    @Override
    public int getCount() {
        return dialogs.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    public void updateUI( ArrayList<Dialog> dialogs ){
        this.dialogs = (ArrayList<Dialog>)dialogs.clone();
        this.notifyDataSetChanged();
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        setData = new SetData();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view1 = inflater.inflate(R.layout.style_list_view,null);
        setData.user_name = (TextView) view1.findViewById(R.id.textView);
        setData.msg = (TextView) view1.findViewById(R.id.textView2);
        setData.time_msg = (TextView) view1.findViewById(R.id.textView3);
        TextView Name = (TextView) view1.findViewById(R.id.name);
        Name.setText(dialogs.get(i).message_author_name);
        final int k = i;
        if(!dialogs.get(i).chat){
            String online = "";
            if((((Chat)dialogs.get(k)).online==true)||(((Chat)dialogs.get(k)).mobile_online==true)){
                online = " (online)";
            }
            else
            {
                online = " (offline)";
            }
            setData.user_name.setText(((Chat)dialogs.get(k)).user_name + online + " (new: " + String.valueOf(dialogs.get(k).unread_messages)+")");
        } else {
            setData.user_name.setText(((MultiChat)dialogs.get(k)).title + " (new: " + String.valueOf(dialogs.get(k).unread_messages) + ")");
        }
        String ans;
        if(dialogs.get(k).message_text.length()>40){
            ans=dialogs.get(k).message_text.substring(0,37)+"...";
        }else{
            ans=dialogs.get(k).message_text;
        }
        setData.msg.setText(ans);
        Long ticks = Long.parseLong(String.valueOf(dialogs.get(k).message_date));
        Calendar mydate = Calendar.getInstance();
        mydate.setTimeInMillis(ticks * 1000);
        String read = "";
        if(dialogs.get(i).message_unread == 1){
            read = "(read) ";
        }else{
            read = "(unread) ";
        }
        setData.time_msg.setText(read+mydate.get(Calendar.HOUR) + ":" +
                mydate.get(Calendar.MINUTE) + ":" +
                mydate.get(Calendar.SECOND)+"____"+
                mydate.get(Calendar.DAY_OF_MONTH) + "." +
                mydate.get(Calendar.MONTH) + "." +
                mydate.get(Calendar.YEAR));
        if((dialogs!=null) && (dialogs.size()>0))
        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long id = 0;
                if(dialogs.get(k).chat == false){
                    id = Long.valueOf(((Chat)(dialogs.get(k))).user_id).longValue();
                    final long id1 = id;
                    VKRequest request = new VKRequest("messages.getHistory", VKParameters.from(VKApiConst.USER_ID, id1,VKApiConst.COUNT,100));
                    request.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            try {
                                messageArrayList.clear();
                                JSONArray array = response.json.getJSONObject("response").getJSONArray("items");
                                VKApiMessage [] msg = new VKApiMessage[array.length()];
                                for (int i = 0; i < array.length(); i++){
                                    VKApiMessage mes = new VKApiMessage(array.getJSONObject(i));
                                    msg[i] = mes;
                                }
                                for(VKApiMessage mess : msg){
                                    Message obj = new Message();
                                    if(mess.out){
                                        obj.out = "+";
                                    }else{
                                        obj.out = "-";
                                    }
                                    obj.text = mess.body;
                                    obj.date = String.valueOf(mess.date);
                                    obj.read = mess.read_state;
                                    messageArrayList.add(obj);
                                    //TODO: set author_name
                                }
                                String online = "";
                                if((((Chat)dialogs.get(k)).online==true)||(((Chat)dialogs.get(k)).mobile_online==true)){
                                    online = " (online)";
                                }
                                else
                                {
                                    online = " (offline)";
                                }
                                context.startActivity(new Intent(context, SendMessage.class).putExtra("id",String.valueOf(id1)).putExtra("user_name",((Chat)dialogs.get(k)).user_name + online)/*.putExtra("name",String.valueOf(((Chat)dialogs.get(k)).user_name)).putExtra("chat","-")*/);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(VKError error) {
                            super.onError(error);
                        }
                    });
                }else{

                    id = Long.valueOf(((MultiChat)(dialogs.get(k))).chat_id).longValue(); //VERSION ERROR
                    VKRequest request = new VKRequest("messages.getHistory", VKParameters.from("peer_id", id+2000000000,VKApiConst.COUNT,100,VKApiConst.VERSION,5.74));
                    final long finalId = id;
                    request.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            try {
                                //TODO:разобрать ответ, просетать mesage_author_name
                                messageArrayList.clear();
                                JSONArray array = response.json.getJSONObject("response").getJSONArray("items");
                                VKApiMessage [] msg = new VKApiMessage[array.length()];
                                for (int i = 0; i < array.length(); i++){
                                    VKApiMessage mes = new VKApiMessage(array.getJSONObject(i));
                                    msg[i] = mes;
                                }
                                for(VKApiMessage mess : msg){
                                    Message obj = new Message();
                                    if(mess.out){
                                        obj.out = "+";
                                    }else{
                                        obj.out = "-";
                                    }
                                    obj.text = mess.body;
                                    obj.date = String.valueOf(mess.date);
                                    obj.read = mess.read_state;
                                    messageArrayList.add(obj);
                                }
                                context.startActivity(new Intent(context, SendMessage.class).putExtra("id",String.valueOf(finalId)));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(VKError error) {
                            super.onError(error);
                        }
                    });

                }
            }
        });

        return view1;
    }
    public class SetData{
        TextView user_name, msg, time_msg;
    }
}
