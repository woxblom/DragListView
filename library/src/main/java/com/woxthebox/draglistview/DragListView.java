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

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class DragListView extends FrameLayout {

    public interface DragListListener {
        public void onItemDragStarted(int position);

        public void onItemDragEnded(int fromPosition, int toPosition);
    }

    private DragItemRecyclerView mRecyclerView;
    private DragItem mDragItem;
    private DragListListener mDragListListener;
    private boolean mCanDragHorizontally;
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
        if (mRecyclerView.isDragging()) {
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

    private DragItemRecyclerView createRecyclerView() {
        final DragItemRecyclerView recyclerView = new DragItemRecyclerView(getContext());
        recyclerView.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setDragItemListener(new DragItemRecyclerView.DragItemListener() {
            private int mDragStartPosition;

            @Override
            public void onDragStarted(int itemPosition, float x, float y) {
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

    public void setCustomDragItem(DragItem dragItem) {
        removeViewAt(1);

        if (dragItem != null) {
            mDragItem = dragItem;
        } else {
            mDragItem = new DragItem(getContext());
        }

        mDragItem.setCanDragHorizontally(mCanDragHorizontally);
        mRecyclerView.setDragItem(mDragItem);
        addView(mDragItem.getDragItemView());
    }

    public void setCanDragHorizontally(boolean canDragHorizontally) {
        mCanDragHorizontally = canDragHorizontally;
        mDragItem.setCanDragHorizontally(canDragHorizontally);
    }
}
