# DragListView
DragListView can be used when you want to be able to re-order items in a list, grid or a board.
It also supports horizontal swiping of items in a list.

YouTube demo video<br>
[![Android drag and drop list and board](http://img.youtube.com/vi/Hxc7l06xhv4/0.jpg)](https://www.youtube.com/watch?v=Hxc7l06xhv4)

## Features
* Re-order items in a list, grid or board by dragging and dropping with nice animations.
* Add custom animations when the drag is starting and ending.
* Get a callback when a drag is started and ended with the position.
* Disable and enable drag and drop
* Swipe list items

## Download lib with gradle

    repositories {
        mavenCentral()
    }

    dependencies {
        compile 'com.github.woxthebox:draglistview:1.7.2'
    }

Add this to proguard rules, otherwise animations won't work correctly

    -keep class com.woxthebox.draglistview.** { *; }

## Usage
List and Grid layouts are used as example in the sample project.

  For list and grid view use the DragListView.

        mDragListView = (DragListView) view.findViewById(R.id.drag_list_view);
        mDragListView.setDragListListener(new DragListView.DragListListener() {
            @Override
            public void onItemDragStarted(int position) {
                Toast.makeText(getActivity(), "Start - position: " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                if (fromPosition != toPosition) {
                    Toast.makeText(getActivity(), "End - position: " + toPosition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mDragListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.list_item, R.id.image, false);
        mDragListView.setAdapter(listAdapter);
        mDragListView.setCanDragHorizontally(false);

  If you want to prevent to drag or drop items at certain positions the use these methods.

        // Prevents to drop an item in the top or bottom
        mDragListView.setCanNotDragAboveTopItem(true);
        mDragListView.setCanNotDragBelowBottomItem(true);

        // Set a callback so you can decide exactly which positions that is allowed to drag from and drop to
        mDragListView.setDragListCallback(new DragListView.DragListCallbackAdapter() {
            @Override
            public boolean canDragItemAtPosition(int dragPosition) {
                // Can not drag item at position 5
                return dragPosition != 5;
            }

            @Override
            public boolean canDropItemAtPosition(int dropPosition) {
                // Can not drop item at position 2
                return dropPosition != 2;
            }
        });

  A custom drag item can be provided to change the visual appearance of the dragging item.

        mDragListView.setCustomDragItem(new MyDragItem(getActivity(), R.layout.list_item));

        private static class MyDragItem extends DragItem {
            public MyDragItem(Context context, int layoutId) {
                super(context, layoutId);
            }

            @Override
            public void onBindDragView(View clickedView, View dragView) {
                CharSequence text = ((TextView) clickedView.findViewById(R.id.text)).getText();
                ((TextView) dragView.findViewById(R.id.text)).setText(text);
                dragView.setBackgroundColor(dragView.getResources().getColor(R.color.list_item_background));
            }
        }

  If you don't want items to automatically reorder when dragging you can disable that with the following code. This
  will do so you can swap two items instead of reorder one item as you are dragging. You should add a drop target drawable
  when using this feature to make it clear which item you will swap with when dropping. You can add either a background or
  foreground drop target drawable, or both.

        mDragListView.setDisableReorderWhenDragging(true);
        mDragListView.setDropTargetDrawables(myBackgroundDrawable, myForeGroundDrawable);

  To enable swiping of list items then just set a swipe listener on the DragListView.

        mDragListView.setSwipeListener(new ListSwipeHelper.OnSwipeListenerAdapter() {
            @Override
            public void onItemSwipeStarted(ListSwipeItem item) {
                mRefreshLayout.setEnabled(false);
            }

            @Override
            public void onItemSwipeEnded(ListSwipeItem item, ListSwipeItem.SwipeDirection swipedDirection) {
                mRefreshLayout.setEnabled(true);

                // Swipe to delete on left
                if (swipedDirection == ListSwipeItem.SwipeDirection.LEFT) {
                    Pair<Long, String> adapterItem = (Pair<Long, String>) item.getTag();
                    int pos = mDragListView.getAdapter().getPositionForItem(adapterItem);
                    mDragListView.getAdapter().removeItem(pos);
                }
            }
        });

   It is also possible to configure how the swiping should work on individual items by changing supported SwipeDirection and the SwipeInStyle.

        public enum SwipeDirection {
            LEFT, RIGHT, LEFT_AND_RIGHT, NONE
        }

        public enum SwipeInStyle {
            APPEAR, SLIDE
        }

        swipeItem.setSwipeInStyle(SwipeInStyle.SLIDE)
        swipeItem.setSupportedSwipeDirection(SwipeDirection.LEFT)

  The swipe item is setup from xml like this. Check out the sample app to see the details. The important thing here is to set the swipeViewId, leftViewId and rightViewId.

      <com.woxthebox.draglistview.swipe.ListSwipeItem
          xmlns:android="http://schemas.android.com/apk/res/android"
          xmlns:app="http://schemas.android.com/apk/res-auto"
          android:layout_width="match_parent"
          android:layout_height="wrap_content"
          app:leftViewId="@+id/item_left"
          app:rightViewId="@+id/item_right"
          app:swipeViewId="@+id/item_layout">
          ...
          ...
      </com.woxthebox.draglistview.swipe.ListSwipeItem>

  For a board, which is a number of horizontal columns with lists, then use BoardView. For an example with custom animations
  check the sample code. A custom header view can also be used when adding a column. This can be any view and will be attached to
  the top of the column. There are many different features that you can toggle on the BoardView as seen below. You read about them
  in the java doc of each method.

        mBoardView = (BoardView) view.findViewById(R.id.board_view);
        mBoardView.setSnapToColumnsWhenScrolling(true);
        mBoardView.setSnapToColumnWhenDragging(true);
        mBoardView.setSnapDragItemToTouch(true);
        mBoardView.setSnapToColumnInLandscape(false);
        mBoardView.setColumnSnapPosition(BoardView.ColumnSnapPosition.CENTER);
        mBoardView.setBoardListener(new BoardView.BoardListener() {
            @Override
            public void onItemDragStarted(int column, int row) {
                Toast.makeText(getActivity(), "Start - column: " + column + " row: " + row, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemDragEnded(int fromColumn, int fromRow, int toColumn, int toRow) {
                if (fromColumn != toColumn || fromRow != toRow) {
                    Toast.makeText(getActivity(), "End - column: " + toColumn + " row: " + toRow, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemChangedPosition(int oldColumn, int oldRow, int newColumn, int newRow) {
                Toast.makeText(mBoardView.getContext(), "Position changed - column: " + newColumn + " row: " + newRow, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemChangedColumn(int oldColumn, int newColumn) {
                TextView itemCount1 = (TextView) mBoardView.getHeaderView(oldColumn).findViewById(R.id.item_count);
                itemCount1.setText("" + mBoardView.getAdapter(oldColumn).getItemCount());
                TextView itemCount2 = (TextView) mBoardView.getHeaderView(newColumn).findViewById(R.id.item_count);
                itemCount2.setText("" + mBoardView.getAdapter(newColumn).getItemCount());
            }

            @Override
            public void onFocusedColumnChanged(int oldColumn, int newColumn) {
                Toast.makeText(getContext(), "Focused column changed from " + oldColumn + " to " + newColumn, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onColumnDragStarted(int position) {
                Toast.makeText(getContext(), "Column drag started from " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onColumnDragChangedPosition(int oldPosition, int newPosition) {
                Toast.makeText(getContext(), "Column changed from " + oldPosition + " to " + newPosition, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onColumnDragEnded(int position) {
                Toast.makeText(getContext(), "Column drag ended at " + position, Toast.LENGTH_SHORT).show();
            }
        });
        mBoardView.setBoardCallback(new BoardView.BoardCallback() {
            @Override
            public boolean canDragItemAtPosition(int column, int dragPosition) {
                // Add logic here to prevent an item to be dragged
                return true;
            }

            @Override
            public boolean canDropItemAtPosition(int oldColumn, int oldRow, int newColumn, int newRow) {
                // Add logic here to prevent an item to be dropped
                return true;
            }
        });
        ...
        mBoardView.addColumn(columnProperties);

  To set custom column width you can use the method below.
    /**
     * @param width the width of columns in both portrait and landscape. This must be called before {@link #addColumn} is
     *              called for the width to take effect.
     */
     public void setColumnWidth(int width)

  Methods addColumn and insert column which returns the column view are indicated as deprecated and will be removed in future versions. Instead of them, to add or insert a column you should use ColumnProperties instance, where you can set all necessary parameters to this column.

        LinearLayoutManager layoutManager = mGridLayout ? new GridLayoutManager(getContext(), 4) : new LinearLayoutManager(getContext());
        int backgroundColor = ContextCompat.getColor(getContext(), R.color.column_background);

        ColumnProperties columnProperties = ColumnProperties.Builder.newBuilder(listAdapter)
                                      .setLayoutManager(layoutManager)
                                      .setHasFixedItemSize(false)
                                      .setColumnBackgroundColor(Color.TRANSPARENT)
                                      .setItemsSectionBackgroundColor(backgroundColor)
                                      .setHeader(header)
                                      .setColumnDrugView(header)
                                      .build();

        mBoardView.addColumn(columnProperties);

  To add spacing between columns you can use BoardView parameter "columnSpacing" or indicate it programmatically via method "setColumnSpacing(int columnSpacing)" where the space should be indicated in pixels.
  The space before the first column and after the last one can be added by parameter "boardEdges" or programmatically via method "setBoardEdge(int boardEdge)" where space should be indicated in pixels as well.

  To enable dragging and reordering of columns you need to provide a column drag view when adding the column. It is the view that will
  start the column drag process when long pressed on. You can also implement a custom column drag item to control the visuals and animations.
  Check out the sample app to see how it is done. If no custom drag item is used a screenshot of the column will be used instead.

    mBoardView.setCustomColumnDragItem(new MyColumnDragItem(getActivity(), R.layout.column_drag_layout));
    mBoardView.addColumn(listAdapter, header, columnDragView, false);

  For your adapter, extend DragItemAdapter and call setItemList() with a List<T> type. setItemList() can be called anytime later to change the list.

    public class ItemAdapter extends DragItemAdapter<Pair<Long, String>, ItemAdapter.ViewHolder>
    ...
    public ItemAdapter(ArrayList<Pair<Long, String>> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        setItemList(list);
    }

  The adapter must provide unique ids for each item with the implementation of the abstract method below.

    /**
     * @return a unique id for an item at the specific position.
     */
     public abstract long getUniqueItemId(int position);

  Your ViewHolder should extend DragItemAdapter.ViewHolder and you must supply an id of the view that should respond to a drag.
  You also need to provide a boolean to the super constructor to decide if you want the drag to happen on long press or directly when touching the item.
  If you want to respond to clicks, long clicks or touch events on the itemView root layout you should not set your own click listeners.
  You should instead override onItemClick, onItemLongClicked and onItemTouch as these needs to be handled in the super class when
  disabling and enabling drag.
  
    public class ViewHolder extends DragItemAdapter.ViewHolder {
        public TextView mText;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            mText = (TextView) itemView.findViewById(R.id.text);
        }

        @Override
        public void onItemClicked(View view) {
            Toast.makeText(view.getContext(), "Item clicked", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onItemLongClicked(View view) {
            Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

## License

If you feel like it then drop me a mail at woxthebox@gmail.com and tell me what app you have included this lib in. It is always fun to hear!

    Copyright 2014 Magnus Woxblom

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
