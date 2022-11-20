package com.demo.mxplayer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import android.util.AttributeSet;
import android.view.MotionEvent;

import com.demo.mxplayer.R;

public class VideoSliceSeekBar extends androidx.appcompat.widget.AppCompatImageView {
    private static final int SELECT_THUMB_LEFT = 1;
    private static final int SELECT_THUMB_NON = 0;
    private static final int SELECT_THUMB_RIGHT = 2;
    private boolean blocked;
    private boolean isVideoStatusDisplay;
    private int maxValue = 100;
    private Paint paint = new Paint();
    private Paint paintThumb = new Paint();
    private int progressBottom;
    private int progressColor = getResources().getColor(R.color.default_color);
    private int progressHalfHeight = 3;
    private int progressMinDiff = 15;
    private int progressMinDiffPixels;
    private int progressTop;
    private SeekBarChangeListener scl;
    private int secondaryProgressColor = getResources().getColor(R.color.gray);
    private int selectedThumb;
    private Bitmap thumbCurrentVideoPosition = BitmapFactory.decodeResource(getResources(), R.drawable.seekbar_thumb);
    private int thumbCurrentVideoPositionHalfWidth;
    private int thumbCurrentVideoPositionX;
    private int thumbCurrentVideoPositionY;
    private int thumbPadding = getResources().getDimensionPixelOffset(R.dimen.default_margin);
    private Bitmap thumbSlice = BitmapFactory.decodeResource(getResources(), R.drawable.cutter_01);
    private int thumbSliceHalfWidth;
    private int thumbSliceLeftValue;
    private int thumbSliceLeftX;
    private int thumbSliceRightValue;
    private int thumbSliceRightX;
    private int thumbSliceY;
    private Bitmap thumbSlicer = BitmapFactory.decodeResource(getResources(), R.drawable.cutter_02);

    public interface SeekBarChangeListener {
        void SeekBarValueChanged(int i, int i2);
    }

    public VideoSliceSeekBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public VideoSliceSeekBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public VideoSliceSeekBar(Context context) {
        super(context);
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (!isInEditMode()) {
            init();
        }
    }

    private void init() {
        if (this.thumbSlice.getHeight() > getHeight()) {
            getLayoutParams().height = this.thumbSlice.getHeight();
        }
        this.thumbSliceY = (getHeight() / 2) - (this.thumbSlice.getHeight() / 2);
        this.thumbCurrentVideoPositionY = (getHeight() / 2) - (this.thumbCurrentVideoPosition.getHeight() / 2);
        this.thumbSliceHalfWidth = this.thumbSlice.getWidth() / 2;
        this.thumbCurrentVideoPositionHalfWidth = this.thumbCurrentVideoPosition.getWidth() / 2;
        if (this.thumbSliceLeftX == 0 || this.thumbSliceRightX == 0) {
            this.thumbSliceLeftX = this.thumbPadding;
            this.thumbSliceRightX = getWidth() - this.thumbPadding;
        }
        this.progressMinDiffPixels = calculateCorrds(this.progressMinDiff) - (this.thumbPadding * 2);
        this.progressTop = (getHeight() / 2) - this.progressHalfHeight;
        this.progressBottom = (getHeight() / 2) + this.progressHalfHeight;
        invalidate();
    }

    public void setSeekBarChangeListener(SeekBarChangeListener seekBarChangeListener) {
        this.scl = seekBarChangeListener;
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.paint.setColor(this.progressColor);
        canvas.drawRect(new Rect(this.thumbPadding, this.progressTop, this.thumbSliceLeftX, this.progressBottom), this.paint);
        canvas.drawRect(new Rect(this.thumbSliceRightX, this.progressTop, getWidth() - this.thumbPadding, this.progressBottom), this.paint);
        this.paint.setColor(this.secondaryProgressColor);
        canvas.drawRect(new Rect(this.thumbSliceLeftX, this.progressTop, this.thumbSliceRightX, this.progressBottom), this.paint);
        if (!this.blocked) {
            canvas.drawBitmap(this.thumbSlice, (float) (this.thumbSliceLeftX - this.thumbSliceHalfWidth), (float) this.thumbSliceY, this.paintThumb);
            canvas.drawBitmap(this.thumbSlicer, (float) (this.thumbSliceRightX - this.thumbSliceHalfWidth), (float) this.thumbSliceY, this.paintThumb);
        }
        if (this.isVideoStatusDisplay) {
            canvas.drawBitmap(this.thumbCurrentVideoPosition, (float) (this.thumbCurrentVideoPositionX - this.thumbCurrentVideoPositionHalfWidth), (float) this.thumbCurrentVideoPositionY, this.paintThumb);
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.blocked) {
            int x = (int) motionEvent.getX();
            switch (motionEvent.getAction()) {
                case 0:
                    if ((x < this.thumbSliceLeftX - this.thumbSliceHalfWidth || x > this.thumbSliceLeftX + this.thumbSliceHalfWidth) && x >= this.thumbSliceLeftX - this.thumbSliceHalfWidth) {
                        if ((x < this.thumbSliceRightX - this.thumbSliceHalfWidth || x > this.thumbSliceRightX + this.thumbSliceHalfWidth) && x <= this.thumbSliceRightX + this.thumbSliceHalfWidth) {
                            if ((x - this.thumbSliceLeftX) + this.thumbSliceHalfWidth >= (this.thumbSliceRightX - this.thumbSliceHalfWidth) - x) {
                                if ((x - this.thumbSliceLeftX) + this.thumbSliceHalfWidth > (this.thumbSliceRightX - this.thumbSliceHalfWidth) - x) {
                                    this.selectedThumb = 2;
                                    break;
                                }
                            }
                            this.selectedThumb = 1;
                            break;
                        }
                        this.selectedThumb = 2;
                        break;
                    }
                    this.selectedThumb = 1;
                    break;

                case 1:
                    this.selectedThumb = 0;
                    break;
                case 2:
                    if ((x <= (this.thumbSliceLeftX + this.thumbSliceHalfWidth) + 0 && this.selectedThumb == 2) || (x >= (this.thumbSliceRightX - this.thumbSliceHalfWidth) - 0 && this.selectedThumb == 1)) {
                        this.selectedThumb = 0;
                    }
                    if (this.selectedThumb != 1) {
                        if (this.selectedThumb == 2) {
                            this.thumbSliceRightX = x;
                            break;
                        }
                    }
                    this.thumbSliceLeftX = x;
                    break;

            }
            notifySeekBarValueChanged();
        }
        return true;
    }

