package com.woxthebox.draglistview;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

 class LayoutWrapper {
    private RecyclerView.LayoutManager layoutManager;

    LayoutWrapper(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    public int getOrientation() {
        if((layoutManager instanceof LinearLayoutManager
                &&((LinearLayoutManager) layoutManager).getOrientation()== OrientationHelper.HORIZONTAL)||
                (layoutManager instanceof StaggeredGridLayoutManager
                &&((StaggeredGridLayoutManager) layoutManager).getOrientation()==OrientationHelper.HORIZONTAL)) {
            return OrientationHelper.HORIZONTAL;
        }
        else {
            return OrientationHelper.VERTICAL;
        }
    }
    int findFirstVisibleItemPosition(){
        if(layoutManager instanceof LinearLayoutManager){
            return ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        else {
            int[] positions;
            positions = ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null);
            return positions[0];
        }
    }
    void scrollToPositionWithOffset(int position, int offset){
        if(layoutManager instanceof LinearLayoutManager){
            ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(position,offset);
        }
        else {
            ((StaggeredGridLayoutManager) layoutManager).scrollToPositionWithOffset(position,offset);
        }
    }
    static void checkLayoutManager(RecyclerView.LayoutManager layoutManager){
        if (!(layoutManager instanceof LinearLayoutManager || layoutManager instanceof StaggeredGridLayoutManager)) {
            throw new RuntimeException("Layout must be an instance of LinearLayoutManager or StaggeredGridLayoutManager");
        }
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return layoutManager;
    }
}
