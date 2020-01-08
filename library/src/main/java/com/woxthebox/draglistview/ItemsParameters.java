package com.woxthebox.draglistview;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;

public class ItemsParameters {
    private LayoutManager mLayoutManager;
    private boolean mHasFixedSize;
    private int mBackgroundColor;

    private ItemsParameters(@NonNull LayoutManager layoutManager, boolean hasFixedSize, @ColorInt int backgroundColor) {
        mLayoutManager = layoutManager;
        mHasFixedSize = hasFixedSize;
        mBackgroundColor = backgroundColor;
    }

    LayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    boolean isHasFixedSize() {
        return mHasFixedSize;
    }

    int getBackgroundColor() {
        return mBackgroundColor;
    }

    public static class Builder {
        private LayoutManager mLayoutManager;
        private boolean mHasFixedSize = false;
        private int mBackgroundColor = Color.TRANSPARENT;

        public Builder (@NonNull LayoutManager layoutManager) {
            mLayoutManager = layoutManager;
        }

        public Builder setHasFixedSize(boolean hasFixedSize) {
            this.mHasFixedSize = hasFixedSize;
            return this;
        }

        public Builder setColumnBackgroundColor(@ColorInt int backgroundColor) {
            mBackgroundColor = backgroundColor;
            return this;
        }

        public ItemsParameters build() {
            return new ItemsParameters(mLayoutManager, mHasFixedSize, mBackgroundColor);
        }
    }
}
