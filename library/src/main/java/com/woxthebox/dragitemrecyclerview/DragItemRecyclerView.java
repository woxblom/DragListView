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
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DragItemRecyclerView extends RecyclerView implements AutoScroller.AutoScrollListener {

    public interface DragItemListener {
        public void onDragStarted(int itemPosition, float x, float y);

        public void onDragging(int itemPosition, float x, float y);

        public void onDragEnded(int newItemPosition);

        public void onDragEndedStarted(View view);
    }

    private enum DragState {
        DRAG_STARTED, DRAGGING, DRAG_ENDED
    }

    private Handler mHandler = new Handler();
    private DragState mDragState = DragState.DRAG_ENDED;
    private DragItemAdapter mAdapter;
    public DragItemAdapter.DragItem mDragItem;
    private DragItemImage mDragItemImage;
    private DragItemListener mListener;
    private AutoScroller mAutoScroller;
    private int mDragItemPosition;
    private boolean mExternalDragItemImage;
    private boolean mHoldChangePosition;
    private float mTouchX;
    private float mTouchY;

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
        mDragItemImage = new DragItemImage(this);
        mAutoScroller = new AutoScroller(getContext(), this);
    }

    public void setDragItemListener(DragItemListener listener) {
        mListener = listener;
    }

    public void setDragItemImage(DragItemImage itemImage) {
        mDragItemImage = itemImage;
        mExternalDragItemImage = true;
    }

    public void setDragItemBackgroundColor(ColorDrawable color) {
        mDragItemImage.setColor(color);
    }

    public boolean isDragging() {
        return mDragState != DragState.DRAG_ENDED;
    }

    public long getDragItemId() {
        return mDragItem != null ? mDragItem.mItemId : -1;
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
        mAdapter.setRecyclerView(this);
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if (!(layout instanceof LinearLayoutManager)) {
            throw new RuntimeException("Layout must be an instance of LinearLayoutManager");
        }

        mDragItemImage.setIsGrid(layout instanceof GridLayoutManager);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (!mExternalDragItemImage && mDragState != DragState.DRAG_ENDED) {
            mDragItemImage.draw(canvas);
        }
    }

    @Override
    public void onAutoScroll(int dx, int dy) {
        scrollBy(dx, dy);
        updateDragPositionAndScroll();
    }

    public View findChildView(float x, float y) {
        final int count = getChildCount();
        if (y <= 0 && count > 0) {
            return getChildAt(0);
        }

        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (x >= child.getLeft() && x <= child.getRight() && y >= child.getTop() && y <= child.getBottom()) {
                return child;
            }
        }

        if (count > 0) {
            return getChildAt(count - 1);
        }
        return null;
    }

    private void updateDragPositionAndScroll() {
        View view = findChildView(mDragItemImage.getCenterX(), mDragItemImage.getCenterY());
        if (view != null) {
            int newPos = getChildPosition(view);
            if (!mHoldChangePosition && mDragItemPosition != -1 && mDragItemPosition != newPos) {
                mAdapter.changeItemPosition(mDragItemPosition, newPos);
                mDragItemPosition = newPos;
            }

            LinearLayoutManager layout = (LinearLayoutManager) getLayoutManager();
            if (mDragItemImage.getCenterY() > getHeight() - view.getHeight() / 3 && layout.findLastCompletelyVisibleItemPosition() !=
                    mAdapter.getItemCount() - 1) {
                mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.UP);
            } else if (mDragItemImage.getCenterY() < view.getHeight() / 3 && layout.findFirstCompletelyVisibleItemPosition() != 0) {
                mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.DOWN);
            } else {
                mAutoScroller.stopAutoScroll();
            }
        }
    }

    void onDragStarted(DragItemAdapter.DragItem dragItem) {
        mDragState = DragState.DRAG_STARTED;
        mDragItem = dragItem;
        mDragItemImage.createBitmap(mDragItem.mItemView);
        mDragItemImage.setCenterX(mTouchX);
        mDragItemImage.setCenterY(mTouchY);
        mDragItemImage.startStartAnimation(mDragItem.mItemView);

        mDragItemPosition = mAdapter.getPositionForItemId(mDragItem.mItemId);
        mAdapter.setDragItem(mDragItem);
        mAdapter.notifyItemChanged(mDragItemPosition);
        if (mListener != null) {
            mListener.onDragStarted(mDragItemPosition, mTouchX, mTouchY);
        }

        invalidate();
    }

    void onDragging(float x, float y) {
        mDragState = DragState.DRAGGING;
        mDragItemPosition = mAdapter.getPositionForItemId(mDragItem.mItemId);
        mDragItemImage.setCenterX(x);
        mDragItemImage.setCenterY(y);

        if (!mAutoScroller.isAutoScrolling()) {
            updateDragPositionAndScroll();
        }

        if (mListener != null) {
            mListener.onDragging(mDragItemPosition, x, y);
        }
        invalidate();
    }

    void onDragEnded() {
        // Need check because sometimes the framework calls drag end twice in a row
        if (mDragState == DragState.DRAG_ENDED) {
            return;
        }

        mAutoScroller.stopAutoScroll();

        final RecyclerView.ViewHolder holder = findViewHolderForPosition(mDragItemPosition);
        getItemAnimator().endAnimation(holder);
        setEnabled(false);
        if (mListener != null) {
            mListener.onDragEndedStarted(holder.itemView);
        }
        mDragItemImage.startEndAnimation(holder.itemView, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                holder.itemView.setAlpha(1);
                mAdapter.setDragItem(null);
                mAdapter.notifyItemChanged(mDragItemPosition);

                // Need to postpone the end to avoid flicker
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDragState = DragState.DRAG_ENDED;
                        if (mListener != null) {
                            mListener.onDragEnded(mDragItemPosition);
                        }

                        mDragItem = null;
                        mDragItemImage.clearBitmap();
                        mDragItemImage.setAlphaValue(1);
                        setEnabled(true);
                        invalidate();
                    }
                });
            }
        });
    }

    void addDragItemAndStart(float y, Object item, long itemId) {
        View child = findChildView(0, y);
        int pos = getChildPosition(child);
        if(pos != -1) {
            mDragState = DragState.DRAG_STARTED;
            mDragItem = new DragItemAdapter.DragItem(null, itemId);
            mAdapter.setDragItem(mDragItem);
            mAdapter.addItem(pos, item);
            mDragItemPosition = pos;

            mHoldChangePosition = true;
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mHoldChangePosition = false;
                }
            }, getItemAnimator().getMoveDuration());
            invalidate();
        }
    }

    Object removeDragItemAndEnd() {
        mAutoScroller.stopAutoScroll();
        Object item = mAdapter.removeItem(mDragItemPosition);
        mAdapter.setDragItem(null);
        mDragState = DragState.DRAG_ENDED;
        mDragItem = null;

        if (!mExternalDragItemImage) {
            mDragItemImage.clearBitmap();
            mDragItemImage.setAlphaValue(1);
        }

        invalidate();
        return item;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchX = event.getX();
                mTouchY = event.getY();
                break;
        }
        if (mDragState != DragState.DRAG_ENDED) {
            return true;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDragState != DragState.DRAG_ENDED) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    mTouchX = event.getX();
                    mTouchY = event.getY();
                    onDragging(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    onDragEnded();
                    break;
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
}
