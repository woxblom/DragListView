package com.woxthebox.dragitemrecyclerview;

import android.content.Context;
import android.os.Handler;

public class AutoScroller {
    public enum ScrollDirection {
        UP, DOWN, LEFT, RIGHT
    }

    public interface AutoScrollListener {
        public void onAutoScroll(int dx, int dy);
    }

    private static final int SCROLL_SPEED_DP = 6;
    private static final int AUTO_SCROLL_UPDATE_DELAY = 12;

    private Handler mHandler = new Handler();
    private AutoScrollListener mListener;
    private boolean mIsAutoScrolling;
    private int mScrollSpeed;

    public AutoScroller(Context context, AutoScrollListener listener) {
        mListener = listener;
        mScrollSpeed = (int)(context.getResources().getDisplayMetrics().density * SCROLL_SPEED_DP);
    }

    public boolean isAutoScrolling() {
        return mIsAutoScrolling;
    }

    public void stopAutoScroll() {
        mIsAutoScrolling = false;
    }

    public void startAutoScroll(ScrollDirection direction) {
        switch (direction) {
            case UP:
                startAutoScroll(0, mScrollSpeed);
                break;
            case DOWN:
                startAutoScroll(0, -mScrollSpeed);
                break;
            case LEFT:
                startAutoScroll(mScrollSpeed, 0);
                break;
            case RIGHT:
                startAutoScroll(-mScrollSpeed, 0);
                break;
        }
    }

    private void startAutoScroll(int dx, int dy) {
        if (!mIsAutoScrolling) {
            mIsAutoScrolling = true;
            autoScroll(dx, dy);
        }
    }

    private void autoScroll(final int dx, final int dy) {
        if (mIsAutoScrolling) {
            mListener.onAutoScroll(dx, dy);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    autoScroll(dx, dy);
                }
            }, AUTO_SCROLL_UPDATE_DELAY);
        }
    }
}
