package com.example.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.inbox.CTInboxMessage;

import java.util.List;

public class CustomInboxAdapter extends RecyclerView.Adapter<CustomInboxAdapter.InboxViewHolder> {
    private Context context;
    private List<CTInboxMessage> inboxMessages;
    private CleverTapAPI clevertapInstance;

    public CustomInboxAdapter(Context context, List<CTInboxMessage> inboxMessages, CleverTapAPI clevertapInstance) {
        this.context = context;
        this.inboxMessages = inboxMessages;
        this.clevertapInstance = clevertapInstance;
    }

    @NonNull
    @Override
    public InboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.inbox_item_layout, parent, false);
        return new InboxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxViewHolder holder, int position) {
        CTInboxMessage message = inboxMessages.get(position);

        // Set title and message
        holder.titleTextView.setText(message.getTitle());
        holder.messageTextView.setText(message.getBody());

        // Load image
//        String imageUrl = message.getInboxMessageBody().getMedia().get(0).getUrl();
//        Glide.with(context)
//                .load(imageUrl)
//                .placeholder(android.R.drawable.ic_menu_gallery)
//                .error(android.R.drawable.ic_dialog_alert)
//                .into(holder.imageView);

        // Mark message as read when clicked
//        holder.itemView.setOnClickListener(v -> {
//            message.recordMessageClicked();
//            // Optional: Add custom click handling here
//        });
    }

    @Override
    public int getItemCount() {
        return inboxMessages.size();
    }

    static class InboxViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView messageTextView;

        public InboxViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            titleTextView = itemView.findViewById(R.id.title);
            messageTextView = itemView.findViewById(R.id.message);
        }
    }
}