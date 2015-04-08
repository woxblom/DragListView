package com.woxthebox.dragitemrecyclerview;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Scroller;

import java.util.ArrayList;

public class BoardView extends HorizontalScrollView implements AutoScroller.AutoScrollListener {

    public interface BoardListener {
        public void onItemMoved(int fromColumn, int fromRow, int toColumn, int toRow);
    }

    private static final int SCROLL_ANIMATION_DURATION = 300;
    private Scroller mScroller;
    private AutoScroller mAutoScroller;
    private GestureDetector mGestureDetector;
    private FrameLayout mRootLayout;
    private LinearLayout mColumnLayout;
    private ArrayList<DragItemRecyclerView> mLists = new ArrayList<>();
    private DragItemRecyclerView mCurrentRecyclerView;
    private DragItem mDragItem;
    private BoardListener mBoardListener;
    private boolean mPageScrolling;
    private int mDragStartColumn;
    private int mDragStartRow;
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
        mGestureDetector = new GestureDetector(getContext(), new GestureListener());
        mScroller = new Scroller(getContext(), new DecelerateInterpolator(1.1f));
        mAutoScroller = new AutoScroller(getContext(), this);
        mDragItem = new DragItem(getContext());

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
        } else {
            if(mPageScrolling && mGestureDetector.onTouchEvent(event)) {
                return true;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(!mScroller.isFinished()) {
                        // View was grabbed during animation
                        mScroller.forceFinished(true);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    break;
            }
            return false;
        }
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            if (getScrollX() != x || getScrollY() != y) {
                scrollTo(x, y);
            }

            postInvalidateOnAnimation();
        } else {
            super.computeScroll();
        }
    }

    @Override
    public void onAutoScroll(int dx, int dy) {
        if (mCurrentRecyclerView.isDragging()) {
            scrollBy(dx, dy);
            updateScrollPosition();
        } else {
            mAutoScroller.stopAutoScroll();
        }
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

    private int getCurrentColumn(float x) {
        for (int i = 0; i < mLists.size(); i++) {
            RecyclerView list = mLists.get(i);
            View parent = (View) list.getParent();
            if (parent.getLeft() <= x && parent.getRight() > x) {
                return i;
            }
        }
        return 0;
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

    private int getColumnOfList(DragItemRecyclerView list) {
        int column = 0;
        for (int i = 0; i < mLists.size(); i++) {
            RecyclerView tmpList = mLists.get(i);
            if (tmpList == list) {
                column = i;
            }
        }
        return column;
    }

    public RecyclerView getRecyclerView(int column) {
        if (column > 0 && column < mLists.size()) {
            return mLists.get(column);
        }
        return null;
    }

    public int getItemCount() {
        int count = 0;
        for (DragItemRecyclerView list : mLists) {
            count += list.getAdapter().getItemCount();
        }
        return count;
    }

    public void setPageScrollingEnabled(boolean pageScrolling) {
        mPageScrolling = pageScrolling;
    }

    public void setBoardListener(BoardListener listener) {
        mBoardListener = listener;
    }

    public void setCustomDragItem(DragItem dragItem) {
        mDragItem = dragItem;
        mRootLayout.removeViewAt(1);
        mRootLayout.addView(dragItem.getDragItemView());
    }

    public DragItemRecyclerView addColumnList(final DragItemAdapter adapter, final View header, boolean hasFixedItemSize) {
        final DragItemRecyclerView recyclerView = new DragItemRecyclerView(getContext());
        recyclerView.setDragItem(mDragItem);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(hasFixedItemSize);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setDragItemListener(new DragItemRecyclerView.DragItemListener() {
            @Override
            public void onDragStarted(int itemPosition, float x, float y) {
                mDragStartColumn = getColumnOfList(recyclerView);
                mDragStartRow = itemPosition;
                mCurrentRecyclerView = recyclerView;
                mDragItem.setOffset(((View) mCurrentRecyclerView.getParent()).getX(), mCurrentRecyclerView.getY());
                invalidate();
            }

            @Override
            public void onDragging(int itemPosition, float x, float y) {
            }

            @Override
            public void onDragEnded(int newItemPosition) {
                int newColumn = getColumnOfList(recyclerView);
                if (mBoardListener != null && (mDragStartColumn != newColumn || mDragStartRow != newItemPosition)) {
                    mBoardListener.onItemMoved(mDragStartColumn, mDragStartRow, getColumnOfList(recyclerView), newItemPosition);
                }
            }
        });

        recyclerView.setAdapter(adapter);
        adapter.setDragStartedListener(new DragItemAdapter.DragStartedListener() {
            @Override
            public void onDragStarted(View itemView, long itemId) {
                recyclerView.onDragStarted(itemView, itemId, getListTouchX(recyclerView), getListTouchY(recyclerView));
            }
        });

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LayoutParams((int) (getResources().getDisplayMetrics().widthPixels * 0.86), LayoutParams.MATCH_PARENT));
        if (header != null) {
            layout.addView(header);
        }
        layout.addView(recyclerView);

        mLists.add(recyclerView);
        mColumnLayout.addView(layout);
        return recyclerView;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int column = getCurrentColumn(e2.getX() + getScrollX());
            // TODO: It is possible that we fling back to the same column as well sometimes...
            int newColumn = velocityX < 0 ? column + 1 : column - 1;
            if (newColumn < 0 || newColumn > mLists.size() - 1) {
                newColumn = newColumn < 0 ? 0 : mLists.size() - 1;
            }

            View parent = (View) mLists.get(newColumn).getParent();
            int newX = parent.getLeft() - (getMeasuredWidth() - parent.getMeasuredWidth()) / 2;
            int maxScroll = mRootLayout.getMeasuredWidth() - getMeasuredWidth();
            newX = newX < 0 ? 0 : newX;
            newX = newX > maxScroll ? maxScroll : newX;
            if (getScrollX() != newX) {
                mScroller.startScroll(getScrollX(), getScrollY(), newX - getScrollX(), 0, SCROLL_ANIMATION_DURATION);
                BoardView.this.postInvalidateOnAnimation();
            }
            return true;
        }
    }
}
