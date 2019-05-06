package com.rhino.giftprogress;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.rhino.giftprogress.view.GiftProgressView;
import com.rhino.giftprogress.view.SpaceProgressView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GiftProgressView mGiftProgressView;
    private SpaceProgressView mSpaceProgressView;
    private int mCurrentProgress = 1;
    private int mCurrentProgress1 = 1;
    private int minMaxSpace = 10;
    private int topDrawableSpace = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGiftProgressView = findViewById(R.id.GiftProgressView);
        mSpaceProgressView = findViewById(R.id.SpaceProgressView);

        mGiftProgressView.setProgress(mCurrentProgress);
        mGiftProgressView.setOnProgressChangedListener(new GiftProgressView.OnProgressChangedListener() {
            @Override
            public void onChanged(GiftProgressView progressView, boolean fromUser, boolean isFinished) {
                switchMinMaxProgress(progressView.getProgress());
            }
        });

        switchMinMaxProgress1();
        mSpaceProgressView.setOnProgressChangedListener(new SpaceProgressView.OnProgressChangedListener() {
            @Override
            public void onChanged(SpaceProgressView progressView, boolean fromUser, boolean isFinished) {

            }
        });

        findViewById(R.id.bt_switch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (minMaxSpace == 10) {
                    minMaxSpace = 20;
                    topDrawableSpace = 5;
                } else {
                    minMaxSpace = 10;
                    topDrawableSpace = 5;
                }
                switchMinMaxProgress(mCurrentProgress);


                mSpaceProgressView.setProgress(6);
            }
        });

        findViewById(R.id.bt_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentProgress ++;
                if (mCurrentProgress > mGiftProgressView.getMaxProgress()) {
                    mCurrentProgress = mGiftProgressView.getMinProgress();
                }
                mGiftProgressView.setProgress(mCurrentProgress);

                mCurrentProgress1 ++;
                if (mCurrentProgress1 > mSpaceProgressView.getMaxProgress()) {
                    mCurrentProgress1 = mSpaceProgressView.getMinProgress();
                }
                mSpaceProgressView.setProgress(mCurrentProgress1);

            }
        });
    }


    private void switchMinMaxProgress(int progress) {
        int a = progress / minMaxSpace;
        int min = a  * minMaxSpace;
        int max = (a + 1) * minMaxSpace;

        if (mGiftProgressView.getMinProgress() == min && mGiftProgressView.getMaxProgress() == max) {
            return;
        }
        List<GiftProgressView.TopDrawable> list = new ArrayList<>();
        int topDrawableCount = minMaxSpace / topDrawableSpace;
        for (int i = 1; i <= topDrawableCount; i++) {
            list.add(new GiftProgressView.TopDrawable(min + i*topDrawableSpace, tintDrawable(getResources().getDrawable(R.mipmap.ic_gift), 0xFFFB7E16), 50, 50));
        }
        mGiftProgressView.setTopDrawableList(list);

        List<GiftProgressView.ProgressSpace> progressSpaceList = new ArrayList<>();
        int progressSpaceCount = minMaxSpace / topDrawableSpace;
        for (int i = 1; i < progressSpaceCount; i++) {
            progressSpaceList.add(new GiftProgressView.ProgressSpace(min + i*topDrawableSpace, dip2px(getApplicationContext(), 2), 0xFFFFFFFF));
        }
        mGiftProgressView.setProgressSpaceList(progressSpaceList);

        List<GiftProgressView.ProgressText> progressTextList = new ArrayList<>();
        int progressTextCount = minMaxSpace / topDrawableSpace;
        for (int i = 0; i <= progressTextCount; i++) {
            progressTextList.add(new GiftProgressView.ProgressText(min + i*topDrawableSpace, (min + i*topDrawableSpace) + "人", 0xFF888888));
        }
        mGiftProgressView.setProgressTextList(progressTextList);

        mGiftProgressView.setMinProgress(min);
        mGiftProgressView.setMaxProgress(max);
        mGiftProgressView.setProgress(min);

    }

    private void switchMinMaxProgress1() {

        int min = 0;
        int max = 6;

        List<SpaceProgressView.ProgressSpace> progressSpaceList = new ArrayList<>();
        List<SpaceProgressView.Progress> progressList = new ArrayList<>();
        for (int i = min; i < max; i++) {
            progressSpaceList.add(new SpaceProgressView.ProgressSpace(1+i, dip2px(getApplicationContext(), 2), 0xFFFFFFFF));
            progressList.add(new SpaceProgressView.Progress(1+i, 0xFF888888, 0xFFFF0000, 0xFFD9D9D9));
        }
        mSpaceProgressView.setProgressList(progressList);
        mSpaceProgressView.setProgressSpaceList(progressSpaceList);

        mSpaceProgressView.setMinProgress(min);
        mSpaceProgressView.setMaxProgress(max);
        mSpaceProgressView.setProgress(min);
    }

    public Drawable tintDrawable(Drawable drawable, int color) {
        return tintDrawable(drawable, ColorStateList.valueOf(color));
    }

    public Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, colors);
        return wrappedDrawable;
    }

    private int dip2px(Context ctx, float dpValue) {
        final float scale = ctx.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


}
