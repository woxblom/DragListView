package com.woxthebox.dragitemrecyclerview;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class BoardView extends HorizontalScrollView implements AutoScroller.AutoScrollListener {

    private FrameLayout mRootLayout;
    private LinearLayout mColumnLayout;
    private ArrayList<DragItemRecyclerView> mLists = new ArrayList<>();
    private DragItemRecyclerView mCurrentRecyclerView;
    private DragItemImage mDragItemImage;
    private AutoScroller mAutoScroller;
    private HoverListener mHoverListener;
    private View mHoverView;
    private float mTouchX;
    private float mTouchY;

    public interface HoverListener {
        public void bindHoverView(View clickedView, View hoverView);

        public void startHoverAnimation(View hoverView);

        public void endHoverAnimation(View hoverView);
    }

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
        updateHoverPosition();

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

    private void updateHoverPosition() {
        mHoverView.setX(mTouchX + getScrollX() - mHoverView.getMeasuredWidth() / 2);
        mHoverView.setY(mTouchY - mHoverView.getMeasuredHeight() / 2);
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
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mCurrentRecyclerView != null && mCurrentRecyclerView.isDragging()) {
            canvas.save();
            canvas.translate(((View) mCurrentRecyclerView.getParent()).getX(), mCurrentRecyclerView.getY());
            //mDragItemImage.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAutoScroller = new AutoScroller(getContext(), this);
        mDragItemImage = new DragItemImage(this);

        mRootLayout = new FrameLayout(getContext());
        mRootLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        mColumnLayout = new LinearLayout(getContext());
        mColumnLayout.setOrientation(LinearLayout.HORIZONTAL);
        mColumnLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        mRootLayout.addView(mColumnLayout);
        addView(mRootLayout);
    }

    public void setCustomHoverView(View view, HoverListener hoverListener) {
        mHoverListener = hoverListener;
        mHoverView = view;
        mHoverView.setVisibility(View.INVISIBLE);
        mHoverView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mRootLayout.addView(mHoverView);
    }

    public DragItemRecyclerView addColumnList(final DragItemAdapter adapter, final View header) {
        final DragItemRecyclerView recyclerView = new DragItemRecyclerView(getContext());
        recyclerView.setDragItemImage(mDragItemImage);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setDragItemListener(new DragItemRecyclerView.DragItemListener() {
            private float mNewX;
            private float mNewY;

            @Override
            public void onDragStarted(int itemPosition, float x, float y) {
                mCurrentRecyclerView = recyclerView;
                invalidate();
                mHoverView.setVisibility(View.VISIBLE);
                mHoverListener.bindHoverView(mCurrentRecyclerView.mDragItem.mItemView, mHoverView);
                mHoverListener.startHoverAnimation(mHoverView);

                mNewX = mTouchX + getScrollX() - mHoverView.getMeasuredWidth() / 2;
                mNewY = mTouchY - mHoverView.getMeasuredHeight() / 2;
                float startX = ((View) mCurrentRecyclerView.getParent()).getX() + mCurrentRecyclerView.mDragItem.mItemView.getX();
                float startY = mCurrentRecyclerView.getY() + mCurrentRecyclerView.mDragItem.mItemView.getY();
                mHoverView.setX(startX - (mHoverView.getMeasuredWidth() - mCurrentRecyclerView.mDragItem.mItemView.getMeasuredWidth()) / 2);
                mHoverView.setY(startY - (mHoverView.getMeasuredHeight() - mCurrentRecyclerView.mDragItem.mItemView.getMeasuredHeight())
                        / 2);

                PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("x", mHoverView.getX(), mNewX);
                PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("y", mHoverView.getY(), mNewY);
                ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(this, pvhX, pvhY);
                anim.setInterpolator(new DecelerateInterpolator());
                anim.setDuration(DragItemImage.ANIMATION_DURATION);
                anim.start();
            }

            @Override
            public void onDragging(int itemPosition, float x, float y) {
            }

            @Override
            public void onDragEnded(int newItemPosition) {
                mHoverView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onDragEndedStarted(View view) {
                mHoverListener.endHoverAnimation(mHoverView);

                mNewX = view.getX() + ((View) mCurrentRecyclerView.getParent()).getX() - (mHoverView.getMeasuredWidth() - view.getMeasuredWidth()) / 2;
                mNewY = view.getY() + mCurrentRecyclerView.getY() - (mHoverView.getMeasuredHeight() - view.getMeasuredHeight()) / 2;
                mHoverView.animate().setInterpolator(new DecelerateInterpolator()).setDuration(DragItemImage.ANIMATION_DURATION).x(mNewX)
                        .y(mNewY).start();
            }

            void setX(float x) {
                float moveDx = mTouchX + getScrollX() - mNewX;
                mHoverView.setX(x + moveDx - mHoverView.getMeasuredWidth() / 2);
                mHoverView.invalidate();
            }

            void setY(float y) {
                float moveDy = mTouchY - mNewY;
                mHoverView.setY(y + moveDy - mHoverView.getMeasuredHeight() / 2);
                mHoverView.invalidate();
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
        mColumnLayout.addView(layout);
        return recyclerView;
    }
}
