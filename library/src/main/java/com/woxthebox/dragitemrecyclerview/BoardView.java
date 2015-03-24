package com.woxthebox.dragitemrecyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class BoardView extends HorizontalScrollView implements AutoScroller.AutoScrollListener {

    private LinearLayout mLayout;
    private ArrayList<DragItemRecyclerView> mLists = new ArrayList<>();
    private DragItemRecyclerView mCurrentRecyclerView;
    private DragItemImage mDragItemImage;
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

    private void updateScrollPosition() {
        // Updated event to scrollview coordinates
        DragItemRecyclerView currentList = getCurrentRecyclerView(mTouchX + getScrollX());
        if (mCurrentRecyclerView != currentList) {
            long itemId = mCurrentRecyclerView.getDragItemId();
            Object item = mCurrentRecyclerView.removeDragItemAndEnd();
            mCurrentRecyclerView = currentList;
            mCurrentRecyclerView.addDragItemAndStart(mTouchY, item, itemId);
        }
        // Updated event to list coordinates
        mCurrentRecyclerView.onDragging(mTouchX + getScrollX() - mCurrentRecyclerView.getX(), mTouchY);

        if (mTouchX > getWidth() - 200 && getScrollX() < mLayout.getWidth()) {
            mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.LEFT);
        } else if (mTouchX < 200 && getScrollX() > 0) {
            mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.RIGHT);
        } else {
            mAutoScroller.stopAutoScroll();
        }
        invalidate();
    }

    private DragItemRecyclerView getCurrentRecyclerView(float x) {
        for (DragItemRecyclerView list : mLists) {
            if (list.getLeft() <= x && list.getRight() > x) {
                return list;
            }
        }
        return mCurrentRecyclerView;
    }

    private boolean isDragging() {
        return mCurrentRecyclerView != null && mCurrentRecyclerView.isDragging();
    }

    @Override
    public void onAutoScroll(int dx, int dy) {
        scrollBy(dx, dy);
        updateScrollPosition();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return isDragging() || super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isDragging()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    mTouchX = event.getX();
                    mTouchY = event.getY();
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

        return super.onTouchEvent(event);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (mCurrentRecyclerView != null && mCurrentRecyclerView.isDragging()) {
            canvas.save();
            canvas.translate(mCurrentRecyclerView.getX(), 0);
            mDragItemImage.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAutoScroller = new AutoScroller(getContext(), this);
        mDragItemImage = new DragItemImage(this);
        mLayout = new LinearLayout(getContext());
        mLayout.setOrientation(LinearLayout.HORIZONTAL);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        mLayout.setLayoutParams(params);

        addView(mLayout);
    }

    public DragItemRecyclerView addColumnList(final DragItemAdapter adapter) {
        final DragItemRecyclerView recyclerView = new DragItemRecyclerView(getContext());
        recyclerView.setDragItemImage(mDragItemImage);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(720 / 2, LinearLayout.LayoutParams.MATCH_PARENT);

        recyclerView.setLayoutParams(params);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setDragItemListener(new DragItemRecyclerView.DragItemListener() {
            @Override
            public void onDragStarted(int itemPosition) {
                if (!isDragging()) {
                    mCurrentRecyclerView = recyclerView;
                    invalidate();
                }
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

        mLists.add(recyclerView);
        mLayout.addView(recyclerView);

        return recyclerView;
    }
}
