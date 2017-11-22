package com.guoxiaoxing.phoenix.demo;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;
import com.guoxiaoxing.phoenix.core.model.MimeType;
import com.guoxiaoxing.phoenix.picker.util.DateUtils;
import com.guoxiaoxing.phoenix.picker.util.DebugUtil;
import com.guoxiaoxing.phoenix.picker.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    private static final int TYPE_ADD = 1;
    private static final int TYPE_MEDIA = 2;

    private List<MediaEntity> mMediaList = new ArrayList<>();
    private OnAddMediaListener mOnAddMediaListener;

    public interface OnAddMediaListener {
        void onaddMedia();
    }

    public MediaAdapter(OnAddMediaListener mOnAddMediaListener) {
        this.mOnAddMediaListener = mOnAddMediaListener;
    }

    public void setData(List<MediaEntity> mediaList) {
        mMediaList.clear();
        mMediaList.addAll(mediaList);
        notifyDataSetChanged();
    }

    public List<MediaEntity> getData() {
        return mMediaList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPicture;
        LinearLayout llDelete;
        TextView tvDuration;

        ViewHolder(View view) {
            super(view);
            ivPicture = (ImageView) view.findViewById(R.id.ivPicture);
            llDelete = (LinearLayout) view.findViewById(R.id.llDelete);
            tvDuration = (TextView) view.findViewById(R.id.tvDuration);
        }
    }

    @Override
    public int getItemCount() {
        return mMediaList.size() == 0 ? 1 : mMediaList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mMediaList.size() || mMediaList.size() == 0) {
            return TYPE_ADD;
        } else {
            return TYPE_MEDIA;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_media, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        if (getItemViewType(position) == TYPE_ADD) {
            viewHolder.ivPicture.setImageResource(R.drawable.ic_add_media);
            viewHolder.ivPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnAddMediaListener.onaddMedia();
                }
            });
            viewHolder.llDelete.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.llDelete.setVisibility(View.VISIBLE);
            viewHolder.llDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int index = viewHolder.getAdapterPosition();
                    if (index != RecyclerView.NO_POSITION) {
                        mMediaList.remove(index);
                        notifyItemRemoved(index);
                        notifyItemRangeChanged(index, mMediaList.size());
                        DebugUtil.INSTANCE.i("delete position:", index + "--->remove after:" + mMediaList.size());
                    }
                }
            });
            MediaEntity mediaEntity = mMediaList.get(position);
            String path = mediaEntity.getFinalPath();
            int fileType = MimeType.getFileType(mediaEntity.getMimeType());
            long duration = mediaEntity.getDuration();
            viewHolder.tvDuration.setVisibility(fileType == PhoenixConstant.TYPE_VIDEO
                    ? View.VISIBLE : View.GONE);
            if(fileType == MimeType.ofVideo()){
                Drawable drawable = ContextCompat.getDrawable(viewHolder.ivPicture.getContext(), R.drawable.phoenix_video_icon);
                StringUtils.INSTANCE.modifyTextViewDrawable(viewHolder.tvDuration, drawable, 0);
                viewHolder.tvDuration.setText(DateUtils.INSTANCE.timeParse(duration));
            }
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.color.color_f6)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
            Glide.with(viewHolder.itemView.getContext())
                    .load(path)
                    .apply(options)
                    .into(viewHolder.ivPicture);
            //itemView 的点击事件
            if (mItemClickListener != null) {
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int adapterPosition = viewHolder.getAdapterPosition();
                        mItemClickListener.onItemClick(adapterPosition, v);
                    }
                });
            }
        }
    }

    protected OnItemClickListener mItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position, View v);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }
}
