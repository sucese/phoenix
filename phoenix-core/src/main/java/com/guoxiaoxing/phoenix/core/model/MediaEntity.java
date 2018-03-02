package com.guoxiaoxing.phoenix.core.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * 文件信息类，提供了setter/getter与builder两种创建方式
 */
public class MediaEntity implements Serializable, Parcelable {

    private static final long serialVersionUID = 1L;

    //文件类型
    private int fileType;
    //mime类型
    private String mimeType;
    //文件名称
    private String mediaName;
    //创建时间，单位毫秒
    private long createTime;
    //本地地址
    private String localPath;
    //本地缩略图地址
    private String localThumbnailPath;
    //时长，单位毫秒
    private long duration;
    //是否选中
    private boolean isChecked;
    //索引
    public int position;
    //数量
    private int number;
    //宽度
    private int width;
    //高度
    private int height;
    //大小 byte
    private long size;
    //经度
    private double latitude;
    //纬度
    private double longitude;

    //upload
    //是否上传
    private boolean isUploaded;
    //服务器地址
    private String onlinePath;
    //服务器缩略图地址
    private String onlineThumbnailPath;

    //compress
    //是否压缩
    private boolean isCompressed;
    //压缩后地址
    private String compressPath;

    //crop
    //裁剪后地址
    private String cutPath;
    //裁剪
    private int cropOffsetX;
    //
    private int cropOffsetY;
    //
    private int cropWidth;
    //
    private int cropHeight;
    //
    private float cropAspectRatio;
    //是否剪切
    private boolean isCut;

    //编辑后的路径
    private String editPath;

    public MediaEntity() {

    }

    public MediaEntity(String localPath, long duration, int fileType, String mimeType) {
        this.localPath = localPath;
        this.duration = duration;
        this.fileType = fileType;
        this.mimeType = mimeType;
    }

    public MediaEntity(String localPath, long duration, int fileType, String mimeType, int width, int height) {
        this.localPath = localPath;
        this.duration = duration;
        this.fileType = fileType;
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
    }

    public MediaEntity(String localPath, long duration,
                       boolean isChecked, int position, int number, int fileType) {
        this.localPath = localPath;
        this.duration = duration;
        this.isChecked = isChecked;
        this.position = position;
        this.number = number;
        this.fileType = fileType;
    }

