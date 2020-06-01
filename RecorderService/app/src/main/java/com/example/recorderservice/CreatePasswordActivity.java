package com.example.recorderservice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreatePasswordActivity extends AppCompatActivity {

    EditText editText1, editText2, oldPassword;
    Button button, hide;
    String oldPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_password);

        editText1 = (EditText) findViewById(R.id.editText1);
        editText2 = (EditText) findViewById(R.id.editText2);
        oldPassword = (EditText) findViewById(R.id.oldPassword);
        button = (Button) findViewById(R.id.button);
        hide = (Button) findViewById(R.id.hide);

        final SharedPreferences settings = getSharedPreferences("PREFS", 0);
        oldPass = settings.getString("password", "");

        if (oldPass.equals("")) {
            oldPassword.setVisibility(View.INVISIBLE);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text1 = editText1.getText().toString();
                String text2 = editText2.getText().toString();
                String text3 = oldPassword.getText().toString();

                if(!oldPass.equals("") && !oldPass.equals(text3)){
                    Toast.makeText(CreatePasswordActivity.this, "Old Password Incorrect", Toast.LENGTH_LONG).show();
                }

                else if (text1.equals("") || text2.equals("")) {
                    Toast.makeText(CreatePasswordActivity.this, "Password not entered", Toast.LENGTH_SHORT).show();
                } else if (text1.equals(text2)) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("password", text1);
                    editor.apply();

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(CreatePasswordActivity.this, "Password not matched", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

}
