package com.rhino.giftprogress.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.rhino.giftprogress.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rhino
 * @since Create on 2018/10/11.
 **/
public class GiftProgressView extends View {

    private static final int DEFAULT_PROGRESS_BACKGROUND_COLOR = 0xFFD9D9D9;
    private static final int DEFAULT_PROGRESS_COLOR = 0xFFFB7E16;
    private static final int DEFAULT_THUMB_COLOR = 0xFFFFFFFF;
    private static final int DEFAULT_MIN_PROGRESS = 0;
    private static final int DEFAULT_MAX_PROGRESS = 100;
    private static final int DEFAULT_PROGRESS_CORNER = 6;
    private static final int DEFAULT_PROGRESS_HEIGHT = 6;
    private static final int DEFAULT_THUMB_WIDTH = 6;
    private int mProgressBackgroundColor = DEFAULT_PROGRESS_BACKGROUND_COLOR;
    private int mProgressColor = DEFAULT_PROGRESS_COLOR;
    private int mThumbColor = DEFAULT_THUMB_COLOR;
    private int mMinProgress = DEFAULT_MIN_PROGRESS;
    private int mMaxProgress = DEFAULT_MAX_PROGRESS;
    private int mProgressCorner = DEFAULT_PROGRESS_CORNER;
    private int mProgressHeight = DEFAULT_PROGRESS_HEIGHT;
    private int mThumbWidth = DEFAULT_THUMB_WIDTH;

    private int mProgressLength;
    private int mCurrProgress;
    private int mLastProgress;

    private int mViewHeight;
    private int mViewWidth;
    private GradientDrawable mProgressBgDrawable;
    private GradientDrawable mProgressDrawable;
    private Paint mSectionPointPaint;
    private Paint mThumbPaint;
    private Rect mProgressBackgroundRect;
    private Rect mProgressRect;
    private Rect mThumbDestRect;

    private ValueAnimator mToDestValueAnimator;

    private List<ProgressSpace> mProgressSpaceList = new ArrayList<>();
    private List<ProgressText> mProgressTextList = new ArrayList<>();
    private List<TopDrawable> mTopDrawableList = new ArrayList<>();

    private boolean mIsFromUser = false;
    private OnProgressChangedListener mOnProgressListener;


    public GiftProgressView(Context context) {
        this(context, null);
    }

