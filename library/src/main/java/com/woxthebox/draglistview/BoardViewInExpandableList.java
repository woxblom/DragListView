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
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

import java.util.ArrayList;

public class BoardViewInExpandableList extends ScrollView implements AutoScroller.AutoScrollListener {


    public interface BoardListenerOnExpandableList {
        void onItemDragStarted(int column, int row);

        void onItemChangedColumn(int oldColumn, int newColumn);

        void onItemDragEnded(int fromColumn, int fromRow, int toColumn, int toRow);
    }

    private static final int SCROLL_ANIMATION_DURATION = 325;
    private Scroller mScroller;
    private AutoScroller mAutoScroller;
    private FrameLayout mRootLayout;
    private LinearLayout mColumnLayout;
    private ArrayList<DragItemRecyclerView> mLists = new ArrayList<>();
    private SparseArray<View> mHeaders = new SparseArray<>();
    private DragItemRecyclerView mCurrentRecyclerView;
    private DragItem mDragItem;
    private BoardListenerOnExpandableList mBoardListener;
    private boolean mSnapToColumnWhenScrolling = false;
    private boolean mSnapToColumnWhenDragging = false;
    private float mTouchX;
    private float mTouchY;
    private int mDragStartColumn;
    private int mDragStartRow;
    private boolean mHasLaidOut;
    private boolean mDragEnabled = true;

    public BoardViewInExpandableList(Context context) {
        super(context);
    }

    public BoardViewInExpandableList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoardViewInExpandableList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Resources res = getResources();

        mScroller = new Scroller(getContext(), new DecelerateInterpolator(1.1f));
        mAutoScroller = new AutoScroller(getContext(), this);
        mAutoScroller.setAutoScrollMode(AutoScroller.AutoScrollMode
                .POSITION);
        mDragItem = new DragItem(getContext());

        mRootLayout = new FrameLayout(getContext());
        mRootLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mColumnLayout = new LinearLayout(getContext());
        mColumnLayout.setOrientation(LinearLayout.VERTICAL);
        mColumnLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mColumnLayout.setMotionEventSplittingEnabled(false);

