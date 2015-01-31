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

package com.woxthebox.sample;

import android.graphics.Color;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.woxthebox.dragitemrecyclerview.DragItemRecyclerView;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private ArrayList<Pair<Long, String>> mItemArray;
    private DragItemRecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (DragItemRecyclerView) findViewById(R.id.recycle_view);

        mItemArray = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            mItemArray.add(new Pair<>(Long.valueOf(i), "Item " + i));
        }

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setDragItemBackgroundColor(Color.parseColor("#AACCCCCC"));
        mRecyclerView.setDragItemListener(new DragItemRecyclerView.DragItemListener() {
            @Override
            public void onDragStarted(int itemPosition) {
                Toast.makeText(MainActivity.this, "Drag started on pos: "+itemPosition, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDragEnded(int newItemPosition) {
                Toast.makeText(MainActivity.this, "Drag ended on pos: "+newItemPosition, Toast.LENGTH_SHORT).show();
            }
        });

        setupListRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.list_item, R.id.item_layout);
        mRecyclerView.setAdapter(listAdapter);
    }

    private void setupGridRecyclerView() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.grid_item, R.id.item_layout);
        mRecyclerView.setAdapter(listAdapter);
    }
}
