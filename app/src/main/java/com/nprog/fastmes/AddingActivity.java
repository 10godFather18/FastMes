package com.nprog.fastmes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

public class AddingActivity extends AppCompatActivity {
    private Button addvkbutton;
    private int APP_ID = 6402718;
    private boolean isResumed = false;
    private static final String[] sMyScope  = new String[]{
            VKScope.MESSAGES
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding);
        addvkbutton = (Button) findViewById(R.id.addVKButton);
        if(VKSdk.isLoggedIn()){
            addvkbutton.setText("Выйти из VK");
            //удалить листенеры
            addvkbutton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    VKSdk.logout();
                    addvkbutton.setText("Войти в VK");
                    addvkbutton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            VKSdk.login(AddingActivity.this,sMyScope);
                        }
                    });

                }
            });
        }else {
            addvkbutton.setText("Войти в VK");
            //удалить листенеры
            addvkbutton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    VKSdk.login(AddingActivity.this,sMyScope);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
    }

    @Override
    protected void onPause() {
        isResumed = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKCallback<VKAccessToken> callback = new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Toast.makeText(AddingActivity.this, "Вы вошли.", Toast.LENGTH_LONG).show();
                addvkbutton.setText("Выйти из VK");
                addvkbutton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        VKSdk.logout();
                        addvkbutton.setText("Войти в VK");
                        addvkbutton.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view){
                                VKSdk.login(AddingActivity.this,sMyScope);

                            }
                        });
                    }
                });
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(AddingActivity.this, "Вы не вошли.", Toast.LENGTH_LONG).show();
            }
        };

        if (!VKSdk.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
