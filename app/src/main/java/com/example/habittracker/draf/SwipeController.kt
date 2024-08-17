package com.example.habittracker.draf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R

class SwipeController(
    context: Context,
    private val editCallback: (Int) -> Unit,
    private val deleteCallback: (Int) -> Unit,
    private val completeCallback: (Int) -> Unit,
    private val iconSize: Int,
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val editIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.edit_icon)
    private val deleteIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.exit_icon)
    private val completeIcon: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.complete_icon)
    private val editBackground = ColorDrawable(Color.parseColor("#aed581"))
    private val deleteBackground = ColorDrawable(Color.parseColor("#edc5c5"))
    private val completeBackground = ColorDrawable(Color.parseColor("#81c784"))
    private val iconMargin = iconSize / 9
    private var currentViewHolder: RecyclerView.ViewHolder? = null
    private var currentDx = 0f
    private var unSwipeAbleRows: ArrayList<RecyclerView.ViewHolder>? = null

    private var previouslySwipedViewHolder: RecyclerView.ViewHolder? = null

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                currentViewHolder?.let { viewHolder ->
                    val itemView = viewHolder.itemView
                    val itemHeight = itemView.height

                    val intrinsicWidth = iconSize
                    val intrinsicHeight = iconSize
                    val editIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                    val editIconBottom = editIconTop + intrinsicHeight
                    val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                    val deleteIconBottom = deleteIconTop + intrinsicHeight
                    val completeIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                    val completeIconBottom = completeIconTop + intrinsicHeight

                    val clickX = e.x
                    val clickY = e.y

                    val isDeleteIconClicked =
                        clickX > itemView.right - (iconMargin + intrinsicWidth) && clickY > deleteIconTop && clickY < deleteIconBottom

                    val isEditIconClicked =
                        clickX > itemView.right - 2 * (iconMargin + intrinsicWidth) && clickY > editIconTop && clickY < editIconBottom

                    val isCompleteIconClicked =
                        clickX > itemView.right - 3 * (iconMargin + intrinsicWidth) && clickY > completeIconTop && clickY < completeIconBottom

                    if (isDeleteIconClicked && currentDx < 0) {
                        deleteCallback(viewHolder.adapterPosition)
                        return true
                    }
                    if (isEditIconClicked && currentDx < 0) {
                        editCallback(viewHolder.adapterPosition)
                        return true
                    }

                    if (isCompleteIconClicked && currentDx < 0) {
                        completeCallback(viewHolder.adapterPosition)
                        return true
                    }
                }
                return super.onSingleTapUp(e)
            }
        })

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
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
        var translationX = dX
        val horizontalButtonAndItem = 20
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (previouslySwipedViewHolder != null && previouslySwipedViewHolder != viewHolder) {
                previouslySwipedViewHolder?.itemView?.translationX = 0f
                unSwipeAbleRows?.add(previouslySwipedViewHolder!!)
                previouslySwipedViewHolder = null
            }
            if (unSwipeAbleRows?.contains(viewHolder) == true) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    0f,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                return
            }


            previouslySwipedViewHolder = viewHolder
            currentViewHolder = viewHolder
            currentDx = dX


            val itemView = viewHolder.itemView
            val itemHeight = itemView.height

            val intrinsicWidth = iconSize
            val intrinsicHeight = iconSize
            val editIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
            val editIconBottom = editIconTop + intrinsicHeight
            val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
            val deleteIconBottom = deleteIconTop + intrinsicHeight
            val completeIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
            val completeIconBottom = completeIconTop + intrinsicHeight

            if (dX < 0) {
                completeBackground.setBounds(
                    itemView.right - 3 * (iconMargin + intrinsicWidth),
                    completeIconTop,
                    itemView.right - 2 * (iconMargin + intrinsicWidth),
                    completeIconBottom
                )
                editBackground.setBounds(
                    itemView.right - 2 * (iconMargin + intrinsicWidth),
                    editIconTop,
                    itemView.right - (iconMargin + intrinsicWidth),
                    editIconBottom
                )
                deleteBackground.setBounds(
                    itemView.right - (iconMargin + intrinsicWidth),
                    deleteIconTop,
                    itemView.right,
                    deleteIconBottom
                )
                completeIcon?.setBounds(
                    itemView.right - 3 * (iconMargin + intrinsicWidth) + iconMargin,
                    completeIconTop + iconMargin,
                    itemView.right - 2 * (iconMargin + intrinsicWidth) - iconMargin,
                    completeIconBottom - iconMargin
                )
                editIcon?.setBounds(
                    itemView.right - 2 * (iconMargin + intrinsicWidth) + iconMargin,
                    editIconTop + iconMargin,
                    itemView.right - (iconMargin + intrinsicWidth) - iconMargin,
                    editIconBottom - iconMargin
                )
                deleteIcon?.setBounds(
                    itemView.right - (iconMargin + intrinsicWidth) + iconMargin,
                    deleteIconTop + iconMargin,
                    itemView.right - iconMargin,
                    deleteIconBottom - iconMargin
                )

                completeBackground.draw(c)
                editBackground.draw(c)
                deleteBackground.draw(c)

                completeIcon?.draw(c)
                editIcon?.draw(c)
                deleteIcon?.draw(c)
            }
            translationX =
                Math.max(dX, -(3 * iconSize + 3 * iconMargin.toFloat() + horizontalButtonAndItem))
            itemView.translationX = translationX

        } else {
            c.drawColor(Color.TRANSPARENT)
        }
        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            translationX,
            dY,
            actionState,
            isCurrentlyActive
        )
    }

    fun onTouchEvent(event: MotionEvent) {
        gestureDetector.onTouchEvent(event)
    }
}
