package dev.frilly.locket.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import dev.frilly.locket.R;
import dev.frilly.locket.model.Post;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {
    private ArrayList<Post> postList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public MediaAdapter(ArrayList<Post> postList, Context context, OnItemClickListener onItemClickListener) {
        this.postList = postList;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, parent, false);
        return new ViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);
        Glide.with(context).load(post.getFileUrl()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(getBindingAdapterPosition());
                }
            });
        }
    }
}