    private MediaEntity(Builder builder) {
        fileType = builder.fileType;
        mimeType = builder.mimeType;
        mediaName = builder.mediaName;
        createTime = builder.createTime;
        localPath = builder.localPath;
        localThumbnailPath = builder.localThumbnailPath;
        duration = builder.duration;
        isChecked = builder.isChecked;
        position = builder.position;
        number = builder.number;
        width = builder.width;
        height = builder.height;
        size = builder.size;
        latitude = builder.latitude;
        longitude = builder.longitude;
        isUploaded = builder.isUploaded;
        onlinePath = builder.onlinePath;
        onlineThumbnailPath = builder.onlineThumbnailPath;
        isCompressed = builder.isCompressed;
        compressPath = builder.compressPath;
        cutPath = builder.cutPath;
        cropOffsetX = builder.cropOffsetX;
        cropOffsetY = builder.cropOffsetY;
        cropWidth = builder.cropWidth;
        cropHeight = builder.cropHeight;
        cropAspectRatio = builder.cropAspectRatio;
        isCut = builder.isCut;
        editPath = builder.editPath;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Get the final local path
     *
     * @return final local path
     */
    public String getFinalPath() {

        if (!TextUtils.isEmpty(editPath)) {
            return editPath;
        }

        if (!TextUtils.isEmpty(compressPath)) {
            return compressPath;
        }
        return localPath;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getLocalThumbnailPath() {
        return localThumbnailPath;
    }

    public void setLocalThumbnailPath(String localThumbnailPath) {
        this.localThumbnailPath = localThumbnailPath;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setUploaded(boolean uploaded) {
        isUploaded = uploaded;
    }

    public String getOnlinePath() {
        return onlinePath;
    }

    public void setOnlinePath(String onlinePath) {
        this.onlinePath = onlinePath;
    }

    public String getOnlineThumbnailPath() {
        return onlineThumbnailPath;
    }

    public void setOnlineThumbnailPath(String onlineThumbnailPath) {
        this.onlineThumbnailPath = onlineThumbnailPath;
    }

    public boolean isCompressed() {
        return isCompressed;
    }

    public void setCompressed(boolean compressed) {
        isCompressed = compressed;
    }

    public String getCompressPath() {
        return compressPath;
    }

    public void setCompressPath(String compressPath) {
        this.compressPath = compressPath;
    }

    public String getCutPath() {
        return cutPath;
    }

    public void setCutPath(String cutPath) {
        this.cutPath = cutPath;
    }

    public int getCropOffsetX() {
        return cropOffsetX;
    }

    public void setCropOffsetX(int cropOffsetX) {
        this.cropOffsetX = cropOffsetX;
    }

    public int getCropOffsetY() {
        return cropOffsetY;
    }

    public void setCropOffsetY(int cropOffsetY) {
        this.cropOffsetY = cropOffsetY;
    }

    public int getCropWidth() {
        return cropWidth;
    }

    public void setCropWidth(int cropWidth) {
        this.cropWidth = cropWidth;
    }

    public int getCropHeight() {
        return cropHeight;
    }

    public void setCropHeight(int cropHeight) {
        this.cropHeight = cropHeight;
    }

    public float getCropAspectRatio() {
        return cropAspectRatio;
    }

    public void setCropAspectRatio(float cropAspectRatio) {
        this.cropAspectRatio = cropAspectRatio;
    }

    public boolean isCut() {
        return isCut;
    }

    public void setCut(boolean cut) {
        isCut = cut;
    }

    public String getEditPath() {
        return editPath;
    }

    public void setEditPath(String editPath) {
        this.editPath = editPath;
    }

    public static final class Builder implements Parcelable{
        private int fileType = MimeType.ofImage();
        private String mimeType;
        private String mediaName;
        private long createTime;
        private String localPath;
        private String localThumbnailPath;
        private long duration;
        private boolean isChecked;
        private int position;
        private int number;
        private int width;
        private int height;
        private long size;
        private double latitude;
        private double longitude;
        private boolean isUploaded;
        private String onlinePath;
        private String onlineThumbnailPath;
        private boolean isCompressed;
        private String compressPath;
        private String cutPath;
        private int cropOffsetX;
        private int cropOffsetY;
        private int cropWidth;
        private int cropHeight;
        private float cropAspectRatio;
        private boolean isCut;
        private String editPath;

        private Builder() {
        }

        public Builder fileType(int val) {
            fileType = val;
            return this;
        }

        public Builder mimeType(String val) {
            mimeType = val;
            return this;
        }

        public Builder mediaName(String val) {
            mediaName = val;
            return this;
        }

        public Builder createTime(long val) {
            createTime = val;
            return this;
        }

        public Builder localPath(String val) {
            localPath = val;
            return this;
        }

        public Builder localThumbnailPath(String val) {
            localThumbnailPath = val;
            return this;
        }

        public Builder duration(long val) {
            duration = val;
            return this;
        }

        public Builder isChecked(boolean val) {
            isChecked = val;
            return this;
        }

        public Builder position(int val) {
            position = val;
            return this;
        }

        public Builder number(int val) {
            number = val;
            return this;
        }

        public Builder width(int val) {
            width = val;
            return this;
        }

        public Builder height(int val) {
            height = val;
            return this;
        }

        public Builder size(long val) {
            size = val;
            return this;
        }

        public Builder latitude(double val) {
            latitude = val;
            return this;
        }

        public Builder longitude(double val) {
            longitude = val;
            return this;
        }

        public Builder isUploaded(boolean val) {
            isUploaded = val;
            return this;
        }

        public Builder onlinePath(String val) {
            onlinePath = val;
            return this;
        }

        public Builder onlineThumbnailPath(String val) {
            onlineThumbnailPath = val;
            return this;
        }

        public Builder isCompressed(boolean val) {
            isCompressed = val;
            return this;
        }

        public Builder compressPath(String val) {
            compressPath = val;
            return this;
        }

        public Builder cutPath(String val) {
            cutPath = val;
            return this;
        }

        public Builder cropOffsetX(int val) {
            cropOffsetX = val;
            return this;
        }

        public Builder cropOffsetY(int val) {
            cropOffsetY = val;
            return this;
        }

        public Builder cropWidth(int val) {
            cropWidth = val;
            return this;
        }

        public Builder cropHeight(int val) {
            cropHeight = val;
            return this;
        }

        public Builder cropAspectRatio(float val) {
            cropAspectRatio = val;
            return this;
        }

        public Builder isCut(boolean val) {
            isCut = val;
            return this;
        }

        public Builder editPath(String val) {
            editPath = val;
            return this;
        }

        public MediaEntity build() {
            return new MediaEntity(this);
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.fileType);
            dest.writeString(this.mimeType);
            dest.writeString(this.mediaName);
            dest.writeLong(this.createTime);
            dest.writeString(this.localPath);
            dest.writeString(this.localThumbnailPath);
            dest.writeLong(this.duration);
            dest.writeByte(this.isChecked ? (byte) 1 : (byte) 0);
            dest.writeInt(this.position);
            dest.writeInt(this.number);
            dest.writeInt(this.width);
            dest.writeInt(this.height);
            dest.writeLong(this.size);
            dest.writeDouble(this.latitude);
            dest.writeDouble(this.longitude);
            dest.writeByte(this.isUploaded ? (byte) 1 : (byte) 0);
            dest.writeString(this.onlinePath);
            dest.writeString(this.onlineThumbnailPath);
            dest.writeByte(this.isCompressed ? (byte) 1 : (byte) 0);
            dest.writeString(this.compressPath);
            dest.writeString(this.cutPath);
            dest.writeInt(this.cropOffsetX);
            dest.writeInt(this.cropOffsetY);
            dest.writeInt(this.cropWidth);
            dest.writeInt(this.cropHeight);
            dest.writeFloat(this.cropAspectRatio);
            dest.writeByte(this.isCut ? (byte) 1 : (byte) 0);
            dest.writeString(this.editPath);
        }

        protected Builder(Parcel in) {
            this.fileType = in.readInt();
            this.mimeType = in.readString();
            this.mediaName = in.readString();
            this.createTime = in.readLong();
            this.localPath = in.readString();
            this.localThumbnailPath = in.readString();
            this.duration = in.readLong();
            this.isChecked = in.readByte() != 0;
            this.position = in.readInt();
            this.number = in.readInt();
            this.width = in.readInt();
            this.height = in.readInt();
            this.size = in.readLong();
            this.latitude = in.readDouble();
            this.longitude = in.readDouble();
            this.isUploaded = in.readByte() != 0;
            this.onlinePath = in.readString();
            this.onlineThumbnailPath = in.readString();
            this.isCompressed = in.readByte() != 0;
            this.compressPath = in.readString();
            this.cutPath = in.readString();
            this.cropOffsetX = in.readInt();
            this.cropOffsetY = in.readInt();
            this.cropWidth = in.readInt();
            this.cropHeight = in.readInt();
            this.cropAspectRatio = in.readFloat();
            this.isCut = in.readByte() != 0;
            this.editPath = in.readString();
        }

        public static final Creator<Builder> CREATOR = new Creator<Builder>() {
            @Override
            public Builder createFromParcel(Parcel source) {
                return new Builder(source);
            }

            @Override
            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.fileType);
        dest.writeString(this.mimeType);
        dest.writeString(this.mediaName);
        dest.writeLong(this.createTime);
        dest.writeString(this.localPath);
        dest.writeString(this.localThumbnailPath);
        dest.writeLong(this.duration);
        dest.writeByte(this.isChecked ? (byte) 1 : (byte) 0);
        dest.writeInt(this.position);
        dest.writeInt(this.number);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeLong(this.size);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeByte(this.isUploaded ? (byte) 1 : (byte) 0);
        dest.writeString(this.onlinePath);
        dest.writeString(this.onlineThumbnailPath);
        dest.writeByte(this.isCompressed ? (byte) 1 : (byte) 0);
        dest.writeString(this.compressPath);
        dest.writeString(this.cutPath);
        dest.writeInt(this.cropOffsetX);
        dest.writeInt(this.cropOffsetY);
        dest.writeInt(this.cropWidth);
        dest.writeInt(this.cropHeight);
        dest.writeFloat(this.cropAspectRatio);
        dest.writeByte(this.isCut ? (byte) 1 : (byte) 0);
        dest.writeString(this.editPath);
    }

    protected MediaEntity(Parcel in) {
        this.fileType = in.readInt();
        this.mimeType = in.readString();
        this.mediaName = in.readString();
        this.createTime = in.readLong();
        this.localPath = in.readString();
        this.localThumbnailPath = in.readString();
        this.duration = in.readLong();
        this.isChecked = in.readByte() != 0;
        this.position = in.readInt();
        this.number = in.readInt();
        this.width = in.readInt();
        this.height = in.readInt();
        this.size = in.readLong();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.isUploaded = in.readByte() != 0;
        this.onlinePath = in.readString();
        this.onlineThumbnailPath = in.readString();
        this.isCompressed = in.readByte() != 0;
        this.compressPath = in.readString();
        this.cutPath = in.readString();
        this.cropOffsetX = in.readInt();
        this.cropOffsetY = in.readInt();
        this.cropWidth = in.readInt();
        this.cropHeight = in.readInt();
        this.cropAspectRatio = in.readFloat();
        this.isCut = in.readByte() != 0;
        this.editPath = in.readString();
    }

    public static final Creator<MediaEntity> CREATOR = new Creator<MediaEntity>() {
        @Override
        public MediaEntity createFromParcel(Parcel source) {
            return new MediaEntity(source);
        }

        @Override
        public MediaEntity[] newArray(int size) {
            return new MediaEntity[size];
        }
    };
}
