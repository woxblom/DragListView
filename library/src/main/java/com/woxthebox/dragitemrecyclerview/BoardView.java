package com.woxthebox.dragitemrecyclerview;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class BoardView extends HorizontalScrollView implements AutoScroller.AutoScrollListener {

    private FrameLayout mRootLayout;
    private LinearLayout mColumnLayout;
    private ArrayList<DragItemRecyclerView> mLists = new ArrayList<>();
    private DragItemRecyclerView mCurrentRecyclerView;
    private DragItem mDragItem;
    private AutoScroller mAutoScroller;
    private float mTouchX;
    private float mTouchY;

    public BoardView(Context context) {
        super(context);
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAutoScroller = new AutoScroller(getContext(), this);
        mDragItem = new DragItemImpl(getContext());

        mRootLayout = new FrameLayout(getContext());
        mRootLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        mColumnLayout = new LinearLayout(getContext());
        mColumnLayout.setOrientation(LinearLayout.HORIZONTAL);
        mColumnLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        mRootLayout.addView(mColumnLayout);
        mRootLayout.addView(mDragItem.getDragItemView());
        addView(mRootLayout);
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
                    if (!mAutoScroller.isAutoScrolling()) {
                        updateScrollPosition();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mAutoScroller.stopAutoScroll();
                    mCurrentRecyclerView.onDragEnded();
                    invalidate();
                    break;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onAutoScroll(int dx, int dy) {
        scrollBy(dx, dy);
        updateScrollPosition();
    }

    private void updateScrollPosition() {
        // Updated event to scrollview coordinates
        DragItemRecyclerView currentList = getCurrentRecyclerView(mTouchX + getScrollX());
        if (mCurrentRecyclerView != currentList) {
            long itemId = mCurrentRecyclerView.getDragItemId();
            Object item = mCurrentRecyclerView.removeDragItemAndEnd();
            mCurrentRecyclerView = currentList;
            mCurrentRecyclerView.addDragItemAndStart(mTouchY, item, itemId);
            mDragItem.setOffset(((View) mCurrentRecyclerView.getParent()).getX(), mCurrentRecyclerView.getY());
        }

        // Updated event to list coordinates
        mCurrentRecyclerView.onDragging(getListTouchX(mCurrentRecyclerView), getListTouchY(mCurrentRecyclerView));

        float scrollEdge = getResources().getDisplayMetrics().widthPixels * 0.15f;
        if (mTouchX > getWidth() - scrollEdge && getScrollX() < mColumnLayout.getWidth()) {
            mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.LEFT);
        } else if (mTouchX < scrollEdge && getScrollX() > 0) {
            mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.RIGHT);
        } else {
            mAutoScroller.stopAutoScroll();
        }
        invalidate();
    }

    private DragItemRecyclerView getCurrentRecyclerView(float x) {
        for (DragItemRecyclerView list : mLists) {
            View parent = (View) list.getParent();
            if (parent.getLeft() <= x && parent.getRight() > x) {
                return list;
            }
        }
        return mCurrentRecyclerView;
    }

    private boolean isDragging() {
        return mCurrentRecyclerView != null && mCurrentRecyclerView.isDragging();
    }

    private float getListTouchX(DragItemRecyclerView list) {
        return mTouchX + getScrollX() - ((View) list.getParent()).getX();
    }

    private float getListTouchY(DragItemRecyclerView list) {
        return mTouchY - list.getY();
    }

    public void setCustomDragItem(DragItem dragItem) {
        mDragItem = dragItem;
        mRootLayout.removeViewAt(1);
        mRootLayout.addView(dragItem.getDragItemView());
    }

    public DragItemRecyclerView addColumnList(final DragItemAdapter adapter, final View header) {
        final DragItemRecyclerView recyclerView = new DragItemRecyclerView(getContext());
        recyclerView.setDragItem(mDragItem);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setDragItemListener(new DragItemRecyclerView.DragItemListener() {
            @Override
            public void onDragStarted(int itemPosition, float x, float y) {
                mCurrentRecyclerView = recyclerView;
                mDragItem.setOffset(((View) mCurrentRecyclerView.getParent()).getX(), mCurrentRecyclerView.getY());
                invalidate();
            }

            @Override
            public void onDragging(int itemPosition, float x, float y) {
            }

            @Override
            public void onDragEnded(int newItemPosition) {
            }
        });
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recyclerView.setAdapter(adapter);
        adapter.setDragStartedListener(new DragItemAdapter.DragStartedListener() {
            @Override
            public void onDragStarted(View itemView, long itemId) {
                recyclerView.onDragStarted(itemView, itemId, getListTouchX(recyclerView), getListTouchY(recyclerView));
            }
        });

        mLists.add(recyclerView);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LayoutParams((int) (getResources().getDisplayMetrics().widthPixels * 0.86), LayoutParams.MATCH_PARENT));

        if (header != null) {
            layout.addView(header);
        }

        layout.addView(recyclerView);
        mColumnLayout.addView(layout);
        return recyclerView;
    }
}
