package com.woxthebox.sample;

import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.woxthebox.dragitemrecyclerview.DragItemAdapter;

import java.util.ArrayList;

public class ItemAdapter extends DragItemAdapter<ItemAdapter.ViewHolder> {

    private ArrayList<Pair<Long, String>> mItemList;
    private int mLayoutId;
    private int mGrabHandleId;

    public ItemAdapter(ArrayList<Pair<Long, String>> list, int layoutId, int grabHandleId) {
        super(true);
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
        }
    }
}
