/*
 * Copyright 2020 Lisovyi Dmytro
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

import android.graphics.Color;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Properties of adding/inserting column to the {@link BoardView}
 *
 * Instance is created using an associated {@link Builder} object by
 * invoking its {@link Builder#build build} method.
 */
public class ColumnProperties {

    private DragItemAdapter mDragItemAdapter;
    private LayoutManager mLayoutManager;
    private List<RecyclerView.ItemDecoration> mItemDecorations;
    private boolean mHasFixedItemSize;
    private int mColumnBackgroundColor;
    private int mItemsSectionBackgroundColor;
    private View mHeader;
    private View mFooter;
    private View mColumnDragView;

    private ColumnProperties(DragItemAdapter adapter,
                             LayoutManager layoutManager,
                             List<RecyclerView.ItemDecoration> itemDecorations,
                             boolean hasFixedItemSize,
                             int columnBackgroundColor,
                             int itemsSectionBackgroundColor,
                             View columnDragView,
                             View header,
                             View footer) {
        mDragItemAdapter = adapter;
        mLayoutManager = layoutManager;
        mItemDecorations = itemDecorations;
        mHasFixedItemSize = hasFixedItemSize;
        mColumnBackgroundColor = columnBackgroundColor;
        mItemsSectionBackgroundColor = itemsSectionBackgroundColor;
        mHeader = header;
        mFooter = footer;
        mColumnDragView = columnDragView;
    }

    @NonNull DragItemAdapter getDragItemAdapter() {
        return mDragItemAdapter;
    }

    LayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    @NonNull List<RecyclerView.ItemDecoration> getItemDecorations() {
        return mItemDecorations;
    }

    boolean hasFixedItemSize() {
        return mHasFixedItemSize;
    }

    @ColorInt int getColumnBackgroundColor() {
        return mColumnBackgroundColor;
    }

    @ColorInt int getItemsSectionBackgroundColor() {
        return mItemsSectionBackgroundColor;
    }

    View getHeader() {
        return mHeader;
    }

    View getFooter() {
        return mFooter;
    }

    View getColumnDragView() {
        return mColumnDragView;
    }

    /**
     * Builder for {@link ColumnProperties}.
     */
    public static class Builder {

        private DragItemAdapter mDragItemAdapter;
        private LayoutManager mLayoutManager = null;
        private ArrayList<RecyclerView.ItemDecoration> mItemDecoration = new ArrayList<>();
        private boolean mHasFixedItemSize = false;
        private int mColumnBackgroundColor = Color.TRANSPARENT;
        private int mItemsSectionBackgroundColor = Color.TRANSPARENT;
        private View mHeader = null;
        private View mFooter = null;
        private View mColumnDragView = null;

        private Builder(@NonNull DragItemAdapter adapter) {
            mDragItemAdapter = adapter;
        }

        /**
         * Create the {@link Builder} instance with the items' adapter {@link DragItemAdapter}
         *
         * @param adapter Adapter with the items for the column.
         *
         * @return instance of the {@link Builder}
         */
        public static Builder newBuilder(@NonNull DragItemAdapter adapter) {
            return new Builder(adapter);
        }

        /**
         * Sets {@link LayoutManager} for items' list {@link RecyclerView}. By default is used {@link LinearLayoutManager}
         *
         * @param layoutManager A layout manager for items' list {@link RecyclerView}. By default is used {@link LinearLayoutManager}.
         *                      set null to use default value.
         *
         * @return instance of the {@link Builder}
         */
        public Builder setLayoutManager(LayoutManager layoutManager) {
            mLayoutManager = layoutManager;
            return this;
        }

        /**
         * Sets {@link androidx.recyclerview.widget.RecyclerView.ItemDecoration} for column items
         * Decorators will be set in the indicated order
         *
         * @param itemDecorations Decorator for items.
         *
         * @return instance of the {@link Builder}
         *
         * @see androidx.recyclerview.widget.RecyclerView.ItemDecoration
         */
        public Builder addItemDecorations(@NonNull RecyclerView.ItemDecoration... itemDecorations) {
            Collections.addAll(this.mItemDecoration, itemDecorations);
            return this;
        }

        /**
         * This method is used for {@link RecyclerView#setHasFixedSize(boolean)} of items'.
         * Set true if the items will have a fixed size and false if dynamic.
         *
         * @param hasFixedItemSize If the items will have a fixed or dynamic size. Default value is false.
         *
         * @return instance of the {@link Builder}
         */
        public Builder setHasFixedItemSize(boolean hasFixedItemSize) {
            mHasFixedItemSize = hasFixedItemSize;
            return this;
        }

        /**
         * Sets background color to whole the column.
         *
         * @param backgroundColor Color int value. Default value is {@link Color#TRANSPARENT}.
         *
         * @return instance of the {@link Builder}
         */
        public Builder setColumnBackgroundColor(@ColorInt int backgroundColor) {
            mColumnBackgroundColor = backgroundColor;
            return this;
        }

        /**
         * Sets background color to the items area only.
         *
         * @param backgroundColor Color int value. Default value is {@link Color#TRANSPARENT}.
         *
         * @return instance of the {@link Builder}
         */
        public Builder setItemsSectionBackgroundColor(@ColorInt int backgroundColor) {
            mItemsSectionBackgroundColor = backgroundColor;
            return this;
        }

        /**
         * Sets header view that will be positioned above the column
         *
         * @param header View that will be positioned above the column. Default value is null.
         *
         * @return instance of the {@link Builder}
         */
        public Builder setHeader(@Nullable View header) {
            mHeader = header;
            return this;
        }

        /**
         * Sets footer view that will be positioned below the column
         *
         * @param footer View that will be positioned below the column. Default value is null.
         *
         * @return instance of the {@link Builder}
         */
        public Builder setFooter(@Nullable View footer) {
            mFooter = footer;
            return this;
        }

        /**
         * Sets View that will act as handle to drag and drop columns. Can be null.
         *
         * @param columnDragView View that will act as handle to drag and drop columns. Default value is null.
         *
         * @return instance of the {@link Builder}
         */
        public Builder setColumnDragView(@Nullable View columnDragView) {
            mColumnDragView = columnDragView;
            return this;
        }

        /**
         * Builds a {@link ColumnProperties} with the settled parameters
         *
         * @return the {@link ColumnProperties} instance
         */
        public ColumnProperties build() {
            return new ColumnProperties(mDragItemAdapter,
                    mLayoutManager,
                    mItemDecoration,
                    mHasFixedItemSize,
                    mColumnBackgroundColor,
                    mItemsSectionBackgroundColor,
                    mColumnDragView,
                    mHeader,
                    mFooter);
        }
    }
}
