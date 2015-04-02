package com.woxthebox.dragitemrecyclerview;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class DragListView extends FrameLayout {
    private DragItemRecyclerView mRecyclerView;
    private DragItem mDragItem;
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
        mDragItem = new DragItemImpl(getContext());
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

    public void setAdapter(DragItemAdapter adapter) {
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

    private DragItemRecyclerView createRecyclerView() {
        final DragItemRecyclerView recyclerView = new DragItemRecyclerView(getContext());
        recyclerView.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setDragItemListener(new DragItemRecyclerView.DragItemListener() {
            @Override
            public void onDragStarted(int itemPosition, float x, float y) {
            }

            @Override
            public void onDragging(int itemPosition, float x, float y) {
            }

            @Override
            public void onDragEnded(int newItemPosition) {
            }
        });
        return recyclerView;
    }

    public void setCustomDragItem(DragItem dragItem) {
        mDragItem = dragItem;
        removeViewAt(1);
        addView(mDragItem.getDragItemView());
    }

    public void setCanDragHorizontally(boolean canDragHorizontally) {
        mDragItem.setCanDragHorizontally(canDragHorizontally);
    }
}
