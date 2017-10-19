package com.guoxiaoxing.phoenix.upload.model;

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/6/26 上午9:41
 */
public class SoucheUpload {

    private String code;
    private DataBean data;
    private String msg;
    private boolean success;
    private String traceId;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public static class DataBean {

        private String coverPath;
        private double duration;
        private String fileType;
        private String filename;
        private int filesize;
        private String fullCoverPath;
        private String fullFilePath;
        private String parameterName;
        private String relativeFilepath;

        public String getCoverPath() {
            return coverPath;
        }

        public void setCoverPath(String coverPath) {
            this.coverPath = coverPath;
        }

        public double getDuration() {
            return duration;
        }

        public void setDuration(double duration) {
            this.duration = duration;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public int getFilesize() {
            return filesize;
        }

        public void setFilesize(int filesize) {
            this.filesize = filesize;
        }

        public String getFullCoverPath() {
            return fullCoverPath;
        }

        public void setFullCoverPath(String fullCoverPath) {
            this.fullCoverPath = fullCoverPath;
        }

        public String getFullFilePath() {
            return fullFilePath;
        }

        public void setFullFilePath(String fullFilePath) {
            this.fullFilePath = fullFilePath;
        }

        public String getParameterName() {
            return parameterName;
        }

        public void setParameterName(String parameterName) {
            this.parameterName = parameterName;
        }

        public String getRelativeFilepath() {
            return relativeFilepath;
        }

        public void setRelativeFilepath(String relativeFilepath) {
            this.relativeFilepath = relativeFilepath;
        }
    }
}
