package com.example.habittracker

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.ListView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.example.habittracker.conversation.OnActivityTouchListener
import kotlin.math.abs

class HandleHabitTouchListener(var act: Activity, recyclerView: RecyclerView) :
    OnItemTouchListener, OnActivityTouchListener {

    private var swipeAble = false
    private var bgWidthLeft = 1
    private var bgWidthRight = 1
    private var heightOutsideRView = 0


    private val ANIMATION_CLOSE: Long = 150
    private val ANIMATION_STANDARD: Long = 300

    private var fgViewID = 0
    private var bgViewIdLeft = 0

    //    private var bgViewIdRight = 0
    private var fgView: View? = null
    private var bgViewLeft: View? = null

    //    private var bgViewRight: View? = null
    private var mRowClickListener: OnRowClickListener? = null

    private var touchedX = 0f
    private var touchedY = 0f
    private var touchedPosition = 0
    private var mVelocityTracker: VelocityTracker? = null
    private var mBgSwipeListener: OnSwipeOptionsListener? = null
    private var isFgSwiping = false
    private var mSwipingSlop = 0

    private var screenHeight = 0
    private var mPaused = false
    private val touchSlop: Int
    private val minFlingVel: Int
    private val maxFlingVel: Int

    private var bgVisible: Boolean = false
    private var bgVisibleView: View? = null
    private var touchedView: View? = null
    private var bgVisiblePosition: Int = -1
    private var isRViewScrolling: Boolean = false
    private var fgPartialViewClicked = false
    private val rView: RecyclerView
    private var clickable = false

    init {
        val vc = ViewConfiguration.get(recyclerView.context)
        touchSlop = vc.scaledTouchSlop
        minFlingVel = vc.scaledMinimumFlingVelocity * 16
        maxFlingVel = vc.scaledMaximumFlingVelocity
        rView = recyclerView
        bgVisible = false
        bgVisiblePosition = -1
        bgVisibleView = null
        isRViewScrolling = false


        rView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                /**
                 * This will ensure that this RecyclerTouchListener is paused during recycler view scrolling.
                 * If a scroll listener is already assigned, the caller should still pass scroll changes through
                 * to this listener.
                 */
                setEnabled(newState != RecyclerView.SCROLL_STATE_DRAGGING)

                /**
                 * This is used so that clicking a row cannot be done while scrolling
                 */
                isRViewScrolling = newState != RecyclerView.SCROLL_STATE_IDLE
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            }
        })
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, motionEvent: MotionEvent): Boolean {
        return handleTouchEvent(motionEvent)
    }

    override fun onTouchEvent(rv: RecyclerView, motionEvent: MotionEvent) {
        handleTouchEvent(motionEvent)
    }

    fun setEnabled(enabled: Boolean) {
        mPaused = !enabled
    }

    fun setSwipeable(
        foregroundId: Int,
        backgroundIdLeft: Int,
        listener: OnSwipeOptionsListener?
    ): HandleHabitTouchListener {
        this.swipeAble = true
        require(!(fgViewID != 0 && foregroundId != fgViewID)) { "foregroundID does not match previously set ID" }
        fgViewID = foregroundId
        bgViewIdLeft = backgroundIdLeft
//        bgViewIdRight = backgroundIdRight
        this.mBgSwipeListener = listener

        if (act is RecyclerTouchListenerHelper) (act as RecyclerTouchListenerHelper).setOnActivityTouchListener(
            this
        )

        val displaymetrics = DisplayMetrics()
        act.windowManager.defaultDisplay.getMetrics(displaymetrics)
        screenHeight = displaymetrics.heightPixels

        return this
    }

    fun setClickable(listener: OnRowClickListener?): HandleHabitTouchListener {
        this.clickable = true
        this.mRowClickListener = listener
        return this
    }

    private fun animateFG(downView: View?, animateType: Animation, duration: Long) {
        if (animateType == Animation.CLOSE) {
            val translateAnimator = ObjectAnimator.ofFloat(
                fgView, View.TRANSLATION_X, 0f
            )
            translateAnimator.setDuration(duration)
            translateAnimator.interpolator = DecelerateInterpolator(1.5f)
            translateAnimator.start()
        }
    }

    private fun handleTouchEvent(motionEvent: MotionEvent): Boolean {
        if (swipeAble && bgWidthLeft < 2) {
            if (act.findViewById<View?>(bgViewIdLeft) != null)
                bgWidthLeft = act.findViewById<View>(bgViewIdLeft).width
//            if (act.findViewById<View?>(bgViewIdRight) != null)
//                bgWidthRight = act.findViewById<View>(bgViewIdRight).width
            heightOutsideRView = screenHeight - rView.height
        }
        when (motionEvent.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (mPaused) {
                    return false
                }
                val rect = Rect()
                val childCount = rView.childCount
                val listViewCoords = IntArray(2)
                rView.getLocationOnScreen(listViewCoords)
                // x and y values respective to the recycler view
                var x = motionEvent.rawX.toInt() - listViewCoords[0]
                var y = motionEvent.rawY.toInt() - listViewCoords[1]
                var child: View
                var i = 0
                while (i < childCount) {
                    child = rView.getChildAt(i)
                    child.getHitRect(rect)
                    if (rect.contains(x, y)) {
                        touchedView = child
                        break
                    }
                    i++
                }
                if (touchedView != null) {
                    touchedX = motionEvent.rawX
                    touchedY = motionEvent.rawY
                    touchedPosition = rView.getChildAdapterPosition(touchedView!!)
                    if (swipeAble) {
                        mVelocityTracker = VelocityTracker.obtain()
                        mVelocityTracker?.addMovement(motionEvent)

                        fgView = touchedView!!.findViewById(fgViewID)
                        bgViewLeft = touchedView!!.findViewById(bgViewIdLeft)
                        bgViewLeft?.minimumHeight = fgView?.height!!
//                        bgViewRight = touchedView!!.findViewById(bgViewIdRight)
//                        bgViewRight?.minimumHeight = fgView?.height!!

                        /*
                         * bgVisible is true when the options menu is opened
                         * This block is to register fgPartialViewClicked status - Partial view is the view that is still
                         * shown on the screen if the options width is < device width
                         */
                        if (bgVisible && fgView != null) {
                            x = motionEvent.rawX.toInt()
                            y = motionEvent.rawY.toInt()
                            fgView!!.getGlobalVisibleRect(rect)
                            fgPartialViewClicked = rect.contains(x, y)
                        } else {
                            fgPartialViewClicked = false
                        }
                    }

                    rView.getHitRect(rect)
                    if (swipeAble && bgVisible && touchedPosition != bgVisiblePosition) {
                        closeVisibleBG(null)
                    }

                }
            }

            MotionEvent.ACTION_UP -> {
                run {
                    if (mVelocityTracker == null) {
                        return false
                    }
                    // swipedLeft and swipedRight are true if the user swipes in the respective direction (no conditions)
                    var swipedLeft = false
                    var swipedRight = false
                    /*
                     * swipedLeftProper and swipedRightProper are true if user swipes in the respective direction
                     * and if certain conditions are satisfied (given some few lines below)
                     */
                    var swipedLeftProper = false
                    var swipedRightProper = false

                    val mFinalDelta = motionEvent.rawX - touchedX

                    // Determine if the swipe was significant and in the proper direction
                    if (abs(mFinalDelta.toDouble()) > bgWidthLeft * 4 / 6 && isFgSwiping) {
                        swipedLeftProper = mFinalDelta < 0

                    }

                    if (abs(mFinalDelta.toDouble()) > bgWidthRight * 4 / 6 && isFgSwiping) {
                        swipedRightProper = mFinalDelta > 0
                    }
                    // Calculate the velocity of the swipe
                    mVelocityTracker!!.addMovement(motionEvent)
                    mVelocityTracker!!.computeCurrentVelocity(1000)
                    val velocityX = mVelocityTracker!!.xVelocity
                    val absVelocityX = abs(velocityX)
                    val absVelocityY = abs(mVelocityTracker!!.yVelocity)


                    if (absVelocityX >= minFlingVel && absVelocityX <= maxFlingVel && absVelocityY < absVelocityX && isFgSwiping) {
                        swipedLeftProper =
                            velocityX < 0 == (mFinalDelta < 0)
                        swipedRightProper =
                            velocityX > 0 == (mFinalDelta > 0)
                    }
                    if (swipeAble && swipedLeftProper && touchedPosition >= 0) {
                        bgVisibleView = fgView
                        val downPosition = touchedPosition
                        closeVisibleBG(object :
                            OnSwipeListener {
                            override fun onSwipeOptionsClosed() {
                                mBgSwipeListener!!.onSwipeOption(
                                    downPosition
                                )
                            }

                            override fun onSwipeOptionsOpened() {
                            }
                        })

                    }
//                    else if (swipeAble && swipedRightProper) {
//                        bgVisibleView = fgView
//                        val downPosition = touchedPosition
//                        closeVisibleBG(object :
//                            OnSwipeListener {
//                            override fun onSwipeOptionsClosed() {
//                                mBgSwipeListener!!.onSwipeOption(
//                                    2,
//                                    downPosition
//                                )
//                            }
//
//                            override fun onSwipeOptionsOpened() {
//                            }
//                        })
//                    }
                    else if (!swipedRight && !swipedLeft) {
                        if (clickable && !bgVisible && touchedPosition >= 0 && !isRViewScrolling) {
                            mRowClickListener!!.onRowClicked(touchedPosition)
                        }
                    }

                    animateFG(bgViewLeft, bgWidthLeft, Animation.CLOSE)
//                    animateFG(bgViewRight, bgWidthRight, Animation.CLOSE)


                    animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD)
                    bgVisible = false
                    bgVisibleView = null
                    bgVisiblePosition = -1


                    // if clicked and not swiped
                    if (swipeAble) {
                        mVelocityTracker!!.recycle()
                        mVelocityTracker = null
                    }
                    touchedX = 0f
                    touchedY = 0f
                    touchedView = null
                    touchedPosition = ListView.INVALID_POSITION
                    isFgSwiping = false
//                    bgViewRight = null
                    bgViewLeft = null

                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (mVelocityTracker == null || mPaused || !swipeAble) {
                    return false
                }
                mVelocityTracker!!.addMovement(motionEvent)
                val deltaX = motionEvent.rawX - touchedX

                if (!isFgSwiping && abs(deltaX.toDouble()) > touchSlop &&
                    abs(touchSlop.toDouble()) < abs(deltaX.toDouble()) / 2
                ) {
                    isFgSwiping = true
                    mSwipingSlop = (if (deltaX > 0) touchSlop else -touchSlop)
                }
                if (swipeAble && isFgSwiping) {

                    if (bgViewLeft == null) {
                        bgViewLeft = touchedView!!.findViewById(bgViewIdLeft)
//                        bgViewLeft?.visibility = View.VISIBLE
                    }
//                    if (bgViewRight == null) {
//                        bgViewRight = touchedView!!.findViewById(bgViewIdRight)
//                    }
                    // if fg is being swiped left
                    if (deltaX < touchSlop && !bgVisible) {
                        val translateAmount = deltaX - mSwipingSlop
                        fgView!!.translationX =
                            if (abs(translateAmount.toDouble()) > bgWidthLeft) -bgWidthLeft.toFloat() else translateAmount
                        if (fgView!!.translationX > 0) fgView!!.translationX = 0f
                        bgViewLeft?.visibility = View.VISIBLE
                        animateFG(bgViewLeft, bgWidthLeft, Animation.OPEN)
                    }
//                    else if (deltaX > 0 && !bgVisible) {
//                        // Swipe right
//                        val translateAmount = deltaX + mSwipingSlop
//                        fgView!!.translationX =
//                            if (abs(translateAmount.toDouble()) > bgWidthRight) bgWidthRight.toFloat() else translateAmount
//                        if (fgView!!.translationX < 0) fgView!!.translationX = 0f
//
//                        bgViewRight?.visibility = View.VISIBLE
//                        animateFG(bgViewRight, bgWidthRight, Animation.OPEN)
//                    }
                    else if (deltaX > 0 && bgVisible) {
                        // For closing leftOptions
                        val translateAmount = (deltaX - mSwipingSlop) + bgWidthRight
                        // Swipe fg till it reaches original position. If swiped further, nothing happens (stalls at 0)
                        fgView!!.translationX = if (translateAmount < 0) 0f else translateAmount

                        animateFG(bgViewLeft, bgWidthLeft, Animation.CLOSE)
                    }
//                else if (deltaX < 0 && bgVisible) {
//                        // For closing leftOptions
//                        val translateAmount = (deltaX + mSwipingSlop) + bgWidthRight
//
//                        // Swipe fg till it reaches original position. If swiped further, nothing happens (stalls at 0)
//                        fgView!!.translationX = if (translateAmount < 0) 0f else translateAmount
//
//                        animateFG(bgViewRight, bgWidthRight, Animation.CLOSE)
//                    }
                    return true
                }
            }

        }
        return false
    }

    private fun animateFG(downView: View?, bgWidth: Int, animateType: Animation) {
        downView?.alpha = 0f
        if (animateType == Animation.OPEN) {
            val alpha = abs(fgView!!.translationX / bgWidth)
            downView?.alpha = if (alpha > 1) 1f else alpha
        } else {
            val alpha = 1 - abs(fgView!!.translationX / bgWidth)
            downView?.alpha = if (alpha < 0) 0f else alpha
        }
    }

    private fun closeVisibleBG(mSwipeCloseListener: OnSwipeListener?) {
        if (bgVisibleView == null) {
            Log.e(TAG, "No rows found for which background options are visible")
            return
        }
        val translateAnimator = ObjectAnimator.ofFloat(
            bgVisibleView,
            View.TRANSLATION_X, 0f
        )
        translateAnimator.setDuration(ANIMATION_CLOSE)
        translateAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                mSwipeCloseListener?.onSwipeOptionsClosed()
                translateAnimator.removeAllListeners()
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        translateAnimator.start()
        bgVisible = false
        bgVisibleView = null
        bgVisiblePosition = -1
    }

    interface OnSwipeListener {
        fun onSwipeOptionsClosed()

        fun onSwipeOptionsOpened()
    }

    interface OnSwipeOptionsListener {
        fun onSwipeOption(position: Int)
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }

    override fun getTouchCoordinates(ev: MotionEvent) {
    }

    companion object {
        private const val TAG = "HandleHabitTouchListener"
    }

    interface RecyclerTouchListenerHelper {
        fun setOnActivityTouchListener(listener: OnActivityTouchListener?)
    }

    private enum class Animation {
        OPEN, CLOSE
    }

    interface OnRowClickListener {
        fun onRowClicked(position: Int)
    }
}