/**
 * Copyright 2014 Magnus Woxblom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.woxthebox.dragitemrecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

public abstract class DragItemAdapter<VH extends DragItemAdapter.ViewHolder> extends RecyclerView.Adapter<VH> {

    private DragItemRecyclerView mRecyclerView;
    private DragItem mDragItem;
    private boolean mDragOnLongPress;

    public abstract int getPositionForItemId(long id);

    public abstract void changeItemPosition(int fromPos, int toPos);

    public DragItemAdapter(boolean dragOnLongPress) {
        mDragOnLongPress = dragOnLongPress;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        long itemId = getItemId(position);
        holder.mItemId = itemId;
        holder.itemView.setVisibility(mDragItem != null && mDragItem.mItemId == itemId ? View.INVISIBLE : View.VISIBLE);
    }

    void setRecyclerView(DragItemRecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    void setDragItem(DragItem dragItem) {
        mDragItem = dragItem;
    }

    boolean isDragOnLongPress() {
        return mDragOnLongPress;
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {
        public View mGrabHandle;
        public long mItemId;

        public ViewHolder(final View itemView, int handleResId) {
            super(itemView);
            mGrabHandle = itemView.findViewById(handleResId);

            if(mDragOnLongPress) {
                mGrabHandle.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        mRecyclerView.onDragStarted(new DragItem(itemView, mItemId));
                        return true;
                    }
                });
            } else {
                mGrabHandle.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            mRecyclerView.onDragStarted(new DragItem(itemView, mItemId));
                            return true;
                        }
                        return false;
                    }
                });
            }
        }
    }

    static class DragItem {
        public View mItemView;
        public long mItemId;

        public DragItem(View itemView, long id) {
            mItemView = itemView;
            mItemId = id;
        }
    }
}
