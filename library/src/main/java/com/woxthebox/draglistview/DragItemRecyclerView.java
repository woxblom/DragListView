/**
 * Copyright 2014 Magnus Woxblom
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

class DragItemRecyclerView extends RecyclerView implements AutoScroller.AutoScrollListener {

    public interface DragItemListener {
        void onDragStarted(int itemPosition, float x, float y);

        void onDragging(int itemPosition, float x, float y);

        void onDragEnded(int newItemPosition);
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
    private boolean mHoldChangePosition;
    private int mDragItemPosition;
    private int mTouchSlop;
    private float mStartY;
    private boolean mClipToPadding;
    private boolean mCanNotDragAboveTop;
    private boolean mScrollingEnabled = true;

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
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!mScrollingEnabled) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float diffY = Math.abs(event.getY() - mStartY);
                if (diffY > mTouchSlop * 0.5) {
                    // Steal event from parent as we now only want to scroll in the list
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    public void setCanNotDragAboveTopItem(boolean canNotDragAboveTop) {
        mCanNotDragAboveTop = canNotDragAboveTop;
    }

    public void setScrollingEnabled(boolean scrollingEnabled) {
        mScrollingEnabled = scrollingEnabled;
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
    public void setClipToPadding(boolean clipToPadding) {
        super.setClipToPadding(clipToPadding);
        mClipToPadding = clipToPadding;
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
    public void onAutoScrollPositionBy(int dx, int dy) {
        if (isDragging()) {
            scrollBy(dx, dy);
            updateDragPositionAndScroll();
        } else {
            mAutoScroller.stopAutoScroll();
        }
    }

    @Override
    public void onAutoScrollColumnBy(int columns) {
    }

    private View findChildView(float x, float y) {
        final int count = getChildCount();
        if (y <= 0 && count > 0) {
            return getChildAt(0);
        }

        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
            if (x >= child.getLeft() - params.leftMargin && x <= child.getRight() + params.rightMargin
                    && y >= child.getTop() - params.topMargin && y <= child.getBottom() + params.bottomMargin) {
                return child;
            }
        }

        return null;
    }

    private void updateDragPositionAndScroll() {
        View view = findChildView(mDragItem.getX(), mDragItem.getY());
        int newPos = getChildLayoutPosition(view);
        if (newPos != -1) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
            if (!mHoldChangePosition && mDragItemPosition != -1 && mDragItemPosition != newPos) {
                // If we are not allowed to drag above top and new pos is 0 then don't do anything
                if (!(mCanNotDragAboveTop && newPos == 0)) {
                    int pos = layoutManager.findFirstVisibleItemPosition();
                    View posView = layoutManager.findViewByPosition(pos);
                    mAdapter.changeItemPosition(mDragItemPosition, newPos);
                    mDragItemPosition = newPos;

                    // Since notifyItemMoved scrolls the list we need to scroll back to where we were after the position change.
                    if (layoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
                        int topMargin = ((MarginLayoutParams) posView.getLayoutParams()).topMargin;
                        layoutManager.scrollToPositionWithOffset(pos, posView.getTop() - topMargin);
                    } else {
                        int leftMargin = ((MarginLayoutParams) posView.getLayoutParams()).leftMargin;
                        layoutManager.scrollToPositionWithOffset(pos, posView.getLeft() - leftMargin);
                    }
                }
            }

            boolean lastItemReached = false;
            boolean firstItemReached = false;
            int top = mClipToPadding ? getPaddingTop() : 0;
            int bottom = mClipToPadding ? getHeight() - getPaddingBottom() : getHeight();
            int left = mClipToPadding ? getPaddingLeft() : 0;
            int right = mClipToPadding ? getWidth() - getPaddingRight() : getWidth();
            ViewHolder lastChild = findViewHolderForLayoutPosition(mAdapter.getItemCount() - 1);
            ViewHolder firstChild = findViewHolderForLayoutPosition(0);

            // Check if first or last item has been reached
            if (layoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
                if (lastChild != null && lastChild.itemView.getBottom() <= bottom) {
                    lastItemReached = true;
                }
                if (firstChild != null && firstChild.itemView.getTop() >= top) {
                    firstItemReached = true;
                }
            } else {
                if (lastChild != null && lastChild.itemView.getRight() <= right) {
                    lastItemReached = true;
                }
                if (firstChild != null && firstChild.itemView.getLeft() >= left) {
                    firstItemReached = true;
                }
            }

            // Start auto scroll if at the edge
            if (layoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
                if (mDragItem.getY() > getHeight() - view.getHeight() / 2 && !lastItemReached) {
                    mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.UP);
                } else if (mDragItem.getY() < view.getHeight() / 2 && !firstItemReached) {
                    mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.DOWN);
                } else {
                    mAutoScroller.stopAutoScroll();
                }
            } else {
                if (mDragItem.getX() > getWidth() - view.getWidth() / 2 && !lastItemReached) {
                    mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.LEFT);
                } else if (mDragItem.getX() < view.getWidth() / 2 && !firstItemReached) {
                    mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.RIGHT);
                } else {
                    mAutoScroller.stopAutoScroll();
                }
            }
        }
    }

    void onDragStarted(View itemView, long itemId, float x, float y) {
        // If a drag is starting the parent must always be allowed to intercept
        getParent().requestDisallowInterceptTouchEvent(false);
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
        if (mDragState == DragState.DRAG_ENDED) {
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
        setEnabled(false);

        // Sometimes the holder will be null if a holder has not yet been set for the position
        final RecyclerView.ViewHolder holder = findViewHolderForAdapterPosition(mDragItemPosition);
        if (holder != null) {
            getItemAnimator().endAnimation(holder);
            mDragItem.endDrag(holder.itemView, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    holder.itemView.setAlpha(1);
                    onDragItemAnimationEnd();
                }
            });
        } else {
            onDragItemAnimationEnd();
        }
    }

    private void onDragItemAnimationEnd() {
        mAdapter.setDragItemId(-1);
        mAdapter.notifyDataSetChanged();

        mDragState = DragState.DRAG_ENDED;
        if (mListener != null) {
            mListener.onDragEnded(mDragItemPosition);
        }

        mDragItemId = -1;
        mDragItem.hide();
        setEnabled(true);
        invalidate();
    }

    void addDragItemAndStart(float y, Object item, long itemId) {
        View child = findChildView(0, y);
        int pos;
        if (child == null && getChildCount() > 0) {
            // If child is null and child count is not 0 it means that an item was
            // dragged below the last item in the list, then put it after that item
            pos = getChildLayoutPosition(getChildAt(getChildCount() - 1)) + 1;
        } else {
            pos = getChildLayoutPosition(child);
        }

        // If pos is -1 it means that the child has not been laid out yet,
        // this only happens for pos 0 as far as I know
        if (pos == -1) {
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
        if (mDragItemPosition == -1) {
            return null;
        }
        mAutoScroller.stopAutoScroll();
        Object item = mAdapter.removeItem(mDragItemPosition);
        mAdapter.setDragItemId(-1);
        mDragState = DragState.DRAG_ENDED;
        mDragItemId = -1;

        invalidate();
        return item;
    }
}
