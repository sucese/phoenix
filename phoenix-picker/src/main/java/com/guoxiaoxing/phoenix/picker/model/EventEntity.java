package com.guoxiaoxing.phoenix.picker.model;

import com.guoxiaoxing.phoenix.core.model.MediaEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EventEntity implements Serializable {
    public int what;
    public int position;
    public List<MediaEntity> mediaEntities = new ArrayList<>();

    public EventEntity() {
        super();
    }

    public EventEntity(int what) {
        super();
        this.what = what;
    }

    public EventEntity(int what, List<MediaEntity> mediaEntities) {
        super();
        this.what = what;
        this.mediaEntities = mediaEntities;
    }

    public EventEntity(int what, int position) {
        super();
        this.what = what;
        this.position = position;
    }

    public EventEntity(int what, List<MediaEntity> mediaEntities, int position) {
        super();
        this.what = what;
        this.position = position;
        this.mediaEntities = mediaEntities;
    }
}
