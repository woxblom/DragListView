package com.woxthebox.dragitemrecyclerview;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public abstract class DragItem {
    protected static final int ANIMATION_DURATION = 250;
    protected View mDragView;
    
    private float mOffsetX;
    private float mOffsetY;
    private float mPosX;
    private float mPosY;
    private float mAnimationDx;
    private float mAnimationDy;

    public abstract View createDragView(Context context);
    public abstract void bindDragView(View clickedView, View dragView);
    public abstract void startDragAnimation(View dragView);
    public abstract void endDragAnimation(View dragView);

    public DragItem(Context context) {
        mDragView = createDragView(context);
        hide();
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
        bindDragView(startFromView, mDragView);
        startDragAnimation(mDragView);
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
        endDragAnimation(mDragView);

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
        mDragView.setX(mPosX + mOffsetX + mAnimationDx - mDragView.getMeasuredWidth() / 2);
        mDragView.setY(mPosY + mOffsetY + mAnimationDy - mDragView.getMeasuredHeight() / 2);
        mDragView.invalidate();
    }
}
