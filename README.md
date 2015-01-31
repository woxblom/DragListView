# DragItemRecyclerView
DragItemRecyclerView can be used when you want to be able to re-order items in a list or grid.

## Features
* Re-order items in a RecyclerView list or grid by dragging and dropping
* Add a background color of the item while dragging
* Get a callback when a drag is started and ended with the position

## Usage
**NOTE: The adapter must use stable ids and only layout managers based on a LinearLayoutManager is supported. List and Grid layouts are used as example in the sample project.

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setDragItemBackgroundColor(Color.parseColor("#AACCCCCC"));
        mRecyclerView.setDragItemListener(new DragItemRecyclerView.DragItemListener() {
            @Override
            public void onDragStarted(int itemPosition) {
            }

            @Override
            public void onDragEnded(int newItemPosition) {
            }
        });
  
  For you adapter, extend DragItemAdapter and implement the following methods<br>

    private ArrayList<Pair<Long, String>> mItemList;  
    
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
  
  Your ViewHolder should extend DragItemAdapter.ViewHolder and supply a the id of the view that should respond to a drag.
  
    public class ViewHolder extends DragItemAdapter.ViewHolder {
        public TextView mText;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId);
            mText = (TextView) itemView.findViewById(R.id.text);
        }
    }

## License

If you use DragItemRecyclerView code in your application you should inform the author about it (*email: woxthebox@gmail.com*) like this:
> **Subject:** DragItemRecyclerView usage notification<br />
> **Text:** I use DragItemRecyclerView in {application_name} - {http://link_to_google_play}.
> I [allow | don't allow] to mention my app in section "Applications using DragItemRecyclerView" on GitHub.

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
