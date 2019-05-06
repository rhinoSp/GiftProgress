package com.rhino.giftprogress.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.rhino.giftprogress.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rhino
 * @since Create on 2019/5/6.
 **/
public class SpaceProgressView extends View {

    private static final int DEFAULT_PROGRESS_BACKGROUND_COLOR = 0xFFD9D9D9;
    private static final int DEFAULT_PROGRESS_COLOR = 0xFFFB7E16;
    private static final int DEFAULT_MIN_PROGRESS = 0;
    private static final int DEFAULT_MAX_PROGRESS = 100;
    private static final int DEFAULT_PROGRESS_CORNER = 6;
    private static final int DEFAULT_PROGRESS_HEIGHT = 6;
    private int mProgressBackgroundColor = DEFAULT_PROGRESS_BACKGROUND_COLOR;
    private int mProgressColor = DEFAULT_PROGRESS_COLOR;
    private int mMinProgress = DEFAULT_MIN_PROGRESS;
    private int mMaxProgress = DEFAULT_MAX_PROGRESS;
    private int mProgressCorner = DEFAULT_PROGRESS_CORNER;
    private int mProgressHeight = DEFAULT_PROGRESS_HEIGHT;

    private int mProgressLength;
    private int mCurrProgress;
    private int mLastProgress;

    private int mViewHeight;
    private int mViewWidth;
    private GradientDrawable mProgressBackgroundDrawable;
    private GradientDrawable mProgressDrawable;
    private Paint mProgressPaint;
    private Rect mProgressBackgroundRect;
    private Rect mProgressRect;

    private ValueAnimator mToDestValueAnimator;

    private List<Progress> mProgressList = new ArrayList<>();
    private List<ProgressSpace> mProgressSpaceList = new ArrayList<>();

    private boolean mIsFromUser = false;
    private OnProgressChangedListener mOnProgressListener;

    public SpaceProgressView(Context context) {
        this(context, null);
    }

    public SpaceProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpaceProgressView(Context context, AttributeSet attrs, int defStyle) {
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

        mProgressPaint = new Paint();
        mProgressPaint.setStyle(Paint.Style.FILL);
        mProgressPaint.setAntiAlias(true);

        mProgressBackgroundDrawable = new GradientDrawable();
        mProgressBackgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        mProgressBackgroundDrawable.setColor(mProgressBackgroundColor);

        mProgressDrawable = new GradientDrawable();
        mProgressDrawable.setShape(GradientDrawable.RECTANGLE);
        mProgressDrawable.setColor(mProgressColor);

        mProgressBackgroundRect = new Rect();
        mProgressRect = new Rect();

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
        drawProgressSpace(canvas);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int progress = coord2Progress(x - mProgressLength / 2f);
                setProgress(progress, false, false);
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
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

        setProgress(mCurrProgress);
    }

    /**
     * Draw the progress background.
     *
     * @param canvas Canvas
     */
    private void drawProgressBackground(Canvas canvas) {
        canvas.save();
        mProgressBackgroundDrawable.setBounds(mProgressBackgroundRect);
        mProgressBackgroundDrawable.setCornerRadius(mProgressCorner);
        mProgressBackgroundDrawable.draw(canvas);
        canvas.restore();
    }

    /**
     * Draw the progress.
     *
     * @param canvas Canvas
     */
    private void drawProgress(Canvas canvas) {
        canvas.save();
        if (mProgressList == null || mProgressList.isEmpty()) {
            mProgressDrawable.setBounds(mProgressRect);
            mProgressDrawable.setCornerRadius(mProgressCorner);
            mProgressDrawable.draw(canvas);
        } else {
            for (Progress progress : mProgressList) {
                int preX = (int) progress2Coord(progress.progress - 1);
                int x = (int) progress2Coord(progress.progress);
                Rect rect = new Rect(preX,
                        mProgressRect.top,
                        x,
                        mProgressRect.bottom);

                mProgressDrawable.setBounds(rect);
                if (progress.progress == mMinProgress + 1) {
                    mProgressDrawable.setCornerRadii(new float[]{mProgressCorner, mProgressCorner, 0, 0, 0, 0, mProgressCorner, mProgressCorner});
                } else if (progress.progress == mMaxProgress) {
                    mProgressDrawable.setCornerRadii(new float[]{0, 0, mProgressCorner, mProgressCorner, mProgressCorner, mProgressCorner, 0, 0});
                } else {
                    mProgressDrawable.setCornerRadius(0);
                }

                if (progress.progress < mCurrProgress) {
                    mProgressDrawable.setColor(progress.progressPreColor);
                } else if (progress.progress == mCurrProgress) {
                    mProgressDrawable.setColor(progress.progressCurrColor);
                } else {
                    mProgressDrawable.setColor(progress.progressNextColor);
                }
                mProgressDrawable.draw(canvas);
            }
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
            if (progressSpace.progress == mMaxProgress) {
                continue;
            }
            int x = (int) progress2Coord(progressSpace.progress);
            Rect rect = new Rect(x - progressSpace.spaceWidth / 2,
                    mProgressRect.top,
                    x + progressSpace.spaceWidth / 2,
                    mProgressRect.bottom);

            mProgressPaint.setColor(progressSpace.spaceColor);
            canvas.drawRect(rect, mProgressPaint);
        }
        canvas.restore();
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
     * Return progress value by x or y coordinate.
     *
     * @param coord x or y coordinate
     * @return progress value
     */
    private int coord2Progress(float coord) {
        if (coord > mProgressLength / 2) {
            return mMaxProgress;
        } else if (coord < -mProgressLength / 2) {
            return mMinProgress;
        } else {
            return Math.round((coord + mProgressLength / 2f)
                    * (mMaxProgress - mMinProgress) / mProgressLength)
                    + mMinProgress;
        }
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
     */
    public void setProgress(int progress, boolean anim) {
        setProgress(progress, anim, false);
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
     * Set the list of progress.
     *
     * @param list list
     */
    public void setProgressList(List<Progress> list) {
        this.mProgressList = list;
    }

    /**
     * Set the list of progress space.
     *
     * @param list list
     */
    public void setProgressSpaceList(List<ProgressSpace> list) {
        this.mProgressSpaceList = list;
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
        void onChanged(SpaceProgressView progressView, boolean fromUser,
                       boolean isFinished);
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

    public static class Progress {
        public int progress;
        public int progressPreColor;
        public int progressCurrColor;
        public int progressNextColor;

        public Progress(int progress, int progressPreColor, int progressCurrColor, int progressNextColor) {
            this.progress = progress;
            this.progressPreColor = progressPreColor;
            this.progressCurrColor = progressCurrColor;
            this.progressNextColor = progressNextColor;
        }
    }

}
