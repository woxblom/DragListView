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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class DragItemRecyclerView extends RecyclerView {

    public interface DragItemListener {
        public void onDragStarted(int itemPosition);

        public void onDragEnded(int newItemPosition);
    }

    private enum DragState {
        DRAG_STARTED, DRAGGING, DRAG_ENDED
    }

    private static final int SCROLL_SPEED = 10;
    private static final int AUTO_SCROLL_UPDATE_DELAY = 12;

    private Handler mHandler = new Handler();
    private DragState mDragState = DragState.DRAG_ENDED;
    private DragItemAdapter mAdapter;
    private DragItemAdapter.DragItem mDragItem;
    private DragItemImage mDragItemImage;
    private DragItemListener mListener;
    private int mItemPosition;
    private boolean mAutoScrollEnabled;
    private boolean mIsGrid;

    public DragItemRecyclerView(Context context) {
        super(context);
        init();
    }

    public DragItemRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public DragItemRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOnDragListener(new DragListener());
        mDragItemImage = new DragItemImage();
    }

    public void setDragItemListener(DragItemListener listener) {
        mListener = listener;
    }

    public void setDragItemBackgroundColor(int color) {
        mDragItemImage.setColor(color);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (!(adapter instanceof DragItemAdapter)) {
            throw new RuntimeException("Adapter must extend DragItemAdapter");
        }
        if (!adapter.hasStableIds()) {
            throw new RuntimeException("Adapter must have stable ids");
        }

        super.setAdapter(adapter);
        mAdapter = (DragItemAdapter) adapter;
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if (!(layout instanceof LinearLayoutManager)) {
            throw new RuntimeException("Layout must be an instance of LinearLayoutManager");
        }

        mIsGrid = (layout instanceof GridLayoutManager);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (mDragState != DragState.DRAG_ENDED) {
            mDragItemImage.draw(canvas);
        }
    }

    private void stopAutoScroll() {
        mAutoScrollEnabled = false;
    }

    private void startAutoScroll(int dy) {
        if (!mAutoScrollEnabled) {
            mAutoScrollEnabled = true;
            autoScrollList(dy);
        }
    }

    private void autoScrollList(final int dy) {
        if (mAutoScrollEnabled) {
            scrollBy(0, dy);
            updateDragPositionAndScroll();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    autoScrollList(dy);
                }
            }, AUTO_SCROLL_UPDATE_DELAY);
        }
    }

    public View findChildView(float x, float y) {
        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (x >= child.getLeft() && x <= child.getRight() && y >= child.getTop() && y <= child.getBottom()) {
                return child;
            }
        }
        return null;
    }

    private void updateDragPositionAndScroll() {
        View view = findChildView(mDragItemImage.getCenterX(), mDragItemImage.getCenterY());
        if (view != null) {
            int newPos = getChildPosition(view);
            if (mItemPosition != -1 && mItemPosition != newPos) {
                mAdapter.changeItemPosition(mItemPosition, newPos);
                mItemPosition = newPos;
            }

            int listBottom = getHeight();
            LinearLayoutManager layout = (LinearLayoutManager) getLayoutManager();
            if (mDragItemImage.getCenterY() > listBottom - view.getHeight() && layout.findLastCompletelyVisibleItemPosition() !=
                    mAdapter.getItemCount() - 1) {
                startAutoScroll(SCROLL_SPEED);
            } else if (mDragItemImage.getCenterY() < view.getHeight() && layout.findFirstCompletelyVisibleItemPosition() != 0) {
                startAutoScroll(-SCROLL_SPEED);
            } else {
                stopAutoScroll();
            }
        }
    }

    private void onDragStarted(DragEvent event) {
        mDragState = DragState.DRAG_STARTED;
        mDragItem = (DragItemAdapter.DragItem) event.getLocalState();
        mDragItemImage.createBitmap(mDragItem.mItemView);
        mDragItemImage.setCenterX(mDragItem.mItemView.getX() + mDragItem.mItemView.getWidth() / 2);
        mDragItemImage.setCenterY(mDragItem.mItemView.getY() + mDragItem.mItemView.getHeight() / 2);
        mDragItemImage.startStartAnimation();

        mItemPosition = mAdapter.getPositionForItemId(mDragItem.mItemId);
        mAdapter.setDragItem(mDragItem);
        mAdapter.notifyItemChanged(mItemPosition);
        if (mListener != null) {
            mListener.onDragStarted(mItemPosition);
        }

        invalidate();
    }

    private void onDragging(DragEvent event) {
        mDragState = DragState.DRAGGING;
        mItemPosition = mAdapter.getPositionForItemId(mDragItem.mItemId);
        mDragItemImage.setCenterX(event.getX());
        mDragItemImage.setCenterY(event.getY());

        if (!mAutoScrollEnabled) {
            updateDragPositionAndScroll();
        }
        invalidate();
    }

    private void onDragEnded() {
        // Need check because sometimes the framework calls drag end twice in a row
        if (mDragState == DragState.DRAG_ENDED) {
            return;
        }

        stopAutoScroll();

        final RecyclerView.ViewHolder holder = findViewHolderForPosition(mItemPosition);
        getItemAnimator().endAnimation(holder);
        setEnabled(false);
        mDragItemImage.startEndAnimation(holder.itemView, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                holder.itemView.setAlpha(1);
                mAdapter.setDragItem(null);
                mAdapter.notifyItemChanged(mItemPosition);

                // Need to postpone the end to avoid flicker
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDragState = DragState.DRAG_ENDED;
                        if (mListener != null) {
                            mListener.onDragEnded(mItemPosition);
                        }

                        mDragItem = null;
                        mDragItemImage.clearBitmap();
                        setEnabled(true);
                        invalidate();
                    }
                });
            }
        });
    }

    private class DragItemImage {

        private static final int ANIMATION_DURATION = 250;
        private float mTranslationX;
        private float mTranslationY;
        private float mCenterY;
        private float mCenterX;
        private float mAlphaValue = 1;
        private int mColor = Color.parseColor("#55FFFFFF");
        private Paint mPaint = new Paint();
        private Bitmap mBitmap;
        private boolean mDrawBackgroundColor;

        public void draw(Canvas canvas) {
            if (mBitmap != null) {
                canvas.save();
                canvas.translate(mTranslationX, mTranslationY);

                final float top = mCenterY - mBitmap.getHeight() / 2;
                final float bottom = top + mBitmap.getHeight();
                final float left = mIsGrid ? mCenterX - mBitmap.getWidth() / 2 : 0;

                if (mDrawBackgroundColor) {
                    mPaint.setColor(mColor);
                    mPaint.setAlpha((int) (Color.alpha(mColor) * mAlphaValue));
                    canvas.drawRect(left, top, left + mBitmap.getWidth(), bottom, mPaint);
                }
                canvas.drawBitmap(mBitmap, left, top, null);
                canvas.restore();
            }
        }

        public void createBitmap(View view) {
            mDrawBackgroundColor = !view.isSelected();
            mBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mBitmap);
            view.draw(canvas);
        }

        public void clearBitmap() {
            if (mBitmap != null) {
                mBitmap.recycle();
                mBitmap = null;
            }
        }

        public void startStartAnimation() {
            Animator alpha = ObjectAnimator.ofFloat(this, "alphaValue", 0, 1);
            alpha.setInterpolator(new DecelerateInterpolator());
            alpha.setDuration(ANIMATION_DURATION);
            alpha.start();
        }

        public void startEndAnimation(View itemView, AnimatorListenerAdapter listener) {
            if (mBitmap != null) {
                float translationX = mCenterX - itemView.getX() - mBitmap.getWidth() / 2;
                float translationY = mCenterY - itemView.getY() - mBitmap.getHeight() / 2;
                setCenterX(itemView.getX() + mBitmap.getWidth() / 2);
                setCenterY(itemView.getY() + mBitmap.getHeight() / 2);

                Animator animatorX = ObjectAnimator.ofFloat(this, "translationX", mIsGrid ? translationX : 0, 0);
                Animator animatorY = ObjectAnimator.ofFloat(this, "translationY", translationY, 0);
                Animator alpha = ObjectAnimator.ofFloat(this, "alphaValue", mAlphaValue, 0);

                AnimatorSet set = new AnimatorSet();
                set.setInterpolator(new DecelerateInterpolator());
                set.playTogether(animatorX, animatorY, alpha);
                set.setDuration(ANIMATION_DURATION);
                set.addListener(listener);
                set.start();
            } else {
                listener.onAnimationEnd(null);
            }
        }

        public void setColor(int color) {
            mColor = color;
            invalidate();
        }

        public void setAlphaValue(float alphaValue) {
            mAlphaValue = alphaValue;
            invalidate();
        }

        public void setCenterX(float x) {
            mCenterX = x;
            invalidate();
        }

        public void setCenterY(float y) {
            mCenterY = y;
            invalidate();
        }

        public float getCenterX() {
            return mCenterX;
        }

        public float getCenterY() {
            return mCenterY;
        }

        public void setTranslationX(float x) {
            mTranslationX = x;
            invalidate();
        }

        public void setTranslationY(float y) {
            mTranslationY = y;
            invalidate();
        }
    }

    private class DragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    onDragStarted(event);
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    onDragging(event);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    onDragEnded();
                    break;
            }
            return true;
        }
    }
}
