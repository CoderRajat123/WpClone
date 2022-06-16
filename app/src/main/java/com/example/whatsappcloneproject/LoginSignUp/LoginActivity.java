package com.example.whatsappcloneproject.LoginSignUp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.whatsappcloneproject.MainActivity;
import com.example.whatsappcloneproject.R;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    TextView needanewAccount, forgotPassword;
    Button phonenumberLogIn, LogIn;
    EditText email, password;
    DatabaseReference UserRef;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Initialize();
        progressDialog=new ProgressDialog(this);
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        needanewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendusertoregActivity();
            }
        });
        phonenumberLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendusertophoneActivity();
            }
        });
        LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserLogIn();
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLinkToMail();
            }
        });
    }

    private void sendLinkToMail() {
        if(email.getText().toString().matches("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$") && email.getText().toString().length()>8)
        {
            AlertDialog.Builder passwordreset=new AlertDialog.Builder(this);
            passwordreset.setTitle(" Reset Password ?");
            passwordreset.setMessage("Press Yes to receive the reset link");
            passwordreset.setPositiveButton("Yes",(dialogInterface, i) ->
            {
               String resetEmail=email.getText().toString();
               auth.sendPasswordResetEmail(resetEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                       Toast.makeText(getApplicationContext(),"Reset Email Link has been send to your email Id",Toast.LENGTH_SHORT).show();
                   }
               });
            });
            passwordreset.setNegativeButton("No",(dialogInterface, i) -> {});
            passwordreset.create().show();
        }
        else
        {
            email.setError("Please enter a Valid Email ");
        }
    }

    private void AllowUserLogIn() {
        String userEmail = email.getText().toString();
        String userPass = password.getText().toString();
        if (TextUtils.isEmpty(userEmail)) {
            email.setError("please enter email id");
        }
        if (TextUtils.isEmpty(userPass)) {
            password.setError("please enter password");
        }
        if (TextUtils.isEmpty(userEmail) && TextUtils.isEmpty(userPass)) {
            email.setError("please enter email id ");
            password.setError("please enter password");
        }
        if (!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPass)) {
            progressDialog.setTitle("Signing In");
            progressDialog.setMessage("please wait,while we are logging into your account ...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            auth.signInWithEmailAndPassword(userEmail,userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        String deviceToken= FirebaseInstanceId.getInstance().getToken();
                        String currentUserId = auth.getCurrentUser().getUid();
                        UserRef.child(currentUserId).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                SendUserToMainActivity();
                                Toast.makeText(getApplicationContext(), "Logged In Successfully ", Toast.LENGTH_SHORT).show();
                            }
                        });;

                    }
                    else
                    {
                        String message= task.getException().getLocalizedMessage();
                        Toast.makeText(getApplicationContext(), "Error : "+message, Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.cancel();
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void SendusertoregActivity() {
            Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
    }
    private void SendusertophoneActivity() {
        Intent intent=new Intent(LoginActivity.this,PhNoLoginActivity.class);
        startActivity(intent);
    }
    private void SendUserToMainActivity() {
        Intent intent=new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void Initialize() {

        needanewAccount=findViewById(R.id.needanewAccount);
        phonenumberLogIn=findViewById(R.id.phone_number_login);
        email= findViewById(R.id.login_email);
        password=findViewById(R.id.login_password);
        LogIn= findViewById(R.id.login_btn);
        forgotPassword= findViewById(R.id.forgot_Password);

        auth= FirebaseAuth.getInstance();
    }
}