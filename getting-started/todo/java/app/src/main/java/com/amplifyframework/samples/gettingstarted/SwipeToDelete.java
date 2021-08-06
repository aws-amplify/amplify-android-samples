package com.amplifyframework.samples.gettingstarted;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;


public abstract class SwipeToDelete extends ItemTouchHelper.SimpleCallback {
    private final ColorDrawable background;
    private final int backgroundColor;
    private final Drawable deleteIcon;
    private int inHeight = 0;
    private int inWidth = 0;

    public boolean onMove(
            @NotNull RecyclerView recyclerView,
            @NotNull RecyclerView.ViewHolder viewHolder,
            @NotNull RecyclerView.ViewHolder target) {
        return false;
    }

    public void onChildDraw(
            @NotNull Canvas c,
            @NotNull RecyclerView recyclerView,
            @NotNull RecyclerView.ViewHolder viewHolder,
            float dX,
            float dY,
            int actionState,
            boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getBottom() - itemView.getTop();

        this.background.setColor(this.backgroundColor);
        this.background.setBounds(
                itemView.getRight() + (int) dX,
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom());
        this.background.draw(c);

        int iconTop = itemView.getTop() + (itemHeight - this.inHeight) / 2;
        int iconMargin = (itemHeight - this.inHeight) / 2;
        int iconLeft = itemView.getRight() - iconMargin - this.inWidth;
        int iconRight = itemView.getRight() - iconMargin;
        int iconBottom = iconTop + this.inHeight;

        this.deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        this.deleteIcon.draw(c);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    public SwipeToDelete(@NotNull Context context) {
        super(0, ItemTouchHelper.LEFT);
        this.background = new ColorDrawable();
        this.backgroundColor = ContextCompat.getColor(context, R.color.red);
        this.deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_delete_24);
        if (this.deleteIcon != null) {
            this.inHeight = this.deleteIcon.getIntrinsicHeight();
            this.inWidth = this.deleteIcon.getIntrinsicWidth();
        }
    }
}
