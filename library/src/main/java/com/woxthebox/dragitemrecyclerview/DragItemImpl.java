package com.woxthebox.dragitemrecyclerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.FrameLayout;

public class DragItemImpl extends DragItem {

    public DragItemImpl(Context context) {
        super(context);
    }

    @Override
    public View createDragView(Context context) {
        View view = new View(context);
        return view;
    }

    @Override
    public void bindDragView(View clickedView, View dragView) {
        Bitmap bitmap = Bitmap.createBitmap(clickedView.getWidth(), clickedView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        clickedView.draw(canvas);

        mDragView.setBackground(new BitmapDrawable(clickedView.getResources(), bitmap));
        mDragView.setLayoutParams(new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight()));
        int widthSpec = View.MeasureSpec.makeMeasureSpec(bitmap.getWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(bitmap.getHeight(), View.MeasureSpec.EXACTLY);
        dragView.measure(widthSpec, heightSpec);
    }

    @Override
    public void startDragAnimation(View hoverView) {
    }

    @Override
    public void endDragAnimation(View hoverView) {
    }
}
