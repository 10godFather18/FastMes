package com.nprog.fastmes;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiDialog;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKList;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

/**
 * Created by Lenovo on 21.03.2018.
 */

public class MessageAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Message> messages;

    public MessageAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = (ArrayList<Message>) messages.clone();
    }


    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    public void updateUI(ArrayList<Message> messages){
        this.messages = (ArrayList<Message>)messages.clone();
        this.notifyDataSetChanged();
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view1 = inflater.inflate(R.layout.style_list_view1,null);
            LinearLayout l = (LinearLayout) view1.findViewById(R.id.linearLayout);
            TextView timeView = (TextView) view1.findViewById(R.id.time0);
            TextView messageView = (TextView) view1.findViewById(R.id.message0);
            TextView userView = (TextView) view1.findViewById(R.id.name_date);
            Long ticks = Long.parseLong(messages.get(i).date);
            Calendar mydate = Calendar.getInstance();
                mydate.setTimeInMillis(ticks * 1000);
                timeView.setText(mydate.get(Calendar.HOUR) + ":" +
                        mydate.get(Calendar.MINUTE) + ":" +
                        mydate.get(Calendar.SECOND)+"____"+
                        mydate.get(Calendar.DAY_OF_MONTH) + "." +
                        mydate.get(Calendar.MONTH) + "." +
                        mydate.get(Calendar.YEAR));
                messageView.setText(messages.get(i).text);
        if (messages.get(i).out.contains("+")) {
            messageView.setGravity(Gravity.RIGHT);
            view1.setPadding(150,25,50,25);
            timeView.setGravity(Gravity.RIGHT);
            l.setBackgroundColor(Color.rgb(174,253,253));
        }
        if (messages.get(i).out.contains("-")) {
            messageView.setGravity(Gravity.LEFT);
            view1.setPadding(50,25,150,25);
            timeView.setGravity(Gravity.LEFT);
            l.setBackgroundColor(Color.rgb(143,253,143));
        }
        return  view1;
    }

}
