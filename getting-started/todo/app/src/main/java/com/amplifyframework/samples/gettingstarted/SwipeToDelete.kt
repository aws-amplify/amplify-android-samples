package com.amplifyframework.samples.gettingstarted

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

abstract class SwipeToDelete(context: Context) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#f44336")
    private val deleteIcon : Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_baseline_delete_24)
    private val inHeight = deleteIcon?.intrinsicHeight
    private val inWidth = deleteIcon?.intrinsicWidth

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top

        background.color = backgroundColor
        background.setBounds(
            itemView.right + dX.toInt(),
            itemView.top,
            itemView.right,
            itemView.bottom
        )
        background.draw(c)

        val iconTop = itemView.top + (itemHeight - inHeight!!) / 2
        val iconMargin = (itemHeight - inHeight) / 2
        val iconLeft = itemView.right - iconMargin - inWidth!!
        val iconRight = itemView.right - iconMargin
        val iconBottom = iconTop + inHeight

        deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        deleteIcon?.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}