package com.guoxiaoxing.phoenix.camera.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.guoxiaoxing.phoenix.camera.R;
import com.guoxiaoxing.phoenix.camera.util.Utils;

public class RecordButton extends View {

    private static final String TAG = "RecordButton";

    public static final long TIME_TO_START_RECORD = 500L;
    public static final float TIME_LIMIT_IN_MILS = 10000.0F;
    public static final float PROGRESS_LIM_TO_FINISH_STARTING_ANIM = 0.1F;

    public static final int RECORD_NOT_STARTED = 0;
    public static final int RECORD_STARTED = 1;
    public static final int RECORD_ENDED = 2;

    private int boundingBoxSize;
    private int outerCycleWidth;
    private int outerCycleWidthInc;
    private float innerCycleRadius;

    private RecordButtonHandler recordButtonHandler;
    private boolean touchable;
    private boolean recordable;

    private Paint centerCirclePaint;
    private Paint outBlackCirclePaint;
    private Paint outMostBlackCirclePaint;
    private float innerCircleRadiusToDraw;
    private RectF outMostCircleRect;
    private float outBlackCircleRadius;
    private float outMostBlackCircleRadius;
    private int colorWhite;
    private int colorRecord;
    private int colorWhiteP60;
    private int colorBlackP40;
    private int colorBlackP80;
    private int colorTranslucent;

    //top
    private float startAngle270;
    private float percentInDegree;
    private float centerX;
    private float centerY;
    private Paint processBarPaint;
    private Paint outMostWhiteCirclePaint;
    private Paint translucentPaint;
    private Context mContext;
    private int translucentCircleRadius = 0;
    private float outMostCircleRadius;
    private float innerCircleRadiusWhenRecord;
    private long btnPressTime;
    private int outBlackCircleRadiusInc;
    private int recordState;
    private OnRecordButtonListener onRecordButtonListener;

    private RecordButtonHandler.Task updateUITask = new RecordButtonHandler.Task() {
        public void run() {
            long timeLapse = System.currentTimeMillis() - btnPressTime;
            float percent = (float) (timeLapse - TIME_TO_START_RECORD) / TIME_LIMIT_IN_MILS;
            if (timeLapse >= TIME_TO_START_RECORD) {
                synchronized (RecordButton.this) {
                    if (recordState == RECORD_NOT_STARTED) {
                        recordState = RECORD_STARTED;
                        if (onRecordButtonListener != null) {
                            onRecordButtonListener.onLongClickStart();
                        }
                    }
                }
                if (!recordable) return;
                centerCirclePaint.setColor(colorRecord);
                outMostWhiteCirclePaint.setColor(colorWhite);
                percentInDegree = (360.0F * percent);
                if (percent <= 1.0F) {
                    if (percent <= PROGRESS_LIM_TO_FINISH_STARTING_ANIM) {
                        float calPercent = percent / PROGRESS_LIM_TO_FINISH_STARTING_ANIM;
                        float outIncDis = outBlackCircleRadiusInc * calPercent;
                        float curOutCircleWidth = outerCycleWidth + outerCycleWidthInc * calPercent;
                        processBarPaint.setStrokeWidth(curOutCircleWidth);
                        outMostWhiteCirclePaint.setStrokeWidth(curOutCircleWidth);
                        outBlackCircleRadius = (outMostCircleRadius + outIncDis - curOutCircleWidth / 2.0F);
                        outMostBlackCircleRadius = (curOutCircleWidth / 2.0F + (outMostCircleRadius + outIncDis));
                        outMostCircleRect = new RectF(centerX - outMostCircleRadius - outIncDis, centerY - outMostCircleRadius - outIncDis, centerX + outMostCircleRadius + outIncDis, centerY + outMostCircleRadius + outIncDis);
                        translucentCircleRadius = (int) (outIncDis + outMostCircleRadius);
                        innerCircleRadiusToDraw = calPercent * innerCircleRadiusWhenRecord;
                    }
                    invalidate();
                } else {
                    reset();
                }
            }
        }
    };

    public RecordButton(Context context) {
        super(context);
        mContext = context;
        setup();
    }

    public RecordButton(Context context, AttributeSet paramAttributeSet) {
        super(context, paramAttributeSet);
        mContext = context;
        setup();
    }

    public RecordButton(Context context, AttributeSet paramAttributeSet, int paramInt) {
        super(context, paramAttributeSet, paramInt);
        mContext = context;
        setup();
    }