        mRootLayout.addView(mColumnLayout);
        mRootLayout.addView(mDragItem.getDragItemView());
        addView(mRootLayout);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // Snap to closes column after first layout.
        // This is needed so correct column is scrolled to after a rotation.
        if (!mHasLaidOut && snapToColumnWhenScrolling()) {
            scrollToColumn(getClosestColumn(), false);
        }
        mHasLaidOut = true;
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
        if (mLists.size() == 0) {
            return false;
        }

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
                    if (snapToColumnWhenScrolling()) {
                        scrollToColumn(getColumnOfList(mCurrentRecyclerView), true);
                    }
                    invalidate();
                    break;
            }
            return true;
        } else {
            if (snapToColumnWhenScrolling()/* && mGestureDetector.onTouchEvent(event)*/) {
                // A page fling occurred, consume event
                return true;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!mScroller.isFinished()) {
                        // View was grabbed during animation
                        mScroller.forceFinished(true);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (snapToColumnWhenScrolling()) {
                        scrollToColumn(getClosestColumn(), true);
                    }
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

            // If auto scrolling at the same time as the scroller is running,
            // then update the drag item position to prevent stuttering item
            if (mAutoScroller.isAutoScrolling()) {
                mDragItem.setPosition(getListTouchX(mCurrentRecyclerView), getListTouchY(mCurrentRecyclerView));
            }

            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            super.computeScroll();
        }
    }

    @Override
    public void onAutoScrollPositionBy(int dx, int dy) {
        if (isDragging()) {
            scrollBy(dx, dy);
            updateScrollPosition();
        } else {
            mAutoScroller.stopAutoScroll();
        }
    }

    @Override
    public void onAutoScrollColumnBy(int columns) {
        if (isDragging()) {
            DragItemRecyclerView currentList = getCurrentRecyclerViewByXY(mTouchX, getHeight() / 2 + getScrollY());
            int newColumn = getColumnOfList(currentList) + columns;
            if (columns != 0 && newColumn >= 0 && newColumn < mLists.size()) {
                scrollToColumn(newColumn, true);
            }
            updateScrollPosition();
        } else {
            mAutoScroller.stopAutoScroll();
        }
    }

    private void updateScrollPosition() {
        // Updated event to scrollview coordinates
        DragItemRecyclerView currentList = getCurrentRecyclerViewByXY(mTouchX + getScrollX(), mTouchY + getScrollY());
        if (mCurrentRecyclerView != currentList) {
            int oldColumn = getColumnOfList(mCurrentRecyclerView);
            int newColumn = getColumnOfList(currentList);
            long itemId = mCurrentRecyclerView.getDragItemId();
            Object item = mCurrentRecyclerView.removeDragItemAndEnd();
            if (item != null) {
                mCurrentRecyclerView = currentList;
                mCurrentRecyclerView.addDragItemAndStart(getListTouchX(mCurrentRecyclerView),getListTouchY(mCurrentRecyclerView), item, itemId);
                mDragItem.setOffset(((View) mCurrentRecyclerView.getParent()).getLeft(), ((View) mCurrentRecyclerView.getParent()).getTop());

                if (mBoardListener != null) {
                    mBoardListener.onItemChangedColumn(oldColumn, newColumn);
                }
            }
        }

        // Updated event to list coordinates
        mCurrentRecyclerView.onDragging(getListTouchX(mCurrentRecyclerView), getListTouchY(mCurrentRecyclerView));


        float scrollEdge = getResources().getDisplayMetrics().heightPixels * 0.14f;
        if (mTouchY > getHeight() - scrollEdge && getScrollY() < mColumnLayout.getHeight()) {
            mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.UP);
            Log.d("Scroll... ", "DOWN!!!");
        } else if (mTouchY < scrollEdge) {
            mAutoScroller.startAutoScroll(AutoScroller.ScrollDirection.DOWN);
            Log.d("Scroll... ", "UP!!!");
        } else {
            Log.d("Scroll... ", "STOPPED!!!");
            mAutoScroller.stopAutoScroll();
        }

        invalidate();
    }

    //TODO: check if mTouchX get's correctly

    private float getListTouchX(DragItemRecyclerView list) {
        return mTouchX + getScrollX() - ((View) list.getParent()).getLeft();
    }

    private float getListTouchY(DragItemRecyclerView list) {
        return mTouchY + getScrollY() - ((View) list.getParent()).getTop();
    }

    private DragItemRecyclerView getCurrentRecyclerViewByXY(float x,float y) {
        for (DragItemRecyclerView list : mLists) {
            View parent = (View) list.getParent();
            if (parent.getLeft() <= x && parent.getRight() > x && y <= parent.getBottom() && y >= parent.getTop()) {
                return list;
            }
        }
        return mCurrentRecyclerView;
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


    private int getCurrentColumn(float posY) {
        for (int i = 0; i < mLists.size(); i++) {
            RecyclerView list = mLists.get(i);
            View parent = (View) list.getParent();
            if (parent.getTop() <= posY && parent.getBottom() > posY) {
                return i;
            }
        }
        return 0;
    }


    private int getClosestColumn() {
        int middlePosY = getScrollY() + getMeasuredHeight() / 2;
        int column = 0;
        int minDiffY = Integer.MAX_VALUE;
        for (int i = 0; i < mLists.size(); i++) {
            RecyclerView list = mLists.get(i);
            int listPosY = ((View) list.getParent()).getTop();
            int diffY = Math.abs(listPosY + list.getMeasuredHeight() / 2 - middlePosY);
            if (diffY < minDiffY) {
                minDiffY = diffY;
                column = i;
            }
        }
        return column;
    }

    private boolean snapToColumnWhenScrolling() {
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        return mSnapToColumnWhenScrolling && isPortrait;
    }

    private boolean snapToColumnWhenDragging() {
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        return mSnapToColumnWhenDragging && isPortrait;
    }

    private boolean isDragging() {
        return mCurrentRecyclerView != null && mCurrentRecyclerView.isDragging();
    }

    public RecyclerView getRecyclerView(int column) {
        if (column >= 0 && column < mLists.size()) {
            return mLists.get(column);
        }
        return null;
    }

    public DragItemAdapter getAdapter(int column) {
        if (column >= 0 && column < mLists.size()) {
            return (DragItemAdapter) mLists.get(column).getAdapter();
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

    public int getItemCount(int column) {
        if (mLists.size() > column) {
            return mLists.get(column).getAdapter().getItemCount();
        }
        return 0;
    }

    public int getColumnCount() {
        return mLists.size();
    }

    public View getHeaderView(int column) {
        return mHeaders.get(column);
    }

    public void removeItem(int column, int row) {
        if (!isDragging() && mLists.size() > column && mLists.get(column).getAdapter().getItemCount() > row) {
            DragItemAdapter adapter = (DragItemAdapter) mLists.get(column).getAdapter();
            adapter.removeItem(row);
        }
    }

    public void addItem(int column, int row, Object item, boolean scrollToItem) {
        if (!isDragging() && mLists.size() > column && mLists.get(column).getAdapter().getItemCount() >= row) {
            DragItemAdapter adapter = (DragItemAdapter) mLists.get(column).getAdapter();
            adapter.addItem(row, item);
            if (scrollToItem) {
                scrollToItem(column, row, false);
            }
        }
    }

    public void moveItem(int fromColumn, int fromRow, int toColumn, int toRow, boolean scrollToItem) {
        if (!isDragging() && mLists.size() > fromColumn && mLists.get(fromColumn).getAdapter().getItemCount() > fromRow
                && mLists.size() > toColumn && mLists.get(toColumn).getAdapter().getItemCount() >= toRow) {
            DragItemAdapter adapter = (DragItemAdapter) mLists.get(fromColumn).getAdapter();
            Object item = adapter.removeItem(fromRow);
            adapter = (DragItemAdapter) mLists.get(toColumn).getAdapter();
            adapter.addItem(toRow, item);
            if (scrollToItem) {
                scrollToItem(toColumn, toRow, false);
            }
        }
    }

    public void moveItem(long itemId, int toColumn, int toRow, boolean scrollToItem) {
        for (int i = 0; i < mLists.size(); i++) {
            RecyclerView.Adapter adapter = mLists.get(i).getAdapter();
            final int count = adapter.getItemCount();
            for (int j = 0; j < count; j++) {
                long id = adapter.getItemId(j);
                if (id == itemId) {
                    moveItem(i, j, toColumn, toRow, scrollToItem);
                    return;
                }
            }
        }
    }

    public void replaceItem(int column, int row, Object item, boolean scrollToItem) {
        if (!isDragging() && mLists.size() > column && mLists.get(column).getAdapter().getItemCount() > row) {
            DragItemAdapter adapter = (DragItemAdapter) mLists.get(column).getAdapter();
            adapter.removeItem(row);
            adapter.addItem(row, item);
            if (scrollToItem) {
                scrollToItem(column, row, false);
            }
        }
    }

    public void scrollToItem(int column, int row, boolean animate) {
        if (!isDragging() && mLists.size() > column && mLists.get(column).getAdapter().getItemCount() > row) {
            mScroller.forceFinished(true);
            scrollToColumn(column, animate);
            if (animate) {
                mLists.get(column).smoothScrollToPosition(row);
            } else {
                mLists.get(column).scrollToPosition(row);
            }
        }
    }

    public void scrollToColumn(int column, boolean animate) {
        if (mLists.size() <= column) {
            return;
        }

        View parent = (View) mLists.get(column).getParent();
        int newY = parent.getTop() - (getMeasuredHeight() - parent.getMeasuredHeight()) / 2;
        int maxScroll = mRootLayout.getMeasuredHeight() - getMeasuredHeight();
        newY = newY < 0 ? 0 : newY;
        newY = newY > maxScroll ? maxScroll : newY;
        if (getScrollY() != newY) {
            mScroller.forceFinished(true);
            if (animate) {
                mScroller.startScroll(getScrollX(), getScrollY(), 0,newY - getScrollY(), SCROLL_ANIMATION_DURATION);
                ViewCompat.postInvalidateOnAnimation(this);
            } else {
                scrollTo(getScrollX(), newY);
            }
        }
    }
    public void clearBoard() {
        int count = mLists.size();
        for (int i = count - 1; i >= 0; i--) {
            mColumnLayout.removeViewAt(i);
            mHeaders.remove(i);
            mLists.remove(i);
        }
    }

    public void toggleSection(int column) {
        DragItemAdapter dragItemAdapter = (DragItemAdapter) mLists.get(column).getAdapter();
        dragItemAdapter.toggleCollapsState();
    }

    public void removeColumn(int column) {
        if (column >= 0 && mLists.size() > column) {
            mColumnLayout.removeViewAt(column);
            mHeaders.remove(column);
            mLists.remove(column);
        }
    }

    public boolean isDragEnabled() {
        return mDragEnabled;
    }

    public void setDragEnabled(boolean enabled) {
        mDragEnabled = enabled;
        if (mLists.size() > 0) {
            for (DragItemRecyclerView list : mLists) {
                list.setDragEnabled(mDragEnabled);
            }
        }
    }

    /**
     * @param snapToColumn true if scrolling should snap to columns. Only applies to portrait mode.
     */
    public void setSnapToColumnsWhenScrolling(boolean snapToColumn) {
        mSnapToColumnWhenScrolling = snapToColumn;
    }

    /**
     * @param snapToColumn true if dragging should snap to columns when dragging towards the edge. Only applies to portrait mode.
     */
    public void setSnapToColumnWhenDragging(boolean snapToColumn) {
        mSnapToColumnWhenDragging = snapToColumn;
        mAutoScroller.setAutoScrollMode(AutoScroller.AutoScrollMode
                .POSITION);
    }

    /**
     * @param snapToTouch true if the drag item should snap to touch position when a drag is started.
     */
    public void setSnapDragItemToTouch(boolean snapToTouch) {
        mDragItem.setSnapToTouch(snapToTouch);
    }

    public void setBoardListener(BoardListenerOnExpandableList listener) {
        mBoardListener = listener;
    }

    public void setCustomDragItem(DragItem dragItem) {
        DragItem newDragItem;
        if (dragItem != null) {
            newDragItem = dragItem;
        } else {
            newDragItem = new DragItem(getContext());
        }

        newDragItem.setSnapToTouch(mDragItem.isSnapToTouch());
        mDragItem = newDragItem;
        mRootLayout.removeViewAt(1);
        mRootLayout.addView(mDragItem.getDragItemView());
    }

    public DragItemRecyclerView addColumnList(final DragItemAdapter adapter, final View header, boolean hasFixedItemSize) {
        final DragItemRecyclerView recyclerView = new DragItemRecyclerView(getContext());
        recyclerView.setMotionEventSplittingEnabled(false);
        recyclerView.setDragItem(mDragItem);
        recyclerView.setLayoutParams(new GridLayoutManager.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),5));
        recyclerView.setHasFixedSize(hasFixedItemSize);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setDragItemListener(new DragItemRecyclerView.DragItemListener() {
            @Override
            public void onDragStarted(int itemPosition, float x, float y) {
                mDragStartColumn = getColumnOfList(recyclerView);
                mDragStartRow = itemPosition;
                mCurrentRecyclerView = recyclerView;
                mDragItem.setOffset(((View) mCurrentRecyclerView.getParent()).getLeft(), ((View) mCurrentRecyclerView.getParent()).getTop());
                if (mBoardListener != null) {
                    mBoardListener.onItemDragStarted(mDragStartColumn, mDragStartRow);
                }
                invalidate();
            }

            @Override
            public void onDragging(int itemPosition, float x, float y) {
            }

            @Override
            public void onDragEnded(int newItemPosition) {
                if (mBoardListener != null) {
                    mBoardListener.onItemDragEnded(mDragStartColumn, mDragStartRow, getColumnOfList(recyclerView), newItemPosition);
                }
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setDragEnabled(mDragEnabled);
        adapter.setDragStartedListener(new DragItemAdapter.DragStartCallback() {
            @Override
            public boolean startDrag(View itemView, long itemId) {
                return recyclerView.startDrag(itemView, itemId, getListTouchX(recyclerView), getListTouchY(recyclerView));
            }

            @Override
            public boolean isDragging() {
                return recyclerView.isDragging();
            }
        });

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        if (header != null) {
            layout.addView(header);
            mHeaders.put(mLists.size(), header);
        }
        layout.addView(recyclerView);

        mLists.add(recyclerView);
        mColumnLayout.addView(layout);
        return recyclerView;
    }

}
