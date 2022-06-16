package com.example.whatsappcloneproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    String receivedUserId, sendUserId, Current_state;
    CircleImageView userProfileImg;
    TextView userProfileName, userProStatus;
    FirebaseAuth auth;
    String image;
    Button DeclineReq, SendMsgReq;
    DatabaseReference RootRef, ChatReqRef, ContactRef, NotificationRef;
    StorageReference userProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        receivedUserId = getIntent().getExtras().getString("Visited_uid");
        Initialize();
        RetriveData();

    }

    private void RetriveData() {
        RootRef.child("Users").child(receivedUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    if(snapshot.hasChild("name")&& snapshot.hasChild("status")&& !snapshot.hasChild("image"))
                    {
                        String uname=snapshot.child("name").getValue().toString();
                        String ustatus=snapshot.child("status").getValue().toString();
                        userProfileName.setText(uname);
                        userProStatus.setText(ustatus);

                    }
                    else if(snapshot.hasChild("name")&& snapshot.hasChild("status")&& snapshot.hasChild("image"))
                    {
                        image=snapshot.child("image").getValue().toString();
                        String uname=snapshot.child("name").getValue().toString();
                        String ustatus=snapshot.child("status").getValue().toString();
                        GetImage(receivedUserId,userProfileImg);
                        userProfileName.setText(uname);
                        userProStatus.setText(ustatus);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Please Update your Profile", Toast.LENGTH_SHORT).show();
                    }
                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequest() {
          ChatReqRef.child(sendUserId)
                  .addValueEventListener(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot snapshot) {
                          if(snapshot.hasChild(receivedUserId)) {
                              String request_type = snapshot.child(receivedUserId).child("request_type")
                                      .getValue().toString();
                              if (request_type.equals("sent")) {
                                  Current_state = "request_sent";
                                  SendMsgReq.setText("Cancel Chat Request");
                              } else if (request_type.equals("received")) {
                                  Current_state = "request_received";
                                  SendMsgReq.setText("Accept Chat Request");
                                  DeclineReq.setVisibility(View.VISIBLE);
                                  DeclineReq.setEnabled(true);
                                  DeclineReq.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View view) {
                                          CancelRequestSent();
                                      }
                                  });
                              }
                          }
                              else {
                                  ContactRef.child(sendUserId)
                                          .addListenerForSingleValueEvent(new ValueEventListener() {
                                              @Override
                                              public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                  if(snapshot.hasChild(receivedUserId)) {
                                                      Current_state = "friends";
                                                      SendMsgReq.setText("Remove this Contact");
                                                  }
                                              }

                                              @Override
                                              public void onCancelled(@NonNull DatabaseError error) {

                                              }
                                          });
                              }
                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError error) {

                      }
                  });
          if(!sendUserId.equals(receivedUserId))
          {
              SendMsgReq.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View view) {
                      SendMsgReq.setEnabled(false);
                      if(Current_state.equals("new"))
                      {
                          SendChatReq();
                      }
                      if(Current_state.equals("request_sent"))
                      {
                          CancelRequestSent();
                      }
                      if(Current_state.equals("request_received"))
                      {
                          AcceptChatRequest();
                      }
                      if(Current_state.equals("friends"))
                      {
                          RemoveSpecificContact();
                      }
                  }
              });
          }
          else
          {
              SendMsgReq.setVisibility(View.INVISIBLE);
          }
    }

    private void RemoveSpecificContact() {
        ContactRef.child(sendUserId).child(receivedUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            SendMsgReq.setEnabled(true);
                            Current_state="new";
                            SendMsgReq.setText("Send Message");
                            DeclineReq.setVisibility(View.INVISIBLE);
                            DeclineReq.setEnabled(false);
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        ContactRef.child(sendUserId).child(receivedUserId)
                .child("Contacts")
                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    ContactRef.child(receivedUserId).child(sendUserId)
                            .child("Contacts")
                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                ChatReqRef.child(sendUserId).child(receivedUserId)
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()) {
                                                    ChatReqRef.child(receivedUserId).child(sendUserId)
                                                            .removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()) {
                                                                        SendMsgReq.setEnabled(true);
                                                                        Current_state = "friends";
                                                                        SendMsgReq.setText("Remove this Contact");
                                                                        DeclineReq.setVisibility(View.INVISIBLE);
                                                                        DeclineReq.setEnabled(false);
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        });
                            }
                        }
                    });
                }
            }
        });
    }

    private void SendChatReq() {
        ChatReqRef.child(sendUserId).child(receivedUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ChatReqRef.child(receivedUserId).child(sendUserId)
                                .child("request_type").setValue("received")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful())
                                                        {
                                                            HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                            chatNotificationMap.put("from",sendUserId);
                                                            chatNotificationMap.put("type","request");

                                                            NotificationRef.child(receivedUserId).push()
                                                                    .setValue(chatNotificationMap)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()) {
                                                                                SendMsgReq.setEnabled(true);
                                                                                Current_state = "request_sent";
                                                                                SendMsgReq.setText(R.string.cancelchatreq);
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                    }
                                });
                    }
                });
    }

    private void CancelRequestSent() {
        ChatReqRef.child(sendUserId).child(receivedUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            ChatReqRef.child(receivedUserId).child(sendUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                SendMsgReq.setEnabled(true);
                                                Current_state = "new";
                                                SendMsgReq.setText("Send Message");
                                                DeclineReq.setVisibility(View.INVISIBLE);
                                                DeclineReq.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void Initialize() {
        auth = FirebaseAuth.getInstance();
        sendUserId=auth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        ChatReqRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef= FirebaseDatabase.getInstance().getReference().child("Notifications");
        userProfileName=findViewById(R.id.user_name_profileActivity);
        userProStatus=findViewById(R.id.user_status_profileActivity);
        SendMsgReq=findViewById(R.id.Send_Message);
        DeclineReq=findViewById(R.id.Cancel_req_btn);
        userProfileImg=findViewById(R.id.profile_image_activity);
        Current_state="new";
    }
    private void GetImage(String currentUser, CircleImageView imageView) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("Profile Images/" + currentUser + ".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(imageView);
            }
        });
    }
}