    void setup() {
        touchable = recordable = true;
        boundingBoxSize = Utils.getRefLength(mContext, 100.0F);
        outerCycleWidth = Utils.getRefLength(mContext, 2.3F);
        outerCycleWidthInc = Utils.getRefLength(mContext, 3.0F);
        innerCycleRadius = Utils.getRefLength(mContext, 32.0F);
        colorRecord = getResources().getColor(R.color.button_background);
        colorWhite = getResources().getColor(R.color.white);
        colorWhiteP60 = getResources().getColor(R.color.white_sixty_percent);
        colorBlackP40 = getResources().getColor(R.color.black_forty_percent);
        colorBlackP80 = getResources().getColor(R.color.black_eighty_percent);
        colorTranslucent = getResources().getColor(R.color.circle_shallow_translucent_background);
        processBarPaint = new Paint();
        processBarPaint.setColor(colorRecord);
        processBarPaint.setAntiAlias(true);
        processBarPaint.setStrokeWidth(outerCycleWidth);
        processBarPaint.setStyle(Style.STROKE);
        processBarPaint.setStrokeCap(Cap.ROUND);
        outMostWhiteCirclePaint = new Paint();
        outMostWhiteCirclePaint.setColor(colorWhite);
        outMostWhiteCirclePaint.setAntiAlias(true);
        outMostWhiteCirclePaint.setStrokeWidth(outerCycleWidth);
        outMostWhiteCirclePaint.setStyle(Style.STROKE);
        centerCirclePaint = new Paint();
        centerCirclePaint.setColor(colorWhiteP60);
        centerCirclePaint.setAntiAlias(true);
        centerCirclePaint.setStyle(Style.FILL_AND_STROKE);
        outBlackCirclePaint = new Paint();
        outBlackCirclePaint.setColor(colorBlackP40);
        outBlackCirclePaint.setAntiAlias(true);
        outBlackCirclePaint.setStyle(Style.STROKE);
        outBlackCirclePaint.setStrokeWidth(1.0F);
        outMostBlackCirclePaint = new Paint();
        outMostBlackCirclePaint.setColor(colorBlackP80);
        outMostBlackCirclePaint.setAntiAlias(true);
        outMostBlackCirclePaint.setStyle(Style.STROKE);
        outMostBlackCirclePaint.setStrokeWidth(1.0F);
        translucentPaint = new Paint();
        translucentPaint.setColor(colorTranslucent);
        translucentPaint.setAntiAlias(true);
        translucentPaint.setStyle(Style.FILL_AND_STROKE);
        centerX = (boundingBoxSize / 2);
        centerY = (boundingBoxSize / 2);
        outMostCircleRadius = Utils.getRefLength(mContext, 37.0F);
        outBlackCircleRadiusInc = Utils.getRefLength(mContext, 7.0F);
        innerCircleRadiusWhenRecord = Utils.getRefLength(mContext, 35.0F);
        innerCircleRadiusToDraw = innerCycleRadius;
        outBlackCircleRadius = (outMostCircleRadius - outerCycleWidth / 2.0F);
        outMostBlackCircleRadius = (outMostCircleRadius + outerCycleWidth / 2.0F);
        startAngle270 = 270.0F;
        percentInDegree = 0.0F;
        outMostCircleRect = new RectF(centerX - outMostCircleRadius, centerY - outMostCircleRadius, centerX + outMostCircleRadius, centerY + outMostCircleRadius);
        recordButtonHandler = new RecordButtonHandler(Looper.getMainLooper(), updateUITask);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(centerX, centerY, translucentCircleRadius, translucentPaint);

        //center white-p40 circle
        canvas.drawCircle(centerX, centerY, innerCircleRadiusToDraw, centerCirclePaint);

        //static out-most white circle
        canvas.drawArc(outMostCircleRect, startAngle270, 360.0F, false, outMostWhiteCirclePaint);

        //progress bar
        canvas.drawArc(outMostCircleRect, startAngle270, percentInDegree, false, processBarPaint);

        canvas.drawCircle(centerX, centerY, outBlackCircleRadius, outBlackCirclePaint);
        canvas.drawCircle(centerX, centerY, outMostBlackCircleRadius, outMostBlackCirclePaint);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(boundingBoxSize, boundingBoxSize);
    }

    public boolean onTouchEvent(MotionEvent e) {
        if (!touchable) {
            return false;
        }
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent: down");
                startTicking();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent: move");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent: up");
                reset();
                break;

        }
        return true;
    }

    public void reset() {
        //Log.d(TAG, "reset: "+recordState);
        synchronized (RecordButton.this) {
            if (recordState == RECORD_STARTED) {
                if (onRecordButtonListener != null)
                    onRecordButtonListener.onLongClickEnd();
                recordState = RECORD_ENDED;
            } else if (recordState == RECORD_ENDED) {
                recordState = RECORD_NOT_STARTED;
            } else {
                if (onRecordButtonListener != null)
                    onRecordButtonListener.onClick();
            }
        }
        recordButtonHandler.clearMsg();
        percentInDegree = 0.0F;
        centerCirclePaint.setColor(colorWhiteP60);
        outMostWhiteCirclePaint.setColor(colorWhite);
        innerCircleRadiusToDraw = innerCycleRadius;
        outMostCircleRect = new RectF(centerX - outMostCircleRadius, centerY - outMostCircleRadius, centerX + outMostCircleRadius, centerY + outMostCircleRadius);
        translucentCircleRadius = 0;
        processBarPaint.setStrokeWidth(outerCycleWidth);
        outMostWhiteCirclePaint.setStrokeWidth(outerCycleWidth);
        outBlackCircleRadius = (outMostCircleRadius - outerCycleWidth / 2.0F);
        outMostBlackCircleRadius = (outMostCircleRadius + outerCycleWidth / 2.0F);
        invalidate();
    }

    public boolean isTouchable() {
        return touchable;
    }

    public boolean isRecordable() {
        return recordable;
    }

    public void setRecordable(boolean recordable) {
        this.recordable = recordable;
    }

    public void setTouchable(boolean touchable) {
        this.touchable = touchable;
    }

    public void startTicking() {
        synchronized (RecordButton.this) {
            if (recordState != RECORD_NOT_STARTED)
                recordState = RECORD_NOT_STARTED;
        }
        btnPressTime = System.currentTimeMillis();
        recordButtonHandler.sendLoopMsg(0L, 16L);
    }

    public void setOnRecordButtonListener(OnRecordButtonListener onRecordButtonListener) {
        this.onRecordButtonListener = onRecordButtonListener;
    }

    public interface OnRecordButtonListener {
        void onClick();

        void onLongClickStart();

        void onLongClickEnd();
    }
}
