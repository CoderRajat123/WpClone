package com.example.whatsappcloneproject.helper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappcloneproject.ImageViewActivity;
import com.example.whatsappcloneproject.MainActivity;
import com.example.whatsappcloneproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    List<Messages> userMessageList;
    FirebaseAuth auth;
    DatabaseReference userRef;
    Context context;
    public MessageAdapter( List<Messages> userMessageList,Context context)
    {
        this.userMessageList=userMessageList;
        this.context=context;
    }
    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout, parent, false);
        auth=FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MessageViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String messageSenderId=auth.getCurrentUser().getUid();
        Messages messages=userMessageList.get(position);
        String fromUserId= messages.getFrom();
        String fromMessageType=messages.getType();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.senderMessageTextTd.setVisibility(View.GONE);
        holder.sender.setVisibility(View.GONE);
        holder.senderImageView.setVisibility(View.GONE);
        holder.receiverImageView.setVisibility(View.GONE);
        holder.receiverMessageTextTd.setVisibility(View.GONE);
        holder.receiver.setVisibility(View.GONE);
        holder.receiverMessageText.setVisibility(View.GONE);

        if(fromMessageType.equals("text"))
        {
            if (fromUserId.equals(messageSenderId)) {
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageTextTd.setVisibility(View.VISIBLE);
                holder.sender.setVisibility(View.VISIBLE);
                holder.senderMessageText.setText(messages.getMessage());
                holder.senderMessageTextTd.setText(messages.getTime() + " " + messages.getDate());
                holder.senderImageView.setVisibility(View.GONE);
                holder.receiverImageView.setVisibility(View.GONE);
            }
            else
            {
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverMessageTextTd.setVisibility(View.VISIBLE);
                holder.receiver.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setText(messages.getMessage());
                holder.receiverMessageTextTd.setText(messages.getTime() + " " + messages.getDate());
                holder.senderImageView.setVisibility(View.GONE);
                holder.receiverImageView.setVisibility(View.GONE);
            }
        }
        if(fromMessageType.equals("image"))
        {
            if(fromUserId.equals(messageSenderId)) {
                holder.senderImageView.setVisibility(View.VISIBLE);
                holder.receiverImageView.setVisibility(View.GONE);
                GetImage(messages.getMessageID(),holder.senderImageView);
            }
            else
            {
                holder.receiverImageView.setVisibility(View.VISIBLE);
                holder.senderImageView.setVisibility(View.GONE);
                GetImage(messages.getMessageID(),holder.receiverImageView);
            }
        }
        if(fromMessageType.equals("pdf")||fromMessageType.equals("docx"))
        {
            if(fromUserId.equals(messageSenderId)) {
                holder.senderImageView.setVisibility(View.VISIBLE);
                holder.senderImageView.setBackgroundResource(R.drawable.ic_baseline_insert_drive_file_24);
                holder.receiverImageView.setVisibility(View.GONE);
            }
            else
            {
                holder.receiverImageView.setVisibility(View.VISIBLE);
                holder.receiverImageView.setBackgroundResource(R.drawable.ic_baseline_insert_drive_file_24);
                holder.senderImageView.setVisibility(View.GONE);
            }
        }
        if(fromUserId.equals(messageSenderId))
        {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(messages.getType().equals("pdf")||messages.getType().equals("docx"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and View this Document",
                                        "Cancel",
                                        "Delete for EveryOne"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0)
                                {
                                    deleteSendMessageForMe(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if(i==1)
                                {
                                   openFile(messages.getMessageID(),messages.getType());
                                }
                                if(i==3)
                                {
                                    deleteMessageForEveryOne(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    if(messages.getType().equals("text"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel",
                                        "Delete for EveryOne"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0)
                                {
                                    deleteSendMessageForMe(position, holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if(i==2)
                                {
                                    deleteMessageForEveryOne(position, holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    if(messages.getType().equals("image"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "View This Image",
                                        "Cancel",
                                        "Delete for EveryOne"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0)
                                {
                                    deleteSendMessageForMe(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if(i==1)
                                {
                                    Intent intent=new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    intent.putExtra("messageId",messages.getMessageID());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if(i==3)
                                {
                                    deleteMessageForEveryOne(position, holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
        else
        {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(messages.getType().equals("pdf")||messages.getType().equals("docx"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and View this Document",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0)
                                {
                                    deleteReceiverMessageForMe(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if(i==1)
                                {
                                   openFile(messages.getMessageID(),messages.getType());
                                }
                            }
                        });
                        builder.show();
                    }
                    if(messages.getType().equals("text"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0)
                                {
                                    deleteReceiverMessageForMe(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    if(messages.getType().equals("image"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "View This Image",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0)
                                {
                                    deleteReceiverMessageForMe(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if(i==1)
                                {
                                    Intent intent=new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    intent.putExtra("messaggeId",messages.getMessageID());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }
    private void deleteSendMessageForMe(int position, MessageViewHolder holder) {
        DatabaseReference RootRef=FirebaseDatabase.getInstance().getReference();
        RootRef.child("Messages")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageID())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(holder.itemView.getContext(), "Message Deleted Successfully", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deleteReceiverMessageForMe(int position, MessageViewHolder holder) {
        DatabaseReference RootRef=FirebaseDatabase.getInstance().getReference();
        RootRef.child("Messages")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageID())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(holder.itemView.getContext(), "Message Deleted Successfully", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deleteMessageForEveryOne(int position, MessageViewHolder holder) {
        DatabaseReference RootRef=FirebaseDatabase.getInstance().getReference();
        RootRef.child("Messages")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageID())
                .removeValue();
        RootRef.child("Messages")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageID())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(holder.itemView.getContext(), "Message Deleted Successfully", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void openFile(String messageID, String type) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child("Document Files/" + messageID+"."+type).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if(type.equals("pdf"))
                {
                    Intent intent=new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uri,"application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Intent in=Intent.createChooser(intent,"open file");
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(in);
                }
                else
                {
                    String[] mimetypes={"application/vnd.openxmlformats-officedocument.wordprocessingml.document","application/msword"};
                    Intent intent=new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uri,"*/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES,mimetypes);
                    Intent in=Intent.createChooser(intent, "open file");
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(in);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return userMessageList.size();
    }
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText;
        public TextView senderMessageTextTd,receiverMessageTextTd;
        public LinearLayout sender,receiver;
        public ImageView senderImageView,receiverImageView;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            senderMessageText = itemView.findViewById(R.id.sender_messages);
            receiverMessageText = itemView.findViewById(R.id.receiver_messages);
            senderMessageTextTd= itemView.findViewById(R.id.sender_messages_td);
            receiverMessageTextTd=itemView.findViewById(R.id.receiver_messages_td);
            sender=itemView.findViewById(R.id.lls);
            receiver=itemView.findViewById(R.id.llr);
            senderImageView=itemView.findViewById(R.id.messager_sender_image_view);
            receiverImageView=itemView.findViewById(R.id.receiver_sender_image_view);
        }
    }
    private void GetImage(String currentUser, ImageView imageView) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("Image Files/" + currentUser + ".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(imageView);
            }
        });
    }
}
