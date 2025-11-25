package com.example.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewDecoration extends RecyclerView.ItemDecoration {
    private final int endOffset, rightOffset, startOffset, bottomOffset;

    public RecyclerViewDecoration(int startOffset, int rightOffset, int endOffset, int bottomOffset) {
        this.endOffset = endOffset;
        this.rightOffset = rightOffset;
        this.startOffset = startOffset;
        this.bottomOffset = bottomOffset;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int dataSize = state.getItemCount();
        int position = parent.getChildAdapterPosition(view);
//        if (dataSize == 1) {
//            outRect.set(30, 0, 0, 0);
//        } else if (dataSize > 0 && position == dataSize - 1) {
//            outRect.set(0, 0, endOffset, 0);
//        } else if (dataSize > 0 && position == 0) {
//            outRect.set(30, 0, rightOffset, 0);
//        } else {
//            outRect.set(0, 0, rightOffset, 0);
//        }
        if (dataSize == 1) {
            outRect.set(startOffset, 0, 0, 0);
        } else if (position == 0) {
            outRect.set(startOffset, 0, rightOffset, 0);
        } else if (position == dataSize - 1) {
            outRect.set(0, 0, endOffset, bottomOffset);
        } else {
            outRect.set(0, 0, rightOffset, 0);
        }

    }
}