package com.woxthebox.draglistview.sample;

import android.content.Context;
import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;

public class MySwipeRefreshLayout extends SwipeRefreshLayout {
    private View mScrollingView;

    public MySwipeRefreshLayout(Context context) {
        super(context);
    }

    public MySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canChildScrollUp() {
        return mScrollingView != null && ViewCompat.canScrollVertically(mScrollingView, -1);
    }

    public void setScrollingView(View scrollingView) {
        mScrollingView = scrollingView;
    }
}
