package com.digitalhawks.chatbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    Button go;
    Button switcher;
    TextView email;
    TextView passwd;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email = findViewById(R.id.email);
        passwd = findViewById(R.id.password);
        go = findViewById(R.id.signUp);
        switcher = findViewById(R.id.login);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user!=null){
            tobooks();
        }
    }
    public void action(View view){
        if (check()) {
            mAuth.createUserWithEmailAndPassword(email.getText().toString(), passwd.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user = mAuth.getCurrentUser();
                                assert user != null;
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(), "Check your mail to verify email Id", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(MainActivity.this,InfoActivity.class);
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Not Verified", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                    });
        }
    }

    public void change(View view){
        if (check()) {
            mAuth.signInWithEmailAndPassword(email.getText().toString(), passwd.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user = mAuth.getCurrentUser();
                                assert user != null;
                                if (user.isEmailVerified()) {
                                    tobooks();
                                }else {
                                    user.sendEmailVerification();
                                    Toast.makeText(MainActivity.this,"Your email is not verified..kindly check your mail to verify",Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Toast.makeText(getApplicationContext(),"Invalid password or email-id",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void tobooks(){
        Intent intent = new Intent(MainActivity.this,ChatActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean check(){
        String txt_email = email.getText().toString();
        String txt_passwd = passwd.getText().toString();

        if(TextUtils.isEmpty(txt_email)||TextUtils.isEmpty(txt_passwd))
        {
            Toast.makeText(MainActivity.this,"All Fields are mandatory",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(txt_passwd.length()<6)
        {
            Toast.makeText(MainActivity.this,"Password less than 6",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    public void reset(View view) {
        String txt_email = email.getText().toString();
        if(!TextUtils.isEmpty(txt_email))
        {
            mAuth.sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Check your mail to reset password", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else {
            Toast.makeText(getApplicationContext(),"E-mail is mandatory",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}