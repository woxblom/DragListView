package com.woxthebox.dragitemrecyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
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
        mCurrentRecyclerView.onDragging(mTouchX + getScrollX() - ((View) mCurrentRecyclerView.getParent()).getX(),
                mTouchY - mCurrentRecyclerView.getY());

        float scrollEdge = getResources().getDisplayMetrics().widthPixels*0.15f;
        if (mTouchX > getWidth() - scrollEdge && getScrollX() < mLayout.getWidth()) {
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
            canvas.translate(((View) mCurrentRecyclerView.getParent()).getX(), mCurrentRecyclerView.getY());
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
        mLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        addView(mLayout);
    }

    public DragItemRecyclerView addColumnList(final DragItemAdapter adapter, View header) {
        final DragItemRecyclerView recyclerView = new DragItemRecyclerView(getContext());
        recyclerView.setDragItemImage(mDragItemImage);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

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

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LayoutParams((int) (getResources().getDisplayMetrics().widthPixels * 0.86), LayoutParams.MATCH_PARENT));

        if (header != null) {
            layout.addView(header);
        }

        layout.addView(recyclerView);
        mLayout.addView(layout);

        return recyclerView;
    }
}
