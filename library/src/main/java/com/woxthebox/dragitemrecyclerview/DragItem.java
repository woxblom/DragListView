package com.woxthebox.dragitemrecyclerview;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

public class DragItem {
    protected static final int ANIMATION_DURATION = 250;
    private View mDragView;
    
    private float mOffsetX;
    private float mOffsetY;
    private float mPosX;
    private float mPosY;
    private float mAnimationDx;
    private float mAnimationDy;
    private boolean mCanDragHorizontally = true;

    public DragItem(Context context) {
        mDragView = new View(context);
        hide();
    }

    public DragItem(Context context, int layoutId) {
        mDragView = View.inflate(context, layoutId, null);
        hide();
    }

    public void onBindDragView(View clickedView, View dragView) {
        Bitmap bitmap = Bitmap.createBitmap(clickedView.getWidth(), clickedView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        clickedView.draw(canvas);
        dragView.setBackground(new BitmapDrawable(clickedView.getResources(), bitmap));
    }

    public void onMeasureDragView(View clickedView, View dragView) {
        dragView.setLayoutParams(new FrameLayout.LayoutParams(clickedView.getMeasuredWidth(), clickedView.getMeasuredHeight()));
        int widthSpec = View.MeasureSpec.makeMeasureSpec(clickedView.getMeasuredWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(clickedView.getMeasuredHeight(), View.MeasureSpec.EXACTLY);
        dragView.measure(widthSpec, heightSpec);
    }

    public void onStartDragAnimation(View dragView) {
    }

    public void onEndDragAnimation(View dragView) {
    }

    void setCanDragHorizontally(boolean canDragHorizontally) {
        mCanDragHorizontally = canDragHorizontally;
    }

    View getDragItemView() {
        return mDragView;
    }

    void show() {
        mDragView.setVisibility(View.VISIBLE);
    }

    void hide() {
        mDragView.setVisibility(View.INVISIBLE);
    }

    void startDrag(View startFromView, float touchX, float touchY) {
        show();
        onBindDragView(startFromView, mDragView);
        onMeasureDragView(startFromView, mDragView);
        onStartDragAnimation(mDragView);
        setPosition(touchX, touchY);

        float startX = startFromView.getX() - (mDragView.getMeasuredWidth() - startFromView.getMeasuredWidth()) / 2 + mDragView
                .getMeasuredWidth() / 2;
        float startY = startFromView.getY() - (mDragView.getMeasuredHeight() - startFromView.getMeasuredHeight()) / 2 + mDragView
                .getMeasuredHeight() / 2;
        setAnimationDx(startX - touchX);
        setAnimationDY(startY - touchY);

        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("AnimationDx", mAnimationDx, 0);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("AnimationDY", mAnimationDy, 0);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(this, pvhX, pvhY);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(ANIMATION_DURATION);
        anim.start();
    }

    void endDrag(View endToView, AnimatorListenerAdapter listener) {
        onEndDragAnimation(mDragView);

        float endX = endToView.getX() - (mDragView.getMeasuredWidth() - endToView.getMeasuredWidth()) / 2 + mDragView
                .getMeasuredWidth() / 2;
        float endY = endToView.getY() - (mDragView.getMeasuredHeight() - endToView.getMeasuredHeight()) / 2 + mDragView
                .getMeasuredHeight() / 2;
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("X", mPosX, endX);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("Y", mPosY, endY);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(this, pvhX, pvhY);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(ANIMATION_DURATION);
        anim.addListener(listener);
        anim.start();
    }

    void setAnimationDx(float x) {
        mAnimationDx = x;
        updatePosition();
    }

    void setAnimationDY(float y) {
        mAnimationDy = y;
        updatePosition();
    }

    void setX(float x) {
        mPosX = x;
        updatePosition();
    }

    void setY(float y) {
        mPosY = y;
        updatePosition();
    }

    float getX() {
        return mPosX;
    }

    float getY() {
        return mPosY;
    }

    void setPosition(float x, float y) {
        mPosX = x;
        mPosY = y;
        updatePosition();
    }

    void setOffset(float offsetX, float offsetY) {
        mOffsetX = offsetX;
        mOffsetY = offsetY;
        updatePosition();
    }

    void updatePosition() {
        if(mCanDragHorizontally) {
            mDragView.setX(mPosX + mOffsetX + mAnimationDx - mDragView.getMeasuredWidth() / 2);
        }

        mDragView.setY(mPosY + mOffsetY + mAnimationDy - mDragView.getMeasuredHeight() / 2);
        mDragView.invalidate();
    }
}
