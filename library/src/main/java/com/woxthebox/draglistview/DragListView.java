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

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class DragListView extends FrameLayout {

    public interface DragListListener {
        void onItemDragStarted(int position);

        void onItemDragEnded(int fromPosition, int toPosition);
    }

    private DragItemRecyclerView mRecyclerView;
    private DragListListener mDragListListener;
    private DragItem mDragItem;
    private boolean mDragEnabled = true;
    private float mTouchX;
    private float mTouchY;

    public DragListView(Context context) {
        super(context);
    }

    public DragListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DragListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDragItem = new DragItem(getContext());
        mRecyclerView = createRecyclerView();
        mRecyclerView.setDragItem(mDragItem);
        addView(mRecyclerView);
        addView(mDragItem.getDragItemView());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean retValue = handleTouchEvent(event);
        return retValue || super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean retValue = handleTouchEvent(event);
        return retValue || super.onTouchEvent(event);
    }

    private boolean handleTouchEvent(MotionEvent event) {
        mTouchX = event.getX();
        mTouchY = event.getY();
        if (isDragging()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    mRecyclerView.onDragging(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mRecyclerView.onDragEnded();
                    break;
            }
            return true;
        }
        return false;
    }

    private DragItemRecyclerView createRecyclerView() {
        final DragItemRecyclerView recyclerView = (DragItemRecyclerView) LayoutInflater.from(getContext()).inflate(R.layout.drag_item_recycler_view, this, false);
        recyclerView.setMotionEventSplittingEnabled(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setVerticalScrollBarEnabled(false);
        recyclerView.setHorizontalScrollBarEnabled(false);
        recyclerView.setDragItemListener(new DragItemRecyclerView.DragItemListener() {
            private int mDragStartPosition;

            @Override
            public void onDragStarted(int itemPosition, float x, float y) {
                getParent().requestDisallowInterceptTouchEvent(true);
                mDragStartPosition = itemPosition;
                if (mDragListListener != null) {
                    mDragListListener.onItemDragStarted(itemPosition);
                }
            }

            @Override
            public void onDragging(int itemPosition, float x, float y) {
            }

            @Override
            public void onDragEnded(int newItemPosition) {
                if (mDragListListener != null) {
                    mDragListListener.onItemDragEnded(mDragStartPosition, newItemPosition);
                }
            }
        });
        return recyclerView;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public DragItemAdapter getAdapter() {
        if (mRecyclerView != null) {
            return (DragItemAdapter) mRecyclerView.getAdapter();
        }
        return null;
    }

    public void setAdapter(DragItemAdapter adapter, boolean hasFixedItemSize) {
        mRecyclerView.setHasFixedSize(hasFixedItemSize);
        mRecyclerView.setAdapter(adapter);
        adapter.setDragEnabled(mDragEnabled);
        adapter.setDragStartedListener(new DragItemAdapter.DragStartedListener() {
            @Override
            public void onDragStarted(View itemView, long itemId) {
                mRecyclerView.onDragStarted(itemView, itemId, mTouchX, mTouchY);
            }
        });
    }

    public void setLayoutManager(RecyclerView.LayoutManager layout) {
        mRecyclerView.setLayoutManager(layout);
    }

    public void setDragListListener(DragListListener listener) {
        mDragListListener = listener;
    }

    public boolean isDragEnabled() {
        return mDragEnabled;
    }

    public void setDragEnabled(boolean enabled) {
        mDragEnabled = enabled;
        if (mRecyclerView.getAdapter() != null) {
            ((DragItemAdapter) mRecyclerView.getAdapter()).setDragEnabled(mDragEnabled);
        }
    }

    public void setCustomDragItem(DragItem dragItem) {
        removeViewAt(1);

        DragItem newDragItem;
        if (dragItem != null) {
            newDragItem = dragItem;
        } else {
            newDragItem = new DragItem(getContext());
        }

        newDragItem.setCanDragHorizontally(mDragItem.canDragHorizontally());
        newDragItem.setSnapToTouch(mDragItem.isSnapToTouch());
        mDragItem = newDragItem;
        mRecyclerView.setDragItem(mDragItem);
        addView(mDragItem.getDragItemView());
    }

    public boolean isDragging() {
        return mRecyclerView.isDragging();
    }

    public void setCanDragHorizontally(boolean canDragHorizontally) {
        mDragItem.setCanDragHorizontally(canDragHorizontally);
    }

    public void setSnapDragItemToTouch(boolean snapToTouch) {
        mDragItem.setSnapToTouch(snapToTouch);
    }

    public void setCanNotDragAboveTopItem(boolean canNotDragAboveTop) {
        mRecyclerView.setCanNotDragAboveTopItem(canNotDragAboveTop);
    }

    public void setScrollingEnabled(boolean scrollingEnabled) {
        mRecyclerView.setScrollingEnabled(scrollingEnabled);
    }
}
