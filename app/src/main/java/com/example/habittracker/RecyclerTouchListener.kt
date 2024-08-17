package com.example.habittracker

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Color
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
import java.util.Arrays
import kotlin.math.abs

class RecyclerTouchListener(var act: Activity, recyclerView: RecyclerView) :
    OnItemTouchListener, OnActivityTouchListener {

    private var heightOutsideRView = 0

    private var swipeAble = false

    private var bgVisible: Boolean = false
    private var bgVisibleView: View? = null
    private var bgVisiblePosition: Int = -1
    private var isFgSwiping = false

    private var bgWidth = 1

    private val ANIMATION_CLOSE: Long = 150
    private val ANIMATION_STANDARD: Long = 300

    private var fgViewID = 0
    private var bgViewID = 0
    // Foreground view (to be swiped), Background view (to show)
    private var fgView: View? = null
    private var bgView: View? = null
    private var mRowClickListener: OnRowClickListener? = null
    private var mBgClickListener: OnSwipeOptionsClickListener? = null
    private val touchSlop: Int

    private var screenHeight = 0
    private var mSwipingSlop = 0
    private var mPaused = false
    private var touchedX = 0f
    private var touchedY = 0f
    private var touchedPosition = 0
    private var mVelocityTracker: VelocityTracker? = null
    private var fgPartialViewClicked = false
    private val rView: RecyclerView
    private var isRViewScrolling: Boolean = false
    private var touchedView: View? = null
    private val minFlingVel: Int
    private val maxFlingVel: Int
    var optionViews: List<Int>

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
        optionViews = ArrayList()
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

    fun setEnabled(enabled: Boolean) {
        mPaused = !enabled
    }


    override fun onInterceptTouchEvent(
        recyclerView: RecyclerView,
        motionEvent: MotionEvent
    ): Boolean {
        return handleTouchEvent(motionEvent)
    }

    override fun onTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) {
        handleTouchEvent(motionEvent)
    }


    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }

    /**
     * Gets coordinates from Activity and closes any
     * swiped rows if touch happens outside the recycler view
     */
    override fun getTouchCoordinates(ev: MotionEvent) {
        val y = ev.rawY.toInt()
        if (swipeAble && bgVisible && ev.actionMasked == MotionEvent.ACTION_DOWN && y < heightOutsideRView)
            closeVisibleBG(null)
    }

    fun setClickable(listener: OnRowClickListener?): RecyclerTouchListener {
        this.clickable = true
        this.mRowClickListener = listener
        return this
    }

    private fun animateFG(downView: View?, animateType: Animation, duration: Long) {
        if (animateType == Animation.OPEN) {
            val translateAnimator = ObjectAnimator.ofFloat(
                fgView, View.TRANSLATION_X, -bgWidth.toFloat()
            )
            translateAnimator.setDuration(duration)
            translateAnimator.interpolator = DecelerateInterpolator(1.5f)
            translateAnimator.start()
        } else if (animateType == Animation.CLOSE) {
            val translateAnimator = ObjectAnimator.ofFloat(
                fgView, View.TRANSLATION_X, 0f
            )
            translateAnimator.setDuration(duration)
            translateAnimator.interpolator = DecelerateInterpolator(1.5f)
            translateAnimator.start()
        }
    }

    private fun animateFG(
        downView: View?, animateType: Animation, duration: Long,
        mSwipeCloseListener: OnSwipeListener?
    ) {
        val translateAnimator: ObjectAnimator
        if (animateType == Animation.OPEN) {
            translateAnimator =
                ObjectAnimator.ofFloat(fgView, View.TRANSLATION_X, -bgWidth.toFloat())
            translateAnimator.setDuration(duration)
            translateAnimator.interpolator = DecelerateInterpolator(1.5f)
            translateAnimator.start()
        } else {
            translateAnimator = ObjectAnimator.ofFloat(fgView, View.TRANSLATION_X, 0f)
            translateAnimator.setDuration(duration)
            translateAnimator.interpolator = DecelerateInterpolator(1.5f)
            translateAnimator.start()
        }

        translateAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                if (mSwipeCloseListener != null) {
                    if (animateType == Animation.OPEN) mSwipeCloseListener.onSwipeOptionsOpened()
                    else if (animateType == Animation.CLOSE) mSwipeCloseListener.onSwipeOptionsClosed()
                }
                translateAnimator.removeAllListeners()
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
    }
    fun setSwipeable(
        foregroundID: Int,
        backgroundID: Int,
        listener: OnSwipeOptionsClickListener?
    ): RecyclerTouchListener {
        this.swipeAble = true
        require(!(fgViewID != 0 && foregroundID != fgViewID)) { "foregroundID does not match previously set ID" }
        fgViewID = foregroundID
        bgViewID = backgroundID
        this.mBgClickListener = listener

        if (act is RecyclerTouchListenerHelper) (act as RecyclerTouchListenerHelper).setOnActivityTouchListener(
            this
        )

        val displaymetrics = DisplayMetrics()
        act.windowManager.defaultDisplay.getMetrics(displaymetrics)
        screenHeight = displaymetrics.heightPixels

        return this
    }

    fun setSwipeOptionViews(vararg viewIds: Int?): RecyclerTouchListener {
        this.optionViews = ArrayList(Arrays.asList(*viewIds))
        return this
    }

    private fun getOptionViewID(motionEvent: MotionEvent): Int {
        for (i in optionViews.indices) {
            if (touchedView != null) {
                val rect = Rect()
                val x = motionEvent.rawX.toInt()
                val y = motionEvent.rawY.toInt()
                touchedView!!.findViewById<View>(optionViews[i]).getGlobalVisibleRect(rect)
                if (rect.contains(x, y)) {
                    return optionViews[i]
                }
            }
        }
        return -1
    }


    private fun handleTouchEvent(motionEvent: MotionEvent): Boolean {
        if (swipeAble && bgWidth < 2) {
            if (act.findViewById<View?>(bgViewID) != null) bgWidth =
                act.findViewById<View>(bgViewID).width

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
                        bgView = touchedView!!.findViewById(bgViewID)
                        //                        bgView.getLayoutParams().height = fgView.getHeight();
                        bgView?.minimumHeight = fgView?.height!!
                        bgView?.minimumHeight = fgView?.height!!

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
                    if (mVelocityTracker == null && swipeAble) {
                        return false
                    }
                    if (touchedPosition < 0) return false

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

                    // if swiped in a direction, make that respective variable true
                    if (isFgSwiping) {
                        swipedLeft = mFinalDelta < 0
                        swipedRight = mFinalDelta > 0
                    }
                    if (abs(mFinalDelta.toDouble()) > bgWidth / 2 && isFgSwiping) {
                        swipedLeftProper = mFinalDelta < 0
                        swipedRightProper = mFinalDelta > 0
                    } else if (swipeAble) {
                        mVelocityTracker!!.addMovement(motionEvent)
                        mVelocityTracker!!.computeCurrentVelocity(1000)
                        val velocityX = mVelocityTracker!!.xVelocity
                        val absVelocityX =
                            abs(velocityX.toDouble()).toFloat()
                        val absVelocityY =
                            abs(mVelocityTracker!!.yVelocity.toDouble()).toFloat()
                        if (minFlingVel <= absVelocityX && absVelocityX <= maxFlingVel && absVelocityY < absVelocityX && isFgSwiping
                        ) {
                            // dismiss only if flinging in the same direction as dragging
                            swipedLeftProper =
                                velocityX < 0 == (mFinalDelta < 0)
                            swipedRightProper =
                                velocityX > 0 == (mFinalDelta > 0)
                        }
                    }
                    // if swiped left properly and options menu isn't already visible, animate the foreground to the left
                    if (swipeAble && !swipedRight && swipedLeftProper && touchedPosition != RecyclerView.NO_POSITION && !bgVisible
                    ) {
                        val downView =
                            touchedView // touchedView gets null'd before animation ends
                        val downPosition = touchedPosition

                        animateFG(touchedView, Animation.OPEN, ANIMATION_STANDARD)
                        bgVisible = true
                        bgVisibleView = fgView
                        bgVisiblePosition = downPosition
                    } else if (swipeAble && !swipedLeft && swipedRightProper && touchedPosition != RecyclerView.NO_POSITION && bgVisible
                    ) {
                        // dismiss
                        val downView =
                            touchedView // touchedView gets null'd before animation ends
                        val downPosition = touchedPosition

                        animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD)
                        bgVisible = false
                        bgVisibleView = null
                        bgVisiblePosition = -1
                    } else if (swipeAble && swipedLeft && !bgVisible) {
                        // cancel
                        val tempBgView = bgView
                        animateFG(
                            touchedView,
                            Animation.CLOSE,
                            ANIMATION_STANDARD,
                            object :
                                OnSwipeListener {
                                override fun onSwipeOptionsClosed() {
                                    if (tempBgView != null) tempBgView.visibility = View.VISIBLE
                                }

                                override fun onSwipeOptionsOpened() {
                                }
                            })

                        bgVisible = false
                        bgVisibleView = null
                        bgVisiblePosition = -1
                    } else if (swipeAble && swipedRight && bgVisible) {
                        // cancel
                        animateFG(touchedView, Animation.OPEN, ANIMATION_STANDARD)
                        bgVisible = true
                        bgVisibleView = fgView
                        bgVisiblePosition = touchedPosition
                    } else if (swipeAble && swipedRight && !bgVisible) {
                        // cancel
                        animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD)
                        bgVisible = false
                        bgVisibleView = null
                        bgVisiblePosition = -1
                    } else if (swipeAble && swipedLeft && bgVisible) {
                        // cancel
                        animateFG(touchedView, Animation.OPEN, ANIMATION_STANDARD)
                        bgVisible = true
                        bgVisibleView = fgView
                        bgVisiblePosition = touchedPosition
                    }else if (!swipedRight && !swipedLeft) {
                        // if partial foreground view is clicked (see ACTION_DOWN) bring foreground back to original position
                        // bgVisible is true automatically since it's already checked in ACTION_DOWN block
                        if (swipeAble && fgPartialViewClicked) {
                            animateFG(touchedView, Animation.CLOSE, ANIMATION_STANDARD)
                            bgVisible = false
                            bgVisibleView = null
                            bgVisiblePosition = -1
                        }
                        else if (clickable && !bgVisible && touchedPosition >= 0 && !isRViewScrolling) {
                            mRowClickListener!!.onRowClicked(touchedPosition)
                        }
                        else if (swipeAble && bgVisible && !fgPartialViewClicked) {
                            val optionID = getOptionViewID(motionEvent)
                            if (optionID >= 0 && touchedPosition >= 0) {
                                val downPosition = touchedPosition
                                closeVisibleBG(object : OnSwipeListener {
                                    override fun onSwipeOptionsClosed() {
                                        mBgClickListener!!.onSwipeOptionClicked(
                                            optionID,
                                            downPosition
                                        )
                                    }
                                    override fun onSwipeOptionsOpened() {
                                    }
                                })
                            }
                        }
                    }
                }
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
                bgView = null
            }
            MotionEvent.ACTION_MOVE -> {
                if (mVelocityTracker == null || mPaused || !swipeAble) {
                    return false
                }
                mVelocityTracker!!.addMovement(motionEvent)
                val deltaX = motionEvent.rawX - touchedX
                val deltaY = motionEvent.rawY - touchedY

                /*
                 * isFgSwiping variable which is set to true here is used to alter the swipedLeft, swipedRightProper
                 * variables in "ACTION_UP" block by checking if user is actually swiping at present or not
                 */
                if (!isFgSwiping && abs(deltaX.toDouble()) > touchSlop &&
                    abs(touchSlop.toDouble()) < abs(deltaX.toDouble()) / 2
                ) {
                    isFgSwiping = true
                    mSwipingSlop = (if (deltaX > 0) touchSlop else -touchSlop)
                }
                if (swipeAble && isFgSwiping) {
                    if (bgView == null) {
                        bgView = touchedView!!.findViewById(bgViewID)
                        bgView?.visibility = View.VISIBLE
                    }
                    // if fg is being swiped left
                    if (deltaX < touchSlop && !bgVisible) {
                        val translateAmount = deltaX - mSwipingSlop
                        fgView!!.translationX =
                            if (abs(translateAmount.toDouble()) > bgWidth) -bgWidth.toFloat() else translateAmount
                        if (fgView!!.translationX > 0) fgView!!.translationX = 0f

                    } else if (deltaX > 0 && bgVisible) {
                        // for closing rightOptions
                        if (bgVisible) {
                            val translateAmount = (deltaX - mSwipingSlop) - bgWidth

                            // swipe fg till it reaches original position. If swiped further, nothing happens (stalls at 0)
                            fgView!!.translationX = if (translateAmount > 0) 0f else translateAmount

                        } else {
                            val translateAmount = (deltaX - mSwipingSlop) - bgWidth

                            // swipe fg till it reaches original position. If swiped further, nothing happens (stalls at 0)
                            fgView!!.translationX = if (translateAmount > 0) 0f else translateAmount
                        }
                    }
                    return true
                }
            }
        }
        return false
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

    private enum class Animation {
        OPEN, CLOSE
    }

    interface OnSwipeOptionsClickListener {
        fun onSwipeOptionClicked(viewID: Int, position: Int)
    }

    interface OnRowClickListener {
        fun onRowClicked(position: Int)
    }

    interface RecyclerTouchListenerHelper {
        fun setOnActivityTouchListener(listener: OnActivityTouchListener?)
    }

    interface OnSwipeListener {
        fun onSwipeOptionsClosed()

        fun onSwipeOptionsOpened()
    }
    companion object {
        private const val TAG = "RecyclerTouchListener"
    }

}