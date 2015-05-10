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

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

public abstract class DragItemAdapter<VH extends DragItemAdapter.ViewHolder> extends RecyclerView.Adapter<VH> {

    interface DragStartedListener {
        void onDragStarted(View itemView, long itemId);
    }

    private DragStartedListener mDragStartedListener;
    private long mDragItemId = -1;
    private boolean mDragOnLongPress;
    private boolean mDragEnabled = true;

    public abstract Object removeItem(int pos);

    public abstract void addItem(int pos, Object item);

    public abstract int getPositionForItemId(long id);

    public abstract void changeItemPosition(int fromPos, int toPos);

    public DragItemAdapter(boolean dragOnLongPress) {
        mDragOnLongPress = dragOnLongPress;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        long itemId = getItemId(position);
        holder.mItemId = itemId;
        holder.itemView.setVisibility(mDragItemId == itemId ? View.INVISIBLE : View.VISIBLE);
    }

    void setDragStartedListener(DragStartedListener dragStartedListener) {
        mDragStartedListener = dragStartedListener;
    }

    void setDragItemId(long dragItemId) {
        mDragItemId = dragItemId;
    }

    void setDragEnabled(boolean enabled) {
        mDragEnabled = enabled;
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {
        public View mGrabView;
        public long mItemId;

        public ViewHolder(final View itemView, int handleResId) {
            super(itemView);
            mGrabView = itemView.findViewById(handleResId);

            if (mDragOnLongPress) {
                mGrabView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (mDragEnabled) {
                            mDragStartedListener.onDragStarted(itemView, mItemId);
                            return true;
                        }
                        if (itemView == mGrabView) {
                            return onItemLongClicked(view);
                        }
                        return false;
                    }
                });
            } else {
                mGrabView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (mDragEnabled && event.getAction() == MotionEvent.ACTION_DOWN) {
                            mDragStartedListener.onDragStarted(itemView, mItemId);
                            return true;
                        }
                        if (!mDragEnabled && itemView == mGrabView) {
                            return onTouch(view, event);
                        }
                        return false;
                    }
                });
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClicked(view);
                }
            });

            if (itemView != mGrabView) {
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return onItemLongClicked(view);
                    }
                });
                itemView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        return onItemTouch(view, event);
                    }
                });
            }
        }

        public void onItemClicked(View view) {
        }

        public boolean onItemLongClicked(View view) {
            return false;
        }

        public boolean onItemTouch(View view, MotionEvent event) {
            return false;
        }
    }
}
