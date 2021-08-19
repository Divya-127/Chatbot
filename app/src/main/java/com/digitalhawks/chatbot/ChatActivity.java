package com.digitalhawks.chatbot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.util.TimeUnit;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.MANAGE_OWN_CALLS;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.RECORD_AUDIO;
import static java.net.Proxy.Type.HTTP;


public class ChatActivity extends AppCompatActivity {
    ImageView send;//send button or voice button
    String t;//the text to be sent
    String title;
    String cityName = "";
    EditText text;//where text is written
    private SpeechRecognizer speechRecognizer;//speech Recognizer
    private TextToSpeech textToSpeech;// textToSpeech
    private Intent intent;
    private FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;
    String uname;
    ListView messages;//list where all the messages are displayed
    ArrayList<HashMap<String, String>> messagesList = new ArrayList<>();//messages and who is sending the messages
    SimpleAdapter simpleAdapter;//adapter for setting up listView
    String appName;
    int x = 0;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                        startActivity(intent);
                        mAuth.signOut();
                        finish();
                    }
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        send = findViewById(R.id.imageView);//giving send the id it is pointing to
        text = findViewById(R.id.textView2);//giving text the id it is pointing to
        //setting tag to it is voice or send button
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getApplicationContext().getSharedPreferences("com.digitalhawks.chatbot", MODE_PRIVATE);
        uname = sharedPreferences.getString("name", "user");
        send.setTag(R.drawable.ic_baseline_keyboard_voice_24);//tag showing that it is a voice button
        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO,CALL_PHONE,MANAGE_OWN_CALLS,READ_CONTACTS}, PackageManager.PERMISSION_GRANTED);//Asking for permissions from user
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status!=TextToSpeech.ERROR)
                {
                    textToSpeech.setLanguage(new Locale("hi","IN"));
                }
            }
        }, "com.google.android.tts");

        messages = findViewById(R.id.list);//giving messages the id it is pointing to
        textToSpeech.speak("तुम क्या कर रहे हो?", TextToSpeech.QUEUE_FLUSH, null, null);
        HashMap<String, String> hashMap = new HashMap<>();//Initializing a HashMap to store name and message
        hashMap.put("Name", "Insperon");//Adding name to HashMap
        hashMap.put("Message", "Hello, how do I help you ?");//adding message to HashMap
        messagesList.add(hashMap); //adding this given hashMap to messagesList ArrayList
        simpleAdapter = new SimpleAdapter(this, messagesList, android.R.layout.simple_list_item_2, new String[]{"Name", "Message"}, new int[]{android.R.id.text1, android.R.id.text2});//Setting up SimpleAdapter
        messages.setAdapter(simpleAdapter);
        text.addTextChangedListener(new TextWatcher()//Checking if there is any text change in the text written on EditText named text
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {//if change is there
                if (s.length() > 0 && !(String.valueOf(s).trim()).equals(""))//if length of thing is not 0 and is not just spaces
                {
                    send.setImageResource(R.drawable.ic_baseline_send_24);//set the send image to sent
                    send.setTag(R.drawable.ic_baseline_send_24);//and tag to send
                } else {//if length of thing is 0 or is just spaces
                    send.setImageResource(R.drawable.ic_baseline_keyboard_voice_24);//set send Image to voice
                    send.setTag(R.drawable.ic_baseline_keyboard_voice_24);//and tag to voice
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        String langpref = "hi_IN";
        String langPref = "gu_IN";
            intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,langPref);
            intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE,langpref);

        startActivityForResult(intent,1);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) { }
            @Override
            public void onBeginningOfSpeech() { }
            @Override
            public void onRmsChanged(float rmsdB) { }
            @Override
            public void onBufferReceived(byte[] buffer) { }
            @Override
            public void onEndOfSpeech() { }
            @Override
            public void onError(int error) { }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String string;
                text.setText("");
                if (matches != null) {
                    string = matches.get(0);
                    text.setText(string);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) { }
            @Override
            public void onEvent(int eventType, Bundle params) { }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if (resultCode==RESULT_OK && null!=data){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.d("Tag",result.toString());
                }
        }
    }

    public class downloadJoke extends AsyncTask<String,Void,String>
    {
        @Override
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection;
            try {
                StringBuilder result = new StringBuilder();
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                while (data != -1) {
                    char i = (char) data;
                    result.append(i);
                    data = reader.read();
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        public void timePass()
        {
            for (int i=0;i<100000;i++)
            {

            }
        }
            @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            String jokeTitle = "";
            String punchLine = "";
            try {
                JSONObject jsonObject = new JSONObject(s);
                jokeTitle = jsonObject.getString("setup");
                System.out.println(jokeTitle);
                HashMap<String,String> hashMap1 = new HashMap<String, String>();
                hashMap1.put("Name","Insperon");
                hashMap1.put("Message",jokeTitle);
                //textToSpeech.speak(jokeTitle,TextToSpeech.QUEUE_FLUSH,null,null);
                messagesList.add(hashMap1);
                simpleAdapter.notifyDataSetChanged();
                timePass();
                punchLine = jsonObject.getString("punchline");
                System.out.println(punchLine);
                HashMap<String,String> hashMap2 = new HashMap<String, String>();
                hashMap2.put("Name","Insperon");
                hashMap2.put("Message",punchLine);
                //textToSpeech.speak(punchLine,TextToSpeech.QUEUE_FLUSH,null,null);
                messagesList.add(hashMap2);
                simpleAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public class downloadInfo extends AsyncTask<String,Void,String>
    {

        @Override
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection;
            try {
                StringBuilder result = new StringBuilder();
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                while (data != -1) {
                    char i = (char) data;
                    result.append(i);
                    data = reader.read();
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String temperature="";
            String temp_min="";
            String temp_max="";
            String description="";
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONObject main1 = jsonObject.getJSONObject("main");
                temp_max =Double.toString(Math.round((Double.parseDouble(main1.getString("temp_max"))-273)*100.0)/100.0);
                temp_min =Double.toString(Math.round((Double.parseDouble(main1.getString("temp_min"))-273)*100.0)/100.0);
                temperature =Double.toString(Math.round((Double.parseDouble(main1.getString("temp"))-273)*100.0)/100.0);
                String weatherInfo = jsonObject.getString("weather");
                JSONArray array = new JSONArray(weatherInfo);
                Log.i("check",array.toString());
                Log.i("check",Integer.toString(array.length()));
                for (int i=0;i<array.length();i++)
                {
                    JSONObject jsonObject2 = array.getJSONObject(i);
                    description = jsonObject2.getString("description");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Could not found city",Toast.LENGTH_SHORT).show();
            }
            String finRes = description+"\n"+"Temperature :"+temperature+"\n"+"Min:"+temp_min+"\n"+"Max:"+temp_max;
            HashMap<String,String> hashMap = new HashMap<String, String>();
            hashMap.put("Name","Insperon");
            hashMap.put("Message",finRes);
            textToSpeech.speak(finRes,TextToSpeech.QUEUE_FLUSH,null,null);
            messagesList.add(hashMap);
            simpleAdapter.notifyDataSetChanged();
        }
    }

    public class downloadContents extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection;
            try {
                StringBuilder result = new StringBuilder();
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                while (data != -1) {
                    char i = (char) data;
                    result.append(i);
                    data = reader.read();
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            String res = s.split("\"extract\":")[1];
            String fin = res.substring(1,res.lastIndexOf('.'));
            HashMap<String,String> hashMap = new HashMap<String, String>();
            hashMap.put("Name","Insperon");
            hashMap.put("Message",fin);
            textToSpeech.speak(fin,TextToSpeech.QUEUE_FLUSH,null,null);
            messagesList.add(hashMap);
            simpleAdapter.notifyDataSetChanged();
            textToSpeech.speak("Do you want to know more?",TextToSpeech.QUEUE_FLUSH,null,null);
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void speak(View view)//if image of send vala is clicked
    {
        Integer resource = (Integer) send.getTag();//gets the tag attached to send and converts it to Integer
        if (resource.equals(R.drawable.ic_baseline_send_24))//if that Integer is equal to the place where image of send Image is there
        {
            speechRecognizer.stopListening();
            t = text.getText().toString().toLowerCase();
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("Name", uname);
            hashMap.put("Message", t);
            messagesList.add(hashMap);
            simpleAdapter.notifyDataSetChanged();
            text.setText("");
            send.setImageResource(R.drawable.ic_baseline_keyboard_voice_24);//after the operation is done then set it back to voice vala
            send.setTag(R.drawable.ic_baseline_keyboard_voice_24);//and tag ko bhi
            respond(t);
        } else if (resource.equals(R.drawable.ic_baseline_keyboard_voice_24))//if that Integer is equal to the place where image of send Voice is there
        {
            textToSpeech.speak("How can I help you?", TextToSpeech.QUEUE_FLUSH, null, null);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            speechRecognizer.startListening(intent);
            send.setImageResource(R.drawable.ic_baseline_send_24);//after the operation set it back to send vala
            send.setTag(R.drawable.ic_baseline_send_24);//and tag ko bhi
        }
    }

    public void respond(String quest)//Response is here all the questions will be answered here...
    {
        String[] arr = new String[10];
        HashMap<String, String> hashMap = new HashMap<>();
        //-----------------------------GENERAL RESPONSES-----------------------------//
        //HELLO
        if (there_exists(arr = new String[]{"hello","hi"})) {
            textToSpeech.speak("Hello!", TextToSpeech.QUEUE_FLUSH, null, null);
            hashMap.put("Name", "Insperon");
            hashMap.put("Message", "Hello!");
            messagesList.add(hashMap);
        }
        //USERNAME
        else if (there_exists(arr = new String[]{"my name"})) {
            textToSpeech.speak("तुम क्या कर रहे हो?" + sharedPreferences.getString("name", "user"), TextToSpeech.QUEUE_FLUSH, null, null);
            hashMap.put("Name", "Insperon");
            hashMap.put("Message", "How can I forget that your name is " + sharedPreferences.getString("name", "user"));
            messagesList.add(hashMap);
        }
        else if (there_exists(new String[]{"wikipedia"}))
        {
            title = t.substring(t.indexOf(" "));
            downloadContents task = new downloadContents();
            try {
                task.execute("https://en.wikipedia.org/w/api.php?action=query&prop=extracts&format=json&exsentences=3&exintro=&explaintext=&exsectionformat=plain&titles="+title);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (there_exists(new String[]{"weather","temperature"}))
        {
            cityName = t.substring(t.lastIndexOf(" "));
            downloadInfo task = new downloadInfo();
            task.execute("https://api.openweathermap.org/data/2.5/weather?q="+cityName+"&appid=3adbba153e556b8acd65cdc12de4e649");
        }
        else if (there_exists(new String[]{"tell me a joke","joke","riddle","laugh","smile","happy"}))
        {
            downloadJoke task = new downloadJoke();
            task.execute("https://official-joke-api.appspot.com/random_joke");
        }else if (there_exists(new String[]{"mail"}))
        {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"jon@example.com"}); // recipients
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Email subject");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message text");
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://path/to/email/attachment"));
            startActivity(emailIntent);
            //startActivity(Intent.createChooser(emailIntent, "Choose an Email client :"));
        }
        else if (there_exists(new String[]{"Google","google"}))
        {
            String search = t.substring(t.indexOf(" "));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.google.com/search?q="+search));
            startActivity(intent);
        }
        else if (there_exists(new String[]{"yes","ya","ok"}))
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://en.wikipedia.org/wiki/"+title));
            startActivity(intent);
        }
        //WHAT IS YOUR NAME
        else if (there_exists(arr = new String[]{"what is your name", "what's your name", "tell me your name"})) {
            textToSpeech.speak("My name is Insperon.", TextToSpeech.QUEUE_FLUSH, null, null);
            hashMap.put("Name", "Insperon");
            hashMap.put("Message", "My name is Insperon.");
            messagesList.add(hashMap);
        }
        //CAN I CALL YOU BY SOME OTHER NAME
        else if (there_exists(arr = new String[]{"can i call you by some other name", "can i call you siri", "can i call you alexa", "can i call you cortana"})) {
            textToSpeech.speak("My name is Insperon and I wish to be addressed so!", TextToSpeech.QUEUE_FLUSH, null, null);
            hashMap.put("Name", "Insperon");
            hashMap.put("Message", "My name is Insperon and I wish to be addressed so!");
            messagesList.add(hashMap);
        }
        //WHAT CAN YOU DO
        else if (there_exists(new String[]{"what can you do", "what all can you do", "what stuff can you do"})) {
            textToSpeech.speak(
                    "Starting with, I can respond to your basic questions.\n" +
                            "Tell you the time, day, date.\n" +
                            "I can search the web, show the amazing information on Wikipedia, and open the map for the desired location.\n" +
                            "Are you wondering if I can open YouTube, Instagram, Facebook, Twitter, Snapchat, Pinterest, LinkedIn, Quora, or Netflix for you?\n" +
                            "Yes! I can even do that. Want to book a movie at a theatre or compose that urgent mail?\n" +
                            "No worries, you are just one command away from doing it.\n" +
                            "I am also able to help you in opening the calendar, translating what you don't know, and tell you the weather updates if you don't wish to carry your umbrella.\n" +
                            "And how can I not tell jokes or sing a song for you ?!", TextToSpeech.QUEUE_FLUSH, null, null);
            hashMap.put("Name", "Insperon");
            hashMap.put("Message", "Starting with, I can respond to your basic questions.\n" +
                    "Tell you the time, day, date.\n" +
                    "I can search the web, show the amazing information on Wikipedia, and open the map for the desired location.\n" +
                    "Are you wondering if I can open YouTube, Instagram, Facebook, Twitter, Snapchat, Pinterest, LinkedIn, Quora, or Netflix for you?\n" +
                    "Yes! I can even do that. Want to book a movie at a theatre or compose that urgent mail?\n" +
                    "No worries, you are just one command away from doing it.\n" +
                    "I am also able to help you in opening the calendar, translating what you don't know, and tell you the weather updates if you don't wish to carry your umbrella.\n" +
                    "And how can I not tell jokes or sing a song for you ?!");
            messagesList.add(hashMap);
            //CONFESSION
        } else if (there_exists(arr = new String[]{"i love you", "love you", "i like you", "i have feelings for you", "like you"})) {
            textToSpeech.speak("Thanks but I don't.", TextToSpeech.QUEUE_FLUSH, null, null);
            hashMap.put("Name", "Insperon");
            hashMap.put("Message", "Thanks but I don't.");
            messagesList.add(hashMap);
            //PROPOSAL
        } else if (there_exists(arr = new String[]{"marry me", "be my girlfriend", "be my wife"})) {
            textToSpeech.speak("Sorry, I have got better things to do.", TextToSpeech.QUEUE_FLUSH, null, null);
            hashMap.put("Name", "Insperon");
            hashMap.put("Message", "Sorry, I have got better things to do.");
            messagesList.add(hashMap);
        }
        //COMPLIMENT
        else if (there_exists(arr = new String[]{"well done", "good job", "thanks", "thank you", "keep it up"})) {
            textToSpeech.speak("The pleasure is all mine "+uname+" !", TextToSpeech.QUEUE_FLUSH, null, null);
            hashMap.put("Name", "Insperon");
            hashMap.put("Message", "The pleasure is all mine "+uname+" !");
            messagesList.add(hashMap);
        }
        //GOODBYE
        else if (there_exists(arr = new String[]{"exit", "goodbye", "quit", "bye"})) {
            textToSpeech.speak("Bye "+uname+ " have a great day.", TextToSpeech.QUEUE_FLUSH, null, null);
            hashMap.put("Name", "Insperon");
            hashMap.put("Message", "Bye "+uname+" have a great day.");
            messagesList.add(hashMap);
            //WHERE ARE YOU FROM
        } else if (there_exists(arr = new String[]{"where are you from"})) {
            textToSpeech.speak("I from Mumbai, India.", TextToSpeech.QUEUE_FLUSH, null, null);
            hashMap.put("Name", "Insperon");
            hashMap.put("Message", "I from Mumbai, India.");
            messagesList.add(hashMap);
            //WHERE ARE YOU
        } else if (there_exists(arr = new String[]{"where are you"})) {
            textToSpeech.speak("I am currently in your device.", TextToSpeech.QUEUE_FLUSH, null, null);
            hashMap.put("Name", "Insperon");
            hashMap.put("Message", "I am currently in your device.");
            messagesList.add(hashMap);
            //HOW ARE YOU
        }
        else if(there_exists(arr=new String[]{"how do you do","how are you"})){
            String answer = "Fabulous. I wonder how many people even listen to the answer of this question. Whatsoever, thanks for asking and I hope you are doing good!";
            textToSpeech.speak(answer,TextToSpeech.QUEUE_FLUSH,null,null);
            hashMap.put("Name","Insperon");
            hashMap.put("Message",answer);
            messagesList.add(hashMap);
        }
        //WHAT ARE YOU DOING
         else if (there_exists(arr = new String[]{"what are you doing"})) {
            textToSpeech.speak("Listening to you and answering your questions. Gotta catchup with Cortana and Siri after this.", TextToSpeech.QUEUE_FLUSH, null, null);
            hashMap.put("Name", "Insperon");
            hashMap.put("Message", "Listening to you and answering your questions. Gotta catchup with Cortana and Siri after this.");
            messagesList.add(hashMap);
        }
        //NO
        else if (there_exists(arr = new String[]{"no", "nah"})) {
            textToSpeech.speak("Okay never mind.", TextToSpeech.QUEUE_FLUSH, null, null);
            hashMap.put("Name", "Insperon");
            hashMap.put("Message", "Okay never mind.");
            messagesList.add(hashMap);
        }
        //-----------------------------INSTALLED APPS & SOCIAL MEDIA-----------------------------//
        else if (there_exists(new String[]{"open"}))
        {
            appName = (t.substring(t.indexOf(" "))).trim();
            if (appName.equals("whatsapp")) {
                String answer = "Directing you to WhatsApp";
                boolean isAppInstalled = appInstalled("com.whatsapp");
                if (isAppInstalled) {
                    textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            } else if (appName.equals("instagram") || appName.equals("insta")) {
                String answer = "Directing you to Instagram";
                boolean isAppInstalled = appInstalled("com.instagram.android");
                if (isAppInstalled) {
                    textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            } else if (appName.equals("facebook") || appName.equals("fb")) {
                String answer = "Directing you to Facebook";
                boolean isAppInstalled = appInstalled("com.facebook.katana");
                if (isAppInstalled) {
                    textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.facebook.katana");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            } else if (appName.equals("youtube") || appName.equals("yt")) {
                String answer = "Directing you to YouTube";
                boolean isAppInstalled = appInstalled("com.google.android.youtube");
                if (isAppInstalled) {
                    textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            } else if (appName.equals("linkedin")) {
                String answer = "Directing you to LinkedIn";
                boolean isAppInstalled = appInstalled("com.linkedin.android");
                if (isAppInstalled) {
                    textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.linkedin.android");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            } else if (appName.equals("snapchat")) {
                String answer = "Directing you to Snapchat";
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                boolean isAppInstalled = appInstalled("com.snapchat.android");
                if (isAppInstalled) {
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.snapchat.android");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            } else if (appName.equals("twitter")) {
                String answer = "Directing you to Twitter";
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                boolean isAppInstalled = appInstalled("com.twitter.android");
                if (isAppInstalled) {
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.twitter.android");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            } else if (appName.equals("mail") || appName.equals("gmail")) {
                String answer = "Directing you to Gmail";
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                boolean isAppInstalled = appInstalled("com.google.android.gm");
                if (isAppInstalled) {
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.google.android.gm");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            } else if (appName.equals("quora")) {
                String answer = "Directing you to Quora";
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                boolean isAppInstalled = appInstalled("com.quora.android");
                if (isAppInstalled) {
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.quora.android");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            } else if (appName.equals("pinterest")) {
                String answer = "Directing you to Pinterest";
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                boolean isAppInstalled = appInstalled("com.pinterest");
                if (isAppInstalled) {
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.pinterest");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            } else if (appName.equals("bookmyshow")) {
                String answer = "Directing you to BookMyShow";
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                boolean isAppInstalled = appInstalled("com.bt.bms");
                if (isAppInstalled) {
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.bt.bms");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            } else if (appName.equals("netflix")) {
                String answer = "Directing you to Netflix";
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                boolean isAppInstalled = appInstalled("com.netflix.mediaclient");
                if (isAppInstalled) {
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.netflix.mediaclient");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            } else if (appName.equals("telegram")) {
                String answer = "Directing you to Telegram";
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                boolean isAppInstalled = appInstalled("org.telegram.messenger");
                if (isAppInstalled) {
                    Intent i = getPackageManager().getLaunchIntentForPackage("org.telegram.messenger");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            } else if (appName.equals("uber")) {
                String answer = "Directing you to Uber";
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                boolean isAppInstalled = appInstalled("com.ubercab");
                if (isAppInstalled) {
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.ubercab");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);

                }
            } else if (appName.equals("ola")) {
                String answer = "Directing you to Ola";
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                boolean isAppInstalled = appInstalled("com.olacabs.customer");
                if (isAppInstalled) {
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.olacabs.customer");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);

                }
            }
            else if (appName.equals("amazon")) {
                String answer = "Directing you to Amazon";
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                boolean isAppInstalled = appInstalled("in.amazon.mShop.android.shopping");
                if (isAppInstalled) {
                    Intent i = getPackageManager().getLaunchIntentForPackage("in.amazon.mShop.android.shopping");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMapf.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            }else if (appName.equals("spotify")) {
                String answer = "Directing you to Spotify";
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                boolean isAppInstalled = appInstalled("com.spotify.music");
                if (isAppInstalled) {
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.spotify.music");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            }else if (appName.equals("times of india")||appName.equals("toi"))
            {
                String answer = "Directing you to Times of India";
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                boolean isAppInstalled = appInstalled("com.toi.reader.activities");
                if (isAppInstalled) {
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.toi.reader.activities");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            }else if (appName.equals("google news")||appName.equals("news")) {
                String answer = "Directing you to Google News";
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                boolean isAppInstalled = appInstalled("com.google.android.apps.magazines");
                if (isAppInstalled) {
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.magazinesr");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            }else if (appName.equals("google calendar")||appName.equals("calendar"))
            {
                String answer = "Directing you to Google Calendar";
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                boolean isAppInstalled = appInstalled("com.google.android.calendar");
                if (isAppInstalled) {
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.google.android.calendar");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it or open through the url?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            }else if (appName.equals("pay")||appName.equals("google pay"))
            {
                String answer = "Directing you to Google Pay";
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                boolean isAppInstalled = appInstalled("com.google.android.apps.nbu.paisa.user");
                if (isAppInstalled) {
                    Intent i = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.nbu.paisa.user");
                    startActivity(i);
                } else {
                    textToSpeech.speak("The application is not installed! Do you want to install it?", TextToSpeech.QUEUE_FLUSH, null, null);
                    hashMap.put("Name", "Insperon");
                    hashMap.put("Message", "The application is not installed! Do you want to install it or open through the url?");
                    messagesList.add(hashMap);
                }
            }
        }else if (there_exists(new String[]{"install"}))
        {
            if (appName.equals("whatsapp"))
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.whatsapp")));
            }
            else if (appName.equals("instagram") || appName.equals("insta"))
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.instagram.android")));
            }
            else if (appName.equals("facebook") || appName.equals("fb"))
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.facebook.katana"))); }
            else if (appName.equals("youtube") || appName.equals("yt")){
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.youtube&hl=en_IN")));
            }
             else if (appName.equals("linkedin")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.linkedin.android&hl=en_IN")));
            } else if (appName.equals("snapchat")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.snapchat.android&hl=en_IN")));
            } else if (appName.equals("twitter")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.twitter.android&hl=en_IN")));
            } else if (appName.equals("mail") || appName.equals("gmail")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gm&hl=en_IN")));
            } else if (appName.equals("quora")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.quora.android&hl=en_IN")));
            } else if (appName.equals("pinterest")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.pinterest&hl=en_IN")));
            } else if (appName.equals("bookmyshow")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.bt.bms&hl=en_IN")));
            } else if (appName.equals("netflix")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.netflix.mediaclient&hl=en_IN")));
            } else if (appName.equals("telegram")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.telegram.messenger&hl=en_IN")));
            } else if (appName.equals("uber")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.ubercab&hl=en_IN")));
            } else if (appName.equals("ola")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.olacabs.customer&hl=en_IN")));
            }
            else if (appName.equals("amazon")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=in.amazon.mShop.android.shopping&hl=en_IN")));
            }else if (appName.equals("spotify")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music&hl=en_IN")));
            }else if (appName.equals("times of india")||appName.equals("toi"))
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.toi.reader.activities&hl=en_IN")));
            }else if (appName.equals("google news")||appName.equals("news")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.magazines&hl=en_IN")));
            }else if (appName.equals("google calendar")||appName.equals("calendar"))
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.calendar&hl=en_IN")));
            }else if (appName.equals("pay")||appName.equals("google pay"))
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.nbu.paisa.user&hl=en_IN")));
            }
        }
        else if (there_exists(new String[]{"website","url"}))
        {
            if (appName.equals("whatsapp"))
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.whatsapp")));
            }
            else if (appName.equals("instagram") || appName.equals("insta"))
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/?hl=en")));
            }
            else if (appName.equals("facebook") || appName.equals("fb"))
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/")));
            }
            else if (appName.equals("youtube") || appName.equals("yt")){
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/")));
            }
            else if (appName.equals("linkedin")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/feed/")));
            } else if (appName.equals("snapchat")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.snapchat.com/l/en-gb/")));
            } else if (appName.equals("twitter")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/?lang=en")));
            } else if (appName.equals("mail") || appName.equals("gmail")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com/mail/u/0/#inbox")));
            } else if (appName.equals("quora")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.quora.com/")));
            } else if (appName.equals("pinterest")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.pinterest.ca/")));
            } else if (appName.equals("bookmyshow")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://in.bookmyshow.com/")));
            } else if (appName.equals("netflix")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.netflix.com/in/")));
            } else if (appName.equals("telegram")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.org/")));
            } else if (appName.equals("uber")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.uber.com/jp/en/ride/ubertaxi/")));
            } else if (appName.equals("ola")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.olacabs.com/")));
            }
            else if (appName.equals("amazon")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.amazon.in/?ext_vrnc=hi&tag=googinkenshoo-21&ascsubtag=_k_EAIaIQobChMI6qnRpLn16gIVhX4rCh20Xw-ZEAAYASAAEgJedfD_BwE_k_&ext_vrnc=hi&gclid=EAIaIQobChMI6qnRpLn16gIVhX4rCh20Xw-ZEAAYASAAEgJedfD_BwE")));
            }else if (appName.equals("spotify")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.spotify.com/in/")));
            }else if (appName.equals("times of india")||appName.equals("toi"))
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://timesofindia.indiatimes.com/topic/official-website")));
            }else if (appName.equals("google news")||appName.equals("news")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("")));
            }else if (appName.equals("google calendar")||appName.equals("calendar"))
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://calendar.google.com/calendar/r")));
            }else if (appName.equals("pay")||appName.equals("google pay"))
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.nbu.paisa.user&hl=en_IN")));
            }
        }else if (there_exists(new String[]{"call","dial"}))
        {
            String phone = t.substring(t.indexOf(" "));
            if (isString(phone))
            {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+phone));//change the number
                startActivity(callIntent);
            }
            //String phone = getPhoneNumber(name,ChatActivity.this);
        }
        //-----------------------------GENERAL APPS WHICH ARE PRE-INSTALLED-----------------------------//
        //CAMERA
        else if (there_exists(arr = new String[]{"camera"})) {
            String answer = "Directing you to Camera";
            textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
            Intent i = getPackageManager().getLaunchIntentForPackage("com.sec.android.app.camera");
            startActivity(i);
        }
        else if (there_exists(arr = new String[]{"call", "call log"})) {
            String answer = "Directing you to your Call Logs";
            textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
            Intent i = getPackageManager().getLaunchIntentForPackage("com.android.phone");
            startActivity(i);
        }
        else if (there_exists(new String[]{"calendar"})) {
            String answer = "Directing you to Calendar";
            textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
            Intent i = getPackageManager().getLaunchIntentForPackage("com.samsung.android.calendar");
            startActivity(i);
        }
        else if (there_exists(arr = new String[]{"calculator"})) {
            String answer = "Directing you to Calculator";
            textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
            Intent i = getPackageManager().getLaunchIntentForPackage("com.sec.android.app.popupcalculator");
            startActivity(i);
        }
        else if (there_exists(arr = new String[]{"clock", "alarm"})) {
            String answer = "Directing you to Clock and Alarm";
            textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
            Intent i = getPackageManager().getLaunchIntentForPackage("com.sec.android.app.clockpackage");
            startActivity(i);
        }
        else if (there_exists(arr = new String[]{"contact"})) {
            String answer = "Directing you to Contacts";
            textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
            Intent i = getPackageManager().getLaunchIntentForPackage("com.samsung.android.contacts");
            startActivity(i);
        }
        else if (there_exists(arr = new String[]{"voice recorder", "recorder"})) {
            String answer = "Directing you to Voice Recorder";
            textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
            Intent i = getPackageManager().getLaunchIntentForPackage("com.sec.android.app.voicenote");
            startActivity(i);
        }
        else if (there_exists(arr = new String[]{"files", "my files"})) {
            String answer = "Directing you to My Files";
            textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
            Intent i = getPackageManager().getLaunchIntentForPackage("com.sec.android.app.myfiles");
            startActivity(i);
        }
        else if (there_exists(arr = new String[]{"messages"})) {
            String answer = "Directing you to Messages";
            textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
            Intent i = getPackageManager().getLaunchIntentForPackage("com.samsung.android.messaging");
            startActivity(i);
        }
        else if (there_exists(arr = new String[]{"playstore", "playstore", "store"})) {
            String answer = "Directing you to Play Store";
            textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
            Intent i = getPackageManager().getLaunchIntentForPackage("com.android.vending");
            startActivity(i);
        }
        else if (there_exists(arr = new String[]{"settings", "setting"})) {
            String answer = "Directing you to Settings";
            textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
            Intent i = getPackageManager().getLaunchIntentForPackage("com.android.settings");
            startActivity(i);
        }
        //WE DID NOT GET YOU
        else {
            textToSpeech.speak("We did not get it can you try again?", TextToSpeech.QUEUE_FLUSH, null, null);
            hashMap.put("Name", "Insperon");
            hashMap.put("Message", "We didn't get it can you try again?");
            messagesList.add(hashMap);
        }
        simpleAdapter.notifyDataSetChanged();//notify that something is change and update that
    }

    public boolean there_exists(String[] str) {
        for (String wer : str) {
            if (t.contains(wer)) {
                return true;
            }
        }
        return false;
    }

    public boolean isString(String str)
    {
        try {
            int s = Integer.parseInt(str);
        }catch (Exception e)
        {
            return false;
        }
        return true;
    }

    public String getPhoneNumber(String name, Context context) {
        String ret = null;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + name +"%'";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);
        if (c.moveToFirst()) {
            ret = c.getString(0);
        }
        c.close();
        if(ret==null)
            ret = "Unsaved";
        return ret;
    }
    private boolean appInstalled(String s) {
        PackageManager packageManager = getPackageManager();
        try {
            packageManager.getPackageInfo(s, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}
