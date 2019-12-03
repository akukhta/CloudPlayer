package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.example.myapplication.Usrs.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.SnackbarContentLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

public class MainActivity extends AppCompatActivity {

    Button btnLogIn, btnSignIn;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    RelativeLayout root;
    public static final String LOGGIN = "false";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        SharedPreferences preferences;
        setContentView(R.layout.activity_main);
        btnSignIn = findViewById(R.id.buttonRegister);
        btnLogIn = findViewById(R.id.buttonLogIn);
        SharedPreferences settings = getSharedPreferences(LOGGIN,0);
        boolean login = settings.getBoolean("login",false);
        root = findViewById(R.id.root_start_window);
        if (login == true){
            startActivity(new Intent(MainActivity.this, MusicListActivity.class));
            finish();
        }
        auth = FirebaseAuth.getInstance(); //Начало регистрации
        db = FirebaseDatabase.getInstance(); //Подключение к бд
        users = db.getReference("Users");


        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegister();
            }
        });
        
        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });


    }

    private void login() {
        final AlertDialog.Builder loginDialog = new AlertDialog.Builder(this);
        loginDialog.setTitle("Войти");
        loginDialog.setMessage("Введите данные для авторизации");
        LayoutInflater inflater = LayoutInflater.from(this);
        View loginWindow = inflater.inflate(R.layout.register,null);
        loginDialog.setView(loginWindow);
        final MaterialEditText email = loginWindow.findViewById(R.id.emailField);
        final MaterialEditText password = loginWindow.findViewById(R.id.passField);
        loginDialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        loginDialog.setPositiveButton("Вход", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(email.getText().toString())){
                    Snackbar.make(root,"Введите вашу почту!",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password.getText().toString())){
                    Snackbar.make(root,"Введите ваш пароль!",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        SharedPreferences settings = getSharedPreferences(LOGGIN,0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean("login",true);
                        editor.commit();
                        startActivity(new Intent(MainActivity.this, MusicListActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(root,"Ошбика авторизации!\n" + e.getMessage(),Snackbar.LENGTH_SHORT).show();
                    }
                });

            }
        });
        loginDialog.show();
    }

    private void startRegister() {
        final AlertDialog.Builder registerDialog = new AlertDialog.Builder(this);
        registerDialog.setTitle("Зарегистрироваться");
        registerDialog.setMessage("Введите данные для регистрации");
        LayoutInflater inflater = LayoutInflater.from(this);
        View registerWindow = inflater.inflate(R.layout.register,null);
        registerDialog.setView(registerWindow);
        final MaterialEditText email = registerWindow.findViewById(R.id.emailField);
        final MaterialEditText password = registerWindow.findViewById(R.id.passField);
        registerDialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        registerDialog.setPositiveButton("Готово", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(email.getText().toString())){
                    Snackbar.make(root,"Введите вашу почту!",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password.getText().toString())){
                    Snackbar.make(root,"Введите ваш пароль!",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                //Регистрация пользователя
                auth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User(email.getText().toString(), password.getText().toString());
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(root,"Готово!",Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(root,"Ошибка регистрации!\n" + e.getMessage(),Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
        registerDialog.show();
    }
}
