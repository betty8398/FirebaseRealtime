package com.example.firebasedata;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private Context context;
    private EditText editText_email, editText_password;
    private Button button_login, button_logout, button_cancel, button_register;
    private TextView textView_result;
    private Switch switch_pass;
    private FirebaseAuth authControl;
    private String TAG = "login";
    private String email, password;

    private void setView() {
        editText_email = findViewById(R.id.editText_email);
        editText_password = findViewById(R.id.editText_password);
        button_login = findViewById(R.id.button_login);
        button_logout = findViewById(R.id.button_logout);
        button_cancel = findViewById(R.id.button_cancel);
        button_register = findViewById(R.id.button_register);
        textView_result = findViewById(R.id.textView_result);
        switch_pass = findViewById(R.id.switch_pass);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");
        setView();
        context = this;

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        switch_pass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switch_pass.setText("On");
                    editText_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    switch_pass.setText("Off");
                    editText_password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                }
            }
        });

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText_email.setText("");
                editText_password.setText("");
            }
        });

        button_login.setOnClickListener(new MyButton());
        button_logout.setOnClickListener(new MyButton());
        button_register.setOnClickListener(new MyButton());

        //FirebaseAuth 實體
        authControl = FirebaseAuth.getInstance();
        Log.d(TAG, "authControl = " + authControl);

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyButton implements View.OnClickListener {
        private FirebaseUser currentUser;

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_login:
                    if (editText_email.length() == 0 || editText_password.length() == 0) {
                        Toast.makeText(context, "please input your email and password", Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        email = editText_email.getText().toString();
                        password = editText_password.getText().toString();
                        //檢查有沒有user在線上 有的話登出 (一次只能一個人登入firebase)
                        currentUser = authControl.getCurrentUser();
                        if (currentUser != null) {
                            authControl.signOut();
                        }
                        //登入firebase 參數帶入帳號密碼
                        authControl.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() { //設立登入完成的監聽器
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "login ok");
                                            FirebaseUser user = authControl.getCurrentUser();
                                            DisplayUser(user);
                                        } else {
                                            Log.d(TAG, "login fail");
                                            textView_result.setText("Login fail");
                                        }
                                    }
                                });
                    }
                    break;
                case R.id.button_logout:
                    currentUser = authControl.getCurrentUser();
                    if(currentUser!=null){
                        textView_result.setText(currentUser.getEmail()+" is logout");
                        editText_email.setText("");
                        editText_password.setText("");
                        authControl.signOut();
                    }

                    break;
                case R.id.button_register:
                    if (editText_email.length() == 0 || editText_password.length() == 0) {
                        Toast.makeText(context, "please input your email and password", Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        email = editText_email.getText().toString();
                        password = editText_password.getText().toString();

                        authControl.createUserWithEmailAndPassword(email,password).addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Register ok");
                                    FirebaseUser user = authControl.getCurrentUser();
                                    DisplayUser(user);
                                } else {
                                    Log.d(TAG, "Register fail");
                                    textView_result.setText("Register fail");
                                }
                            }
                        });




                    }

                    break;

            }
        }
    }

    private void DisplayUser(FirebaseUser user) {
        String name = user.getDisplayName();
        String email = user.getEmail();
        String UID = user.getUid();

        textView_result.setText("name = " + name);
        textView_result.append("email = " + email);
        textView_result.append("UID = " + UID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        authControl.signOut();
    }
}