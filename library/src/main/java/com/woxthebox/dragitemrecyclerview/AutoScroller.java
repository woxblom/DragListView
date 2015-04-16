/**
 * Copyright 2014 Magnus Woxblom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    private static final int SCROLL_SPEED_DP = 8;
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
