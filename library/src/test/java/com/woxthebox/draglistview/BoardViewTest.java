/*
* Copyright (c) 2017-Present Pivotal Software, Inc. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy o
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
*/

package com.woxthebox.draglistview;

import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class BoardViewTest {
    private BoardView subject;
    private DragItemAdapter adapter;

    @Before
    public void setUp() throws Exception {
        adapter = mock(DragItemAdapter.class);
        when(adapter.hasStableIds()).thenReturn(true);

        subject = new BoardView(RuntimeEnvironment.application);
        subject.onFinishInflate();
    }

    @Test
    public void addColumnList_whenRecyclerViewLayoutIdConfigured_inflatesRecyclerViewWithLayout() {
        DragItemRecyclerView recyclerView = subject.addColumnList(adapter, mock(View.class), false, R.layout.drag_item_recycler_view);

        assertThat(recyclerView.findViewById(R.id.drag_item_recycler_view)).isNotNull();
    }

    @Test
    public void addColumnList_whenRecyclerViewLayoutIsNull_inflatesRecyclerViewSuccessfully() {
        DragItemRecyclerView recyclerView = subject.addColumnList(adapter, mock(View.class), false, null);

        assertThat(recyclerView).isNotNull();
    }

    @Test
    public void addColumnList_whenRecyclerViewLayoutNotSpecified_inflatesRecyclerViewSuccessfully() {
        DragItemRecyclerView recyclerView = subject.addColumnList(adapter, mock(View.class), false);

        assertThat(recyclerView).isNotNull();
    }
}
