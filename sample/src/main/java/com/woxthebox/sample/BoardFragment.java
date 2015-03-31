package com.woxthebox.sample;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.woxthebox.dragitemrecyclerview.BoardView;
import com.woxthebox.dragitemrecyclerview.DragItem;

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
        mBoardView.setCustomDragItem(new MyDragItem(getActivity()));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("Board");

        addColumnList();
        addColumnList();
        addColumnList();
        addColumnList();
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
        ArrayList mItemArray = new ArrayList<Pair<Long, String>>();
        int addItems = 20;
        for (int i = 0; i < addItems; i++) {
            long id = i + mColumns * addItems;
            mItemArray.add(new Pair<>(id, "Item " + id));
        }
        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.column_item, R.id.item_layout, true);

        View header = View.inflate(getActivity(), R.layout.column_header, null);
        ((TextView) header.findViewById(R.id.text)).setText("Column " + (mColumns + 1));

        mBoardView.addColumnList(listAdapter, header);
        mColumns++;
    }

    private static class MyDragItem extends DragItem {

        public MyDragItem(Context context) {
            super(context);
        }

        @Override
        public View createDragView(Context context) {
            View view = View.inflate(context, R.layout.column_item, null);
            CardView card = ((CardView) view.findViewById(R.id.card));
            card.setMaxCardElevation(40);
            return view;
        }

        @Override
        public void bindDragView(View clickedView, View hoverView) {
            CharSequence text = ((TextView) clickedView.findViewById(R.id.text)).getText();
            ((TextView) hoverView.findViewById(R.id.text)).setText(text);
            CardView hoverCard = ((CardView) hoverView.findViewById(R.id.card));
            CardView clickedCard = ((CardView) clickedView.findViewById(R.id.card));

            hoverCard.setCardElevation(clickedCard.getCardElevation());
            hoverCard.setForeground(clickedView.getResources().getDrawable(R.drawable.card_view_hover_foreground));

            int widthDiff = hoverCard.getPaddingLeft() - clickedCard.getPaddingLeft() + hoverCard.getPaddingRight() -
                    clickedCard.getPaddingRight();
            int heightDiff = hoverCard.getPaddingTop() - clickedCard.getPaddingTop() + hoverCard.getPaddingBottom() -
                    clickedCard.getPaddingBottom();
            int width = clickedView.getMeasuredWidth() + widthDiff;
            int height = clickedView.getMeasuredHeight() + heightDiff;
            hoverView.setLayoutParams(new FrameLayout.LayoutParams(width, height));

            int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
            hoverView.measure(widthSpec, heightSpec);
        }

        @Override
        public void startDragAnimation(View hoverView) {
            CardView hoverCard = ((CardView) hoverView.findViewById(R.id.card));
            ObjectAnimator anim = ObjectAnimator.ofFloat(hoverCard, "CardElevation", hoverCard.getCardElevation(), 40);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setDuration(ANIMATION_DURATION);
            anim.start();
        }

        @Override
        public void endDragAnimation(View hoverView) {
            CardView hoverCard = ((CardView) hoverView.findViewById(R.id.card));
            ObjectAnimator anim = ObjectAnimator.ofFloat(hoverCard, "CardElevation", hoverCard.getCardElevation(), 6);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setDuration(ANIMATION_DURATION);
            anim.start();
        }
    }
}
