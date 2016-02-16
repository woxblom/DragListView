# DragListView
DragListView can be used when you want to be able to re-order items in a list, grid or a board.

Youtube demo video<br>
[![Android drag and drop list and board](http://img.youtube.com/vi/tNgevYpyA9E/0.jpg)](https://www.youtube.com/watch?v=tNgevYpyA9E)

## Features
* Re-order items in a list, grid or board by dragging and dropping with nice animations.
* Add custom animations when the drag is starting and ending.
* Get a callback when a drag is started and ended with the position.
* Disable and enable drag and drop

## Download lib with gradle

    repositories {
        mavenCentral()
    }

    dependencies {
        compile 'com.github.woxthebox:draglistview:1.2.0'
    }

## Usage
**NOTE: The adapter must use stable ids.
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

  For a board, which is a number of horizontal columns with lists, then use BoardView. For an example with custom animations
  check the sample code. A custom header view can also be used when adding a column. This can be any view and will be attached to
  the top of the column.

        mBoardView = (BoardView) view.findViewById(R.id.board_view);
        mBoardView.setSnapToColumnsWhenScrolling(true);
        mBoardView.setSnapToColumnWhenDragging(true);
        mBoardView.setSnapDragItemToTouch(true);
        mBoardView.setBoardListener(new BoardView.BoardListener() {
              @Override
              public void onItemDragStarted(int column, int row) {
                  Toast.makeText(getActivity(), "Start - column: " + column + " row: " + row, Toast.LENGTH_SHORT).show();
              }

              @Override
              public void onItemChangedColumn(int oldColumn, int newColumn) {
                  TextView itemCount1 = (TextView) mBoardView.getHeaderView(oldColumn).findViewById(R.id.item_count);
                  itemCount1.setText("" + mBoardView.getAdapter(oldColumn).getItemCount());
                  TextView itemCount2 = (TextView) mBoardView.getHeaderView(newColumn).findViewById(R.id.item_count);
                  itemCount2.setText("" + mBoardView.getAdapter(newColumn).getItemCount());
              }

              @Override
              public void onItemDragEnded(int fromColumn, int fromRow, int toColumn, int toRow) {
                  if (fromColumn != toColumn || fromRow != toRow) {
                      Toast.makeText(getActivity(), "End - column: " + toColumn + " row: " + toRow, Toast.LENGTH_SHORT).show();
                  }
              }
        });
        ...
        mBoardView.addColumnList(listAdapter, header, false);


  For your adapter, extend DragItemAdapter and call setItemList() with a List<T> type. setItemList() can be called anytime later to change the list.
  You also need to provide a boolean to the super constructor to decide if you want the drag to happen on long press or directly when touching the item.

    public class ItemAdapter extends DragItemAdapter<Pair<Long, String>, ItemAdapter.ViewHolder>
    ...
    public ItemAdapter(ArrayList<Pair<Long, String>> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        super(dragOnLongPress);
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        setHasStableIds(true);
        setItemList(list);
    }

  Your ViewHolder should extend DragItemAdapter.ViewHolder and you must supply an id of the view that should respond to a drag.
  If you want to respond to clicks, long clicks or touch events on the itemView root layout you should not set your own click listeners.
  You should instead override onItemClick, onItemLongClicked and onItemTouch as these needs to be handled in the super class when
  disabling and enabling drag.
  
    public class ViewHolder extends DragItemAdapter.ViewHolder {
        public TextView mText;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId);
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

## Used in apps

[Projectplace](https://play.google.com/store/apps/details?id=com.projectplace.octopi)<br/>
Addresses the daily work & project collaboration needs of everyone in one tool.

[App Organizer](https://play.google.com/store/apps/details?id=com.wox.app_organizer.ui)<br/>
Helps you manage and order your apps.

[Scan It - Book Scanner](https://play.google.com/store/apps/details?id=com.diotek.scanit)<br/>
Put your heavy physical books onto your smartphone.

[EzCCS](https://play.google.com/store/apps/details?id=com.scottsware.ezccsa&hl=en)<br/>
For United Pilots and Flight Attendants that use CCS. Fast one-click access CCS. OFFLINE of viewing of CCS pages.

[Super Simple Shopping List](https://play.google.com/store/apps/details?id=com.bitwize10.supersimpleshoppinglist)<br/>
One of the simplest shopping list apps available!

[Muscle Memory](https://play.google.com/store/apps/details?id=com.binaryshrub.musclememory)<br/>
Will take the mess out of tracking your workouts.

[NEO Bookmark](https://play.google.com/store/apps/details?id=com.seyeonsoft.neobookmark.lite)<br/>
With the NEO bookmark, you can see categories and bookmarks at a glance.

## License

If you use DragItemRecyclerView code in your application please inform the author about it (*email: woxthebox@gmail.com*) like this:
> **Subject:** DragListView usage notification<br />
> **Text:** I use DragListView in {application_name} - {http://link_to_google_play}.
> I [allow | don't allow] you to mention my app in section "Applications using DragListView" on GitHub.

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
