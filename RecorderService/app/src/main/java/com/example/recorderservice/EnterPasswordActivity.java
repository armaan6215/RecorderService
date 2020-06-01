package com.example.recorderservice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class EnterPasswordActivity extends AppCompatActivity {

    EditText editText;
    Button button;
    CheckBox checkBox;
    TextView changePassword, forgotPassword;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_password);

        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        password = settings.getString("password", "");

        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        //checkBox = (CheckBox) findViewById(R.id.checkBox);
        changePassword = (TextView) findViewById(R.id.changePassword);

       /* checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }

                else{
                    editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        }); */

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();

                if(text.equals(password)){
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(EnterPasswordActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), CreatePasswordActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {

    }
}
