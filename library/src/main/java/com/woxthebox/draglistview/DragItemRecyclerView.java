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

package com.woxthebox.draglistview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

class DragItemRecyclerView extends RecyclerView implements AutoScroller.AutoScrollListener {

    public interface DragItemListener {
        public void onDragStarted(int itemPosition, float x, float y);

        public void onDragging(int itemPosition, float x, float y);

        public void onDragEnded(int newItemPosition);
    }

    private enum DragState {
        DRAG_STARTED, DRAGGING, DRAG_ENDED
    }

    private AutoScroller mAutoScroller;
    private DragItemListener mListener;
    private DragState mDragState = DragState.DRAG_ENDED;
    private DragItemAdapter mAdapter;
    private DragItem mDragItem;
    private long mDragItemId = -1;
    private int mDragItemPosition;
    private boolean mHoldChangePosition;

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
        mAutoScroller = new AutoScroller(getContext(), this);
    }

    public void setDragItemListener(DragItemListener listener) {
        mListener = listener;
    }

    public void setDragItem(DragItem dragItem) {
        mDragItem = dragItem;
    }

    public boolean isDragging() {
        return mDragState != DragState.DRAG_ENDED;
    }

    public long getDragItemId() {
        return mDragItemId;
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
    }

    @Override
    public void onAutoScroll(int dx, int dy) {
        if(isDragging()) {
            scrollBy(dx, dy);
            updateDragPositionAndScroll();
        } else {
            mAutoScroller.stopAutoScroll();
        }
    }

    private View findChildView(float x, float y) {
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
        View view = findChildView(mDragItem.getX(), mDragItem.getY());
        int newPos = getChildAdapterPosition(view);
        if (newPos != -1) {
            if (!mHoldChangePosition && mDragItemPosition != -1 && mDragItemPosition != newPos) {
                mAdapter.changeItemPosition(mDragItemPosition, newPos);
                mDragItemPosition = newPos;
            }

            LinearLayoutManager layout = (LinearLayoutManager) getLayoutManager();
            if (mDragItem.getY() > getHeight() - view.getHeight() / 2 && layout.findLastCompletelyVisibleItemPosition() !=
                    mAdapter.getItemCount() - 1) {
                mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.UP);
            } else if (mDragItem.getY() < view.getHeight() / 2 && layout.findFirstCompletelyVisibleItemPosition() != 0) {
                mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.DOWN);
            } else {
                mAutoScroller.stopAutoScroll();
            }
        }
    }

    void onDragStarted(View itemView, long itemId, float x, float y) {
        mDragState = DragState.DRAG_STARTED;
        mDragItemId = itemId;
        mDragItem.startDrag(itemView, x, y);
        mDragItemPosition = mAdapter.getPositionForItemId(mDragItemId);
        updateDragPositionAndScroll();

        mAdapter.setDragItemId(mDragItemId);
        mAdapter.notifyDataSetChanged();
        if (mListener != null) {
            mListener.onDragStarted(mDragItemPosition, mDragItem.getX(), mDragItem.getY());
        }

        invalidate();
    }

    void onDragging(float x, float y) {
        if(mDragState == DragState.DRAG_ENDED) {
            return;
        }

        mDragState = DragState.DRAGGING;
        mDragItemPosition = mAdapter.getPositionForItemId(mDragItemId);
        mDragItem.setPosition(x, y);

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

        final RecyclerView.ViewHolder holder = findViewHolderForAdapterPosition(mDragItemPosition);
        getItemAnimator().endAnimation(holder);
        setEnabled(false);
        mDragItem.endDrag(holder.itemView, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                holder.itemView.setAlpha(1);
                mAdapter.setDragItemId(-1);
                mAdapter.notifyDataSetChanged();

                // Need to postpone the end to avoid flicker
                post(new Runnable() {
                    @Override
                    public void run() {
                        mDragState = DragState.DRAG_ENDED;
                        if (mListener != null) {
                            mListener.onDragEnded(mDragItemPosition);
                        }

                        mDragItemId = -1;
                        mDragItem.hide();
                        setEnabled(true);
                        invalidate();
                    }
                });
            }
        });
    }

    void addDragItemAndStart(float y, Object item, long itemId) {
        View child = findChildView(0, y);
        int pos = getChildAdapterPosition(child);

        // If pos is -1 it means that the child has not been layed out yet,
        // this only happens for pos 0 as far as I know
        if(pos == -1) {
            pos = 0;
        }

        mDragState = DragState.DRAG_STARTED;
        mDragItemId = itemId;
        mAdapter.setDragItemId(mDragItemId);
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

    Object removeDragItemAndEnd() {
        mAutoScroller.stopAutoScroll();
        Object item = mAdapter.removeItem(mDragItemPosition);
        mAdapter.setDragItemId(-1);
        mDragState = DragState.DRAG_ENDED;
        mDragItemId = -1;

        invalidate();
        return item;
    }
}