    private void notifySeekBarValueChanged() {
        if (this.thumbSliceLeftX < this.thumbPadding) {
            this.thumbSliceLeftX = this.thumbPadding;
        }
        if (this.thumbSliceRightX < this.thumbPadding) {
            this.thumbSliceRightX = this.thumbPadding;
        }
        if (this.thumbSliceLeftX > getWidth() - this.thumbPadding) {
            this.thumbSliceLeftX = getWidth() - this.thumbPadding;
        }
        if (this.thumbSliceRightX > getWidth() - this.thumbPadding) {
            this.thumbSliceRightX = getWidth() - this.thumbPadding;
        }
        invalidate();
        if (this.scl != null) {
            calculateThumbValue();
            this.scl.SeekBarValueChanged(this.thumbSliceLeftValue, this.thumbSliceRightValue);
        }
    }

    private void calculateThumbValue() {
        this.thumbSliceLeftValue = (this.maxValue * (this.thumbSliceLeftX - this.thumbPadding)) / (getWidth() - (this.thumbPadding * 2));
        this.thumbSliceRightValue = (this.maxValue * (this.thumbSliceRightX - this.thumbPadding)) / (getWidth() - (this.thumbPadding * 2));
    }

    private int calculateCorrds(int i) {
        return ((int) (((((double) getWidth()) - (((double) this.thumbPadding) * 2.0d)) / ((double) this.maxValue)) * ((double) i))) + this.thumbPadding;
    }

    public void setLeftProgress(int i) {
        if (i < this.thumbSliceRightValue - this.progressMinDiff) {
            this.thumbSliceLeftX = calculateCorrds(i);
        }
        notifySeekBarValueChanged();
    }

    public void setRightProgress(int i) {
        if (i > this.thumbSliceLeftValue + this.progressMinDiff) {
            this.thumbSliceRightX = calculateCorrds(i);
        }
        notifySeekBarValueChanged();
    }

    public int getSelectedThumb() {
        return this.selectedThumb;
    }

    public int getLeftProgress() {
        return this.thumbSliceLeftValue;
    }

    public int getRightProgress() {
        return this.thumbSliceRightValue;
    }

    public void setProgress(int i, int i2) {
        if (i2 - i > this.progressMinDiff) {
            this.thumbSliceLeftX = calculateCorrds(i);
            this.thumbSliceRightX = calculateCorrds(i2);
        }
        notifySeekBarValueChanged();
    }

    public void videoPlayingProgress(int i) {
        this.isVideoStatusDisplay = true;
        this.thumbCurrentVideoPositionX = calculateCorrds(i);
        invalidate();
    }

    public void removeVideoStatusThumb() {
        this.isVideoStatusDisplay = false;
        invalidate();
    }

    public void setSliceBlocked(boolean z) {
        this.blocked = z;
        invalidate();
    }

    public void setMaxValue(int i) {
        this.maxValue = i;
    }

    public void setProgressMinDiff(int i) {
        this.progressMinDiff = i;
        this.progressMinDiffPixels = calculateCorrds(i);
    }

    public void setProgressHeight(int i) {
        this.progressHalfHeight /= 2;
        invalidate();
    }

    public void setProgressColor(int i) {
        this.progressColor = i;
        invalidate();
    }

    public void setSecondaryProgressColor(int i) {
        this.secondaryProgressColor = i;
        invalidate();
    }

    public void setThumbSlice(Bitmap bitmap) {
        this.thumbSlice = bitmap;
        init();
    }

    public void setThumbCurrentVideoPosition(Bitmap bitmap) {
        this.thumbCurrentVideoPosition = bitmap;
        init();
    }

    public void setThumbPadding(int i) {
        this.thumbPadding = i;
        invalidate();
    }
}