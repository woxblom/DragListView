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
        compile 'com.github.woxthebox:draglistview:1.3'
    }

Add this to proguard rules, otherwise animations won't work correctly

    -keep class com.woxthebox.draglistview.** { *; }

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

    public class ItemAdapter extends DragItemAdapter<Pair<Long, String>, ItemAdapter.ViewHolder>
    ...
    public ItemAdapter(ArrayList<Pair<Long, String>> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        setHasStableIds(true);
        setItemList(list);
    }

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

[BMX Guide](https://play.google.com/store/apps/details?id=com.florianschaeffner.bmxguide)<br/>
Store your landed and marked BMX tricks in your personal trick book.

[Scheda Palestra](https://play.google.com/store/apps/details?id=it.ermete.mercurio.schedapalestra)<br/>
Italian fitness application.

[Paperplane - Travel Planning](https://play.google.com/store/apps/details?id=com.paperplane)<br/>
An intuitive travel planning app.

[DogHero](https://play.google.com/store/apps/details?id=br.com.doghero.astro)<br/>
Hosting dogs in your area.

[Photo Studio](https://play.google.com/store/apps/details?id=com.kvadgroup.photostudio)<br/>
Photo Studio is a powerful all-in-one image processing application for photographers of any level.

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