    public GiftProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GiftProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (null != attrs) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GiftProgressView);
            mProgressHeight = typedArray.getDimensionPixelSize(R.styleable.GiftProgressView_csb_progress_height,
                    DEFAULT_PROGRESS_HEIGHT);
            mProgressCorner = typedArray.getDimensionPixelSize(R.styleable.GiftProgressView_csb_progress_corner,
                    DEFAULT_PROGRESS_CORNER);
            mThumbWidth = typedArray.getDimensionPixelSize(R.styleable.GiftProgressView_csb_thumb_width,
                    DEFAULT_THUMB_WIDTH);
            mProgressBackgroundColor = typedArray.getColor(R.styleable.GiftProgressView_csb_background_color,
                    DEFAULT_PROGRESS_BACKGROUND_COLOR);
            mProgressColor = typedArray.getColor(R.styleable.GiftProgressView_csb_progress_color,
                    DEFAULT_PROGRESS_COLOR);
            mMinProgress = typedArray.getInt(R.styleable.GiftProgressView_csb_min_value,
                    DEFAULT_MIN_PROGRESS);
            mMaxProgress = typedArray.getInt(R.styleable.GiftProgressView_csb_max_value,
                    DEFAULT_MAX_PROGRESS);
            typedArray.recycle();
        }

        mSectionPointPaint = new Paint();
        mSectionPointPaint.setStyle(Paint.Style.FILL);
        mSectionPointPaint.setColor(mProgressColor);
        mSectionPointPaint.setAntiAlias(true);

        mThumbPaint = new Paint();
        mThumbPaint.setStyle(Paint.Style.FILL);
        mThumbPaint.setAntiAlias(true);

        mProgressBgDrawable = new GradientDrawable();
        mProgressBgDrawable.setShape(GradientDrawable.RECTANGLE);
        mProgressBgDrawable.setColor(mProgressBackgroundColor);

        mProgressDrawable = new GradientDrawable();
        mProgressDrawable.setShape(GradientDrawable.RECTANGLE);
        mProgressDrawable.setColor(mProgressColor);

        mProgressBackgroundRect = new Rect();
        mProgressRect = new Rect();
        mThumbDestRect = new Rect();

        mCurrProgress = mMinProgress;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            mViewWidth = widthSize;
        } else {
            mViewWidth = getWidth();
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            mViewHeight = heightSize;
        } else {
            mViewHeight = getHeight();
        }
        initViewSize(mViewWidth, mViewHeight);
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

        canvas.translate(mViewWidth / 2, mViewHeight / 2);

        drawProgressBackground(canvas);
        drawProgress(canvas);
        drawThumb(canvas);
        drawTopDrawable(canvas);
        drawProgressSpace(canvas);
        drawProgressText(canvas);

        canvas.restore();
    }

    /**
     * Draw the progress background.
     *
     * @param canvas Canvas
     */
    private void drawProgressBackground(Canvas canvas) {
        canvas.save();
        mProgressBgDrawable.setBounds(mProgressBackgroundRect);
        mProgressBgDrawable.setCornerRadius(mProgressCorner);
        mProgressBgDrawable.draw(canvas);
        canvas.restore();
    }

    /**
     * Draw the progress.
     *
     * @param canvas Canvas
     */
    private void drawProgress(Canvas canvas) {
        canvas.save();
        mProgressDrawable.setBounds(mProgressRect);
        mProgressDrawable.setCornerRadius(mProgressCorner);
        mProgressDrawable.draw(canvas);
        canvas.restore();
    }

    /**
     * Draw the progress Thumb.
     *
     * @param canvas Canvas
     */
    private void drawThumb(Canvas canvas) {
        canvas.save();
        mThumbPaint.setColor(mThumbColor);
        canvas.drawRect(mThumbDestRect, mThumbPaint);

        mThumbPaint.setColor(0xFFAAAAAA);
        mThumbPaint.setTextSize(30);

        String text = mCurrProgress + "人";
        int width = (int) mSectionPointPaint.measureText(text);
        canvas.drawText(text, mThumbDestRect.centerX() - width / 2, mThumbDestRect.bottom + 30 + 5, mThumbPaint);

        mThumbPaint.setColor(mProgressColor);
        Path path = new Path();
        path.moveTo(mThumbDestRect.centerX(), mThumbDestRect.top - 15);
        path.lineTo(mThumbDestRect.centerX() - 15, mThumbDestRect.top - 40);
        path.lineTo(mThumbDestRect.centerX() + 15, mThumbDestRect.top - 40);
        path.lineTo(mThumbDestRect.centerX(), mThumbDestRect.top - 15);
        path.close();
        canvas.drawPath(path, mThumbPaint);

        canvas.restore();
    }

    /**
     * Draw the top Drawable.
     *
     * @param canvas Canvas
     */
    private void drawTopDrawable(Canvas canvas) {
        if (mTopDrawableList == null || mTopDrawableList.isEmpty()) {
            return;
        }
        canvas.save();

        for (TopDrawable topDrawable : mTopDrawableList) {
            if (mCurrProgress == topDrawable.progress) {
                continue;
            }
            int x = (int) progress2Coord(topDrawable.progress);
            Rect rect = new Rect(x - topDrawable.width / 2,
                    mProgressRect.top - topDrawable.height - 10,
                    x + topDrawable.width / 2,
                    mProgressRect.top - 10);
            topDrawable.drawable.setBounds(rect);
            topDrawable.drawable.draw(canvas);
        }
        canvas.restore();
    }

    /**
     * Draw the progress space.
     *
     * @param canvas Canvas
     */
    private void drawProgressSpace(Canvas canvas) {
        if (mProgressSpaceList == null || mProgressSpaceList.isEmpty()) {
            return;
        }
        canvas.save();

        for (ProgressSpace progressSpace : mProgressSpaceList) {
            if (mCurrProgress == progressSpace.progress) {
                continue;
            }
            int x = (int) progress2Coord(progressSpace.progress);
            Rect rect = new Rect(x - progressSpace.spaceWidth / 2,
                    mProgressRect.top,
                    x + progressSpace.spaceWidth / 2,
                    mProgressRect.bottom);

            mThumbPaint.setColor(progressSpace.spaceColor);
            canvas.drawRect(rect, mThumbPaint);
        }
        canvas.restore();
    }

    /**
     * Draw the progress text.
     *
     * @param canvas Canvas
     */
    private void drawProgressText(Canvas canvas) {
        if (mProgressTextList == null || mProgressTextList.isEmpty()) {
            return;
        }
        canvas.save();

        for (ProgressText progressText : mProgressTextList) {
            if (mCurrProgress == progressText.progress) {
                continue;
            }
            int x = (int) progress2Coord(progressText.progress);

            mSectionPointPaint.setColor(0xFFAAAAAA);
            mSectionPointPaint.setTextSize(30);

            int width = (int) mSectionPointPaint.measureText(progressText.text);
            canvas.drawText(progressText.text, x - width / 2, mProgressRect.bottom + 30 + 5, mSectionPointPaint);
        }
        canvas.restore();
    }

    /**
     * Do something init.
     *
     * @param width  width
     * @param height height
     */
    private void initViewSize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        mProgressLength = width - 100;

        mProgressBackgroundRect.top = -mProgressHeight;
        mProgressBackgroundRect.bottom = -mProgressBackgroundRect.top;
        mProgressBackgroundRect.left = -mProgressLength / 2;
        mProgressBackgroundRect.right = mProgressLength / 2;

        mProgressRect.top = -mProgressHeight;
        mProgressRect.bottom = -mProgressRect.top;
        mProgressRect.left = -mProgressLength / 2;
        mProgressRect.right = -mProgressLength / 2;

        mThumbDestRect.top = -mProgressHeight;
        mThumbDestRect.bottom = -mProgressRect.top;
        mThumbDestRect.left = -mProgressLength / 2 - mThumbWidth / 2;
        mThumbDestRect.right = -mProgressLength / 2 + mThumbWidth / 2;

        setProgress(mCurrProgress);
    }

    /**
     * Change thumb to dest progress.
     *
     * @param anim     true show anim, false not show anim
     * @param progress progress
     */
    private void toDestProgress(boolean anim, int progress) {
        if (anim && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            float startCoord = checkCoord(progress2Coord(mCurrProgress));
            float stopCoord = checkCoord(progress2Coord(progress));
            mCurrProgress = progress;
            if (null == mToDestValueAnimator) {
                mToDestValueAnimator = new ValueAnimator();
                mToDestValueAnimator.setDuration(400);
                mToDestValueAnimator.setInterpolator(new DecelerateInterpolator());
                mToDestValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float coord = (Float) animation.getAnimatedValue();
                        moveToPoint(coord);
                    }
                });
            } else {
                mToDestValueAnimator.cancel();
            }
            mToDestValueAnimator.setFloatValues(startCoord, stopCoord);
            mToDestValueAnimator.start();
        } else {
            mCurrProgress = progress;
            float stopCoord = checkCoord(progress2Coord(progress));
            moveToPoint(stopCoord);
        }
    }

    /**
     * Move the thumb position.
     *
     * @param coord the x or y coordinate of thumb
     */
    private void moveToPoint(float coord) {
        float halfLength = mProgressLength / 2;
        if (coord > halfLength) {
            coord = halfLength;
            mCurrProgress = mMaxProgress;
        } else if (coord < -halfLength) {
            coord = -halfLength;
            mCurrProgress = mMinProgress;
        }
        mThumbDestRect.left = (int) (coord - mThumbWidth / 2);
        mThumbDestRect.right = (int) (coord + mThumbWidth / 2);
        mProgressRect.right = (int) coord;
        invalidate();
    }

    /**
     * Check the coordinate
     *
     * @param coord coordinate
     * @return the new coordinate
     */
    private float checkCoord(float coord) {
        float halfLength = mProgressLength / 2;
        if (coord > halfLength) {
            return halfLength;
        } else if (coord < -halfLength) {
            return -halfLength;
        }
        return coord;
    }

    /**
     * Return x or y coordinate by progress value.
     *
     * @param progress progress value
     * @return x or y coordinate
     */
    private float progress2Coord(int progress) {
        return (float) mProgressLength * (progress - mMinProgress)
                / (mMaxProgress - mMinProgress) - mProgressLength / 2f;
    }

    /**
     * Get current progress.
     *
     * @return the current progress
     */
    public int getProgress() {
        return mCurrProgress;
    }

    /**
     * Get min progress.
     *
     * @return the min progress
     */
    public int getMinProgress() {
        return mMinProgress;
    }

    /**
     * Set min progress.
     *
     * @param minProgress the min progress
     */
    public void setMinProgress(int minProgress) {
        this.mMinProgress = minProgress;
    }

    /**
     * Get max progress.
     *
     * @return the max progress
     */
    public int getMaxProgress() {
        return mMaxProgress;
    }

    /**
     * Set max progress.
     *
     * @param maxProgress the max progress
     */
    public void setMaxProgress(int maxProgress) {
        this.mMaxProgress = maxProgress;
    }

    /**
     * Set progress.
     *
     * @param progress progress
     */
    public void setProgress(int progress) {
        setProgress(progress, false, false);
    }

    /**
     * Set progress.
     *
     * @param progress progress
     * @param anim     true show anim, false not show anim
     * @param fromUser true by user, false not by user
     */
    public void setProgress(int progress, boolean anim, boolean fromUser) {
        if (progress <= mMinProgress) {
            progress = mMinProgress;
        } else if (progress >= mMaxProgress) {
            progress = mMaxProgress;
        }

        toDestProgress(anim, progress);
        if (null != mOnProgressListener) {
            if (mLastProgress != mCurrProgress) {
                mIsFromUser = fromUser;
                mOnProgressListener.onChanged(this, mIsFromUser, true);
                mIsFromUser = false;
            }
            mLastProgress = mCurrProgress;
        }
    }

    /**
     * Set the list of top drawable.
     * @param list list
     */
    public void setTopDrawableList(List<TopDrawable> list) {
        this.mTopDrawableList = list;
    }

    /**
     * Set the list of progress space.
     * @param list list
     */
    public void setProgressSpaceList(List<ProgressSpace> list) {
        this.mProgressSpaceList = list;
    }

    /**
     * Set the list of progress text.
     * @param list list
     */
    public void setProgressTextList(List<ProgressText> list) {
        this.mProgressTextList = list;
    }


    /**
     * Register a callback to be invoked when the progress changes.
     *
     * @param listener the callback to call on progress change
     */
    public void setOnProgressChangedListener(OnProgressChangedListener listener) {
        mOnProgressListener = listener;
    }

    public interface OnProgressChangedListener {
        void onChanged(GiftProgressView progressView, boolean fromUser,
                       boolean isFinished);
    }

    public static class TopDrawable {
        public int progress;
        public Drawable drawable;
        public int width;
        public int height;

        public TopDrawable(int progress, Drawable drawable, int width, int height) {
            this.progress = progress;
            this.drawable = drawable;
            this.width = width;
            this.height = height;
        }
    }

    public static class ProgressSpace {
        public int progress;
        public int spaceWidth;
        public int spaceColor;

        public ProgressSpace(int progress, int spaceWidth, int spaceColor) {
            this.progress = progress;
            this.spaceWidth = spaceWidth;
            this.spaceColor = spaceColor;
        }
    }

    public static class ProgressText {
        public int progress;
        public String text;
        public int textColor;

        public ProgressText(int progress, String text, int textColor) {
            this.progress = progress;
            this.text = text;
            this.textColor = textColor;
        }
    }


}
