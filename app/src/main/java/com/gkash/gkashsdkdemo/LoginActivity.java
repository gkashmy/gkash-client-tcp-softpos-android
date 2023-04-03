package com.gkash.gkashsdkdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginBtn = findViewById(R.id.loginBtn);
        EditText usernameET = findViewById(R.id.usernameET);
        EditText passwordET = findViewById(R.id.passwordET);
        CheckBox envCB = findViewById(R.id.envCB);
        loginBtn.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("username", usernameET.getText().toString());
            intent.putExtra("password", passwordET.getText().toString());
            intent.putExtra("env", envCB.isChecked());
            startActivity(intent);
        });
    }
}