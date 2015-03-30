package com.woxthebox.dragitemrecyclerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class DragItemImage extends Drawable {

    public static final int ANIMATION_DURATION = 250;
    private float mTranslationX;
    private float mTranslationY;
    private float mCenterY;
    private float mCenterX;
    private float mAlphaValue = 1;
    private ColorDrawable mColor;
    private Paint mPaint = new Paint();
    public Bitmap mBitmap;
    private boolean mIsGrid;
    private View mParent;

    public DragItemImage(View parent) {
        mParent = parent;
    }

    public void setIsGrid(boolean isGrid) {
        mIsGrid = isGrid;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mBitmap != null) {
            canvas.save();
            canvas.translate(mTranslationX, mTranslationY);

            final float top = mCenterY - mBitmap.getHeight() / 2;
            final float bottom = top + mBitmap.getHeight();
            final float left = mIsGrid ? mCenterX - mBitmap.getWidth() / 2 : 0;

            if (mColor != null) {
                mPaint.setColor(mColor.getColor());
                mPaint.setAlpha((int) (Color.alpha(mColor.getColor()) * mAlphaValue));
                canvas.drawRect(left, top, left + mBitmap.getWidth(), bottom, mPaint);
            }

            canvas.drawBitmap(mBitmap, left, top, null);
            canvas.restore();
        }
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    public int getIntrinsicWidth() {
        return mBitmap.getWidth();
    }

    public int getIntrinsicHeight() {
        return mBitmap.getHeight();
    }

    public void createBitmap(View view) {
        mBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);
        view.draw(canvas);
    }

    public void clearBitmap() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    public void startStartAnimation(View itemView) {
        if (mBitmap != null) {
            float translationX = itemView.getX() + mBitmap.getWidth() / 2 - mCenterX;
            float translationY = itemView.getY() + mBitmap.getHeight() / 2 - mCenterY;

            Animator animatorX = ObjectAnimator.ofFloat(this, "translationX", mIsGrid ? translationX : 0, 0);
            Animator animatorY = ObjectAnimator.ofFloat(this, "translationY", translationY, 0);
            Animator alpha = ObjectAnimator.ofFloat(this, "alphaValue", 0, 1);

            AnimatorSet set = new AnimatorSet();
            set.setInterpolator(new DecelerateInterpolator());
            set.playTogether(animatorX, animatorY, alpha);
            set.setDuration(ANIMATION_DURATION);
            set.start();
        }
    }

    public void startEndAnimation(View itemView, AnimatorListenerAdapter listener) {
        if (mBitmap != null) {
            float translationX = mCenterX - itemView.getX() - mBitmap.getWidth() / 2;
            float translationY = mCenterY - itemView.getY() - mBitmap.getHeight() / 2;
            setCenterX(itemView.getX() + mBitmap.getWidth() / 2);
            setCenterY(itemView.getY() + mBitmap.getHeight() / 2);

            Animator animatorX = ObjectAnimator.ofFloat(this, "translationX", mIsGrid ? translationX : 0, 0);
            Animator animatorY = ObjectAnimator.ofFloat(this, "translationY", translationY, 0);
            Animator alpha = ObjectAnimator.ofFloat(this, "alphaValue", mAlphaValue, 0);

            AnimatorSet set = new AnimatorSet();
            set.setInterpolator(new DecelerateInterpolator());
            set.playTogether(animatorX, animatorY, alpha);
            set.setDuration(ANIMATION_DURATION);
            set.addListener(listener);
            set.start();
        } else {
            listener.onAnimationEnd(null);
        }
    }

    public void setColor(ColorDrawable color) {
        mColor = color;
        mParent.invalidate();
    }

    public void setAlphaValue(float alphaValue) {
        mAlphaValue = alphaValue;
        mParent.invalidate();
    }

    public void setCenterX(float x) {
        mCenterX = x;
        mParent.invalidate();
    }

    public void setCenterY(float y) {
        mCenterY = y;
        mParent.invalidate();
    }

    public float getCenterX() {
        return mCenterX;
    }

    public float getCenterY() {
        return mCenterY;
    }

    public void setTranslationX(float x) {
        mTranslationX = x;
        mParent.invalidate();
    }

    public void setTranslationY(float y) {
        mTranslationY = y;
        mParent.invalidate();
    }
}
