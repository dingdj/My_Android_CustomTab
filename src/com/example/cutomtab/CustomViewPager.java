package com.example.cutomtab;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * @author dingdj
 * Date:2014-2-7下午4:33:24
 *
 */
public class CustomViewPager extends ViewGroup {
	
	
	private Scroller mScroller;
	private CustomViewTab tab;
	/**
	 * 判断滑动的阈值
	 */
	private int mTouchSlop;
	
	/**
	 * 支持横向滑动的子View当前页
	 */
	private int mCurScreen;
	
	/**
	 * 支持横向滑动的子View总页数
	 */
	private int totalScreen = 0;
	
	
	
	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;
	private static final int SNAP_VELOCITY = 600;
	
	/**
	 * touch状态
	 */
	private int mTouchState = TOUCH_STATE_REST;
	
	private float mLastMotionX, mLastMotionY;
	
	private VelocityTracker mVelocityTracker;

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public CustomViewPager(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * @param context
	 */
	public CustomViewPager(Context context) {
		super(context);
		init(context);
	}
	
	/**
	 * 初始化参数
	 * @author dingdj
	 * Date:2014-2-7下午4:39:36
	 *  @param context
	 */
	private void init(Context context){
		mScroller = new Scroller(context);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;
		
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
            	final int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
	}
	
	/**
	 * 跳转到指定屏幕,带动画效果
	 * @param whichScreen
	 */
	public void snapToScreen(int whichScreen){
		if (whichScreen >= getChildCount()){
			return;
		}
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		if (getScrollX() != (whichScreen * getWidth())){
			final int delta = whichScreen * getWidth() - getScrollX();
			int duration = Math.abs(delta) * 2;
			if (duration > 500)
				duration = 500;
			mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
			mCurScreen = whichScreen;
			invalidate();
		}
	}
	
	
	/**
	 * 处理滑动动画
	 * @see android.view.View#computeScroll()
	 */
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
			tab.scrollHighLight(mScroller.getCurrX());
			// 判断滑动是否停止
			if (mScroller.isFinished()) {
				// 停止时更新选中的Tab标签
				tab.updateSelected();
			}
		}
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev){
		final int action = ev.getAction();
		//是滑动则拦截 不是滑动继续向下传递事件
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		final float x = ev.getX();
		final float y = ev.getY();

		switch (action) {
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(mLastMotionX - x);
			final int yDiff = (int) Math.abs(mLastMotionY - y);
			if (xDiff > mTouchSlop && yDiff < mTouchSlop && xDiff > yDiff) {
				mTouchState = TOUCH_STATE_SCROLLING;
			}
			break;
			
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mLastMotionY = y;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		boolean b = mTouchState != TOUCH_STATE_REST;
		if (b) {
			final int xDiff = (int) (mLastMotionX - x);
			if (Math.abs(xDiff) > mTouchSlop) {	
				if (xDiff < 0 && mCurScreen == 0) {
					return true;
				} else if (xDiff > 0 && mCurScreen == totalScreen - 1) {
					return true;
				} else {
					return false;
				}
			}
		}
		return b;
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mLastMotionX = x;
			mLastMotionY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			int deltaX = (int) (mLastMotionX - x);
				
			mLastMotionX = x;
			mLastMotionY = y;
			
			scrollBy(deltaX, 0);
			tab.scrollHighLight(this.getScrollX());
			break;	
		case MotionEvent.ACTION_UP:
			final VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000);

			int velocityX = (int) velocityTracker.getXVelocity();
			if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
				snapToScreen(mCurScreen - 1);
			} else if (velocityX < -SNAP_VELOCITY && mCurScreen < getChildCount() - 1) {
				snapToScreen(mCurScreen + 1);
			} else {
				snapToDestination();
			}
			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		return true;

	}
	
	/**
	 * 滑动到指定位置
	 * @author dingdj
	 * Date:2014-2-8上午9:53:48
	 */
	public void snapToDestination() {
		int screenWidth = getWidth();
		int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
		if(destScreen >= getChildCount()){
			destScreen = getChildCount() - 1;
		}
		snapToScreen(destScreen);
	}

	public void setTab(CustomViewTab tab) {
		this.tab = tab;
	}
}
