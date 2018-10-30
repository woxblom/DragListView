/*
 * Copyright (c) 2017-Present Pivotal Software, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.woxthebox.draglistview;

import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertNull;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class BoardViewTest {
    private BoardView subject;
    private DragItemAdapter adapter;
    private long firstItemId;

    @Before
    public void setUp() {
        adapter = mock(DragItemAdapter.class);
        when(adapter.hasStableIds()).thenReturn(true);

        subject = new BoardView(RuntimeEnvironment.application);
        subject.onFinishInflate();
    }

    @Test
    public void getRecyclerView_beforeAddingColumnList_returnsNull() {
        assertNull(subject.getRecyclerView(0));
    }

    @Test
    public void getRecyclerView_afterAddingColumnList_createsNewRecyclerView() {
        subject.addColumn(adapter, mock(View.class), null, false);

        assertThat(subject.getRecyclerView(0)).isNotNull();
    }

    @Test
    public void columnDragging_whenDraggingItem_callsOnDragItemChangedPosition() {
        BoardView.BoardListener boardListener = mock(BoardView.BoardListener.class);
        subject.setBoardListener(boardListener);

        createColumnsAndDrag(adapter);

        verify(boardListener).onItemChangedPosition(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    public void columnDragging_whenDraggingItem_whenItemPositionChanges_callsOnDragItemChangedPosition() {
        int firstItemPosition = 3;
        BoardView.BoardListener boardListener = mock(BoardView.BoardListener.class);
        subject.setBoardListener(boardListener);
        DragItemRecyclerView column = createColumnsAndDrag(adapter);
        reset(boardListener);

        when(adapter.getPositionForItemId(firstItemId)).thenReturn(firstItemPosition);
        column.onDragging(0, 0);

        verify(boardListener).onItemChangedPosition(0, 0, 0, firstItemPosition);
    }

    private DragItemRecyclerView createColumnsAndDrag(DragItemAdapter adapter) {
        when(adapter.removeItem(anyInt())).thenReturn(mock(Object.class));
        DragItemRecyclerView column = subject.addColumn(adapter, null, null,false);
        View view = mock(View.class);
        when(view.getWidth()).thenReturn(1);
        when(view.getHeight()).thenReturn(1);
        firstItemId = 1L;
        column.startDrag(view, firstItemId, 0.2f, 0.4f);

        column.onDragging(0, 1);

        return column;
    }
}
