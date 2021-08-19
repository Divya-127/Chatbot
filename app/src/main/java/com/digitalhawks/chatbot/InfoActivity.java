package com.digitalhawks.chatbot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class InfoActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    EditText name;
    Button next;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        sharedPreferences =getApplicationContext().getSharedPreferences("com.digitalhawks.chatbot", Context.MODE_PRIVATE);
        name = findViewById(R.id.name);
        next = findViewById(R.id.next);
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                for (Voice tmpVoice:textToSpeech.getVoices())
                {
                    if (tmpVoice.getName().equals("en-AU-Wavenet-D")){
                        break;
                    }
                }
            }
        },"com.google.android.tts");

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (check())
                {
                    sharedPreferences.edit().putString("name",name.getText().toString()).apply();
                    Intent intent = new Intent(InfoActivity.this,ChatActivity.class);
                    startActivity(intent);
                    Toast.makeText(InfoActivity.this,"Hello "+sharedPreferences.getString("name","username"),Toast.LENGTH_SHORT).show();
                    textToSpeech.speak("Insperon Activated ",TextToSpeech.QUEUE_FLUSH,null,null);
                }
            }
        });
    }
    public boolean check(){
        String txt_name = name.getText().toString();
        if(TextUtils.isEmpty(txt_name))
        {
            Toast.makeText(InfoActivity.this,"All Fields are mandatory",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}