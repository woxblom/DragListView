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

package com.woxthebox.draglistview.sample;

import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.woxthebox.draglistview.DragItemAdapter;
import com.woxthebox.sample.R;

import java.util.ArrayList;

public class ItemAdapter extends DragItemAdapter<ItemAdapter.ViewHolder> {

    private ArrayList<Pair<Long, String>> mItemList;
    private int mLayoutId;
    private int mGrabHandleId;

    public ItemAdapter(ArrayList<Pair<Long, String>> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        super(dragOnLongPress);
        mItemList = list;
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public Object removeItem(int pos) {
        Object item = mItemList.remove(pos);
        notifyItemRemoved(pos);
        return item;
    }

    @Override
    public void addItem(int pos, Object item) {
        mItemList.add(pos, (Pair<Long, String>) item);
        notifyItemInserted(pos);
    }

    @Override
    public int getPositionForItemId(long id) {
        for (int i = 0; i < mItemList.size(); i++) {
            if (id == mItemList.get(i).first) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void changeItemPosition(int fromPos, int toPos) {
        Pair<Long, String> pair = mItemList.remove(fromPos);
        mItemList.add(toPos, pair);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        String text = mItemList.get(position).second;
        holder.mText.setText(text);
        holder.itemView.setTag(text);
    }

    @Override
    public int getItemCount() {
        return mItemList != null ? mItemList.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).first;
    }

    public class ViewHolder extends DragItemAdapter.ViewHolder {
        public TextView mText;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId);
            mText = (TextView) itemView.findViewById(R.id.text);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), "Item clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
