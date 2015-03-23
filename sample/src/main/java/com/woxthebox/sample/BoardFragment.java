package com.woxthebox.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.woxthebox.dragitemrecyclerview.BoardView;
import com.woxthebox.dragitemrecyclerview.DragItemRecyclerView;

import java.util.ArrayList;

public class BoardFragment extends Fragment {

    private BoardView mBoardView;
    private int mColumns;

    public static BoardFragment newInstance() {
        return new BoardFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.board_layout, container, false);

        mBoardView = (BoardView) view.findViewById(R.id.board_view);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_board, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_column:
                addColumnList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addColumnList() {
        ArrayList mItemArray = new ArrayList<>();
        int addItems = 40;
        for (int i = 0; i < addItems; i++) {
            long id = i + mColumns * addItems;
            mItemArray.add(new Pair<>(id, "Item " + id));
        }
        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.grid_item, R.id.item_layout, true);

        DragItemRecyclerView recyclerView = mBoardView.addColumnList(listAdapter);
        recyclerView.setDragItemBackgroundColor(getResources().getColor(R.color.list_item_background));
        mColumns++;
    }
}
