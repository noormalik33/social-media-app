package com.example.interfaces;

import android.view.View;

import com.example.items.ItemComments;

public interface DoubleClickListener {

    /**
     * Called when the user make a single click.
     */
    void onSingleClick(final View view);

    /**
     * Called when the user make a double click.
     */
    void onDoubleClick(final View view);
}
