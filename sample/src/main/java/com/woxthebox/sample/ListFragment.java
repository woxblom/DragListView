package com.woxthebox.sample;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.woxthebox.dragitemrecyclerview.DragItemRecyclerView;

import java.util.ArrayList;

public class ListFragment extends Fragment {

    private ArrayList<Pair<Long, String>> mItemArray;
    private DragItemRecyclerView mRecyclerView;

    public static ListFragment newInstance() {
        return new ListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_layout, container, false);

        mRecyclerView = (DragItemRecyclerView) view.findViewById(R.id.recycle_view);

        mItemArray = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            mItemArray.add(new Pair<>(Long.valueOf(i), "Item " + i));
        }

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setDragItemListener(new DragItemRecyclerView.DragItemListener() {
            @Override
            public void onDragStarted(int itemPosition, float x, float y) {
                if(isAdded()) {
                    Toast.makeText(getActivity(), "Drag started on pos: " + itemPosition, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDragging(int itemPosition, float x, float y) {

            }

            @Override
            public void onDragEnded(int newItemPosition) {
                if(isAdded()) {
                    Toast.makeText(getActivity(), "Drag ended on pos: " + newItemPosition, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDragEndedStarted(View view) {

            }
        });

        setupListRecyclerView();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("List and Grid");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_list:
                setupListRecyclerView();
                return true;
            case R.id.action_grid:
                setupGridRecyclerView();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupListRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setDragItemBackgroundColor(new ColorDrawable(getResources().getColor(R.color.list_item_background)));
        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.list_item, R.id.image, false);
        mRecyclerView.setAdapter(listAdapter);
    }

    private void setupGridRecyclerView() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        mRecyclerView.setDragItemBackgroundColor(null);
        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.grid_item, R.id.item_layout, true);
        mRecyclerView.setAdapter(listAdapter);
    }
}
