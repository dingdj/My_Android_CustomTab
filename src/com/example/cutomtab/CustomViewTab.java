package com.example.cutomtab;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author dingdj
 * Date:2014-2-7上午10:31:04
 *
 */
public class CustomViewTab extends View{
	
	/**
	 *  tab 之间的分割线
	 */
	private Drawable tabDivideImg;
	/**
	 * 选定tab的底部图标
	 */
	private Drawable tabSelectedBottom;
	private TextPaint textPaint = new TextPaint();
	
	/**
	 * selectTextColor:选中的文字颜色
	 * defaultTextColor:默认的文字颜色
	 * selectShadowColor:选中的文字背景颜色
	 * defaultShadowColor:未选中文字背景颜色
	 */
	private int selectTextColor, defaultTextColor;
	
	/**
	 * textHeight:文字的高度
	 * textMargin:文字的外边距
	 */
	private int textHeight, textMargin;
	
	private List<TabInfo> tabs = new ArrayList<TabInfo>();
	
	private int textTop;
	
	/**
	 * 滑动的比例
	 */
	private float distanceScale;
	
	/**
	 * 当前选中的tab
	 */
	private int selectTab = -1;
	
	/**
	 * 
	 */
	private int touchedTab = -1;
	
	/**
	 * 每个tab的宽度
	 */
	private float subTabWidth;
	
	private CustomViewPager pager;
	
	public CustomViewTab(Context context) {
		super(context);
		init(context);
	}

	public CustomViewTab(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CustomViewTab(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	/**
	 * 初始化参数
	 * @author dingdj
	 * Date:2014-2-7上午10:34:17
	 *  @param context
	 */
	private void init(Context context){
		Resources res = context.getResources();
		selectTextColor = res.getColor(R.color.tab_selected_text_color);
		defaultTextColor =  res.getColor(R.color.tab_default_text_color);
		
		textPaint.setAntiAlias(true);
		textPaint.setColor(defaultTextColor);
		textPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.tab_textsize));
		textHeight = textPaint.getFontMetricsInt(null);
		textMargin = textHeight / 2;
		
		tabSelectedBottom =  context.getResources().getDrawable(R.drawable.tab_selected_bottom);
		tabDivideImg = context.getResources().getDrawable(R.drawable.tab_split);
		setBackgroundResource(R.drawable.tab_bg);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int N = tabs.size();
		if (N <= 1)
			return;
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		subTabWidth = width / N;
		textTop = (height + textMargin) / 2;
		float startLeft = 0;
		for (int i = 0; i < N; i++) {
			TabInfo info = tabs.get(i);
			info.tabWidth = subTabWidth;
			info.tabLeft = startLeft + subTabWidth*i;
			info.tabCenter = info.tabLeft + info.tabWidth / 2;
			info.textTop = textTop;
			info.textLeft = info.tabLeft + subTabWidth / 2 - info.textWidth / 2;
		}
		distanceScale = subTabWidth/(width+0L);
		tabSelectedBottom.setBounds((int) tabs.get(selectTab).tabLeft, 0, (int) (tabs.get(selectTab).tabLeft+subTabWidth), height);
	}
	
	
	@Override
	protected void onDraw(Canvas canvas){
		final int N = tabs.size();
		if (N <= 1)
			return;
		
		tabSelectedBottom.draw(canvas);
		Rect bgRect = tabSelectedBottom.getBounds();
		for (int i = 0; i < N; i++) {
			TabInfo info = tabs.get(i);
			if (i == selectTab || i == touchedTab) {
				textPaint.setColor(selectTextColor);
			} else {
				textPaint.setColor(defaultTextColor);
			}
			char[] cs = info.title.toCharArray();
			float leftToal = info.textLeft;
			for(int j = 0 ; j < cs.length ; j++){
				char c = cs[j];
				float wordW = textPaint.measureText(c + "");
				float temp = leftToal + wordW;
				Rect rect = new Rect((int)temp, (int)(info.textTop), (int)temp, (int)(info.textTop + 5));
				if (bgRect.contains(rect)) {
					textPaint.setColor(selectTextColor);
				} else {
					textPaint.setColor(defaultTextColor);
				}
				canvas.drawText(c + "", leftToal, info.textTop, textPaint);
				leftToal = temp;
			}
		}
		
		if (null != tabDivideImg) {
			int divideWidth = dip2px(this.getContext(), 1);
			int divideHeight = textHeight;
			int divideTop = textTop - textHeight;
			for (int i = 0; i < N - 1; i++) {
				int divideLeft = (i + 1) * (int)subTabWidth - divideWidth / 2;
				tabDivideImg.setBounds(divideLeft, 
						               divideTop - divideHeight / 2, 
						               divideLeft + divideWidth, 
						               divideTop + divideHeight * 2);
				tabDivideImg.draw(canvas);
			}
		}
	}
	
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
			TabInfo hintInfo = getHitRangeInfo(event);
			if (hintInfo != null) {
				pager.snapToScreen(tabs.indexOf(hintInfo));
			}
			touchedTab = -1;
			invalidate();
		} else if (action == MotionEvent.ACTION_DOWN) {
			TabInfo hintInfo = getHitRangeInfo(event);
			if (hintInfo != null) {
				touchedTab = tabs.indexOf(hintInfo);
				invalidate();
			}
		}
		return true;
	}

	
	/**
	 * 默认显示的TAB
	 * @param title
	 */
	public void addTitle(String title[]) {
		final int N = title.length;
		for (int i = 0; i < N; i++) {
			TabInfo tab = new TabInfo();
			tab.title = title[i];
			tab.textWidth = textPaint.measureText(tab.title);
			tab.tabWidth = textPaint.measureText(tab.title);
			tabs.add(tab);
			if (tab.tabWidth > subTabWidth)
				subTabWidth = tab.tabWidth;
		}

		if (selectTab == -1)
			selectTab = (N - 1) / 2;
	}
	
	/**
	 * tab高亮滑动
	 * @author dingdj
	 * Date:2014-2-7下午5:22:09
	 *  @param scrollX
	 */
	public void scrollHighLight(int scrollX) {
		int left = (int) (scrollX * distanceScale);
		tabSelectedBottom.setBounds(left, 0, (int) (left + subTabWidth), this.getHeight());
		invalidate();
	}
	
	/**
	 * 计算滑动停止时所处的tab
	 * @author dingdj
	 * Date:2014-2-7下午5:25:17
	 */
	public void updateSelected() {
		Rect bound = tabSelectedBottom.getBounds();
		final int y = bound.top + (bound.bottom - bound.top) / 2;
		for (int i = 0; i < tabs.size(); i++) {
			if (!bound.contains((int) tabs.get(i).tabCenter, y))
				continue;
			selectTab = i;
			invalidate();
			break;
		}
	}
	
	class TabInfo {
		Drawable logo;
		String title;
		float textWidth;
		float textHeight;
		float textLeft;
		float textTop;
		float tabWidth;
		float tabLeft; 
		float tabCenter;
	}

	/**
	 * 从点击的坐标计算tab页
	 * @author dingdj
	 * Date:2014-2-7下午4:27:47
	 *  @param e
	 *  @return
	 */
	private TabInfo getHitRangeInfo(MotionEvent e) {
		int x = Math.round(e.getX());
		int y = Math.round(e.getY());
		Rect hitRect;
		for (int index = 0; index < tabs.size(); index++) {
			TabInfo info = tabs.get(index);
			hitRect = new Rect((int) (index * info.tabWidth), 
					(int) (info.textTop - textHeight - textMargin), 
					(int) ((index + 1) * info.tabWidth), 
					(int) (info.textTop + textMargin));
			if (hitRect.contains(x, y)) {
				return info;
			}
		}

		return null;
	}
	
	/**
	 * 取相反颜色
	 * @param alpha
	 * @param color
	 * @return int
	 */
	public static int antiColorAlpha(int alpha, int color) {
		if(-1 == alpha){
			alpha = Color.alpha(color);
			if(255 == alpha){
				alpha = 200;
			}
		}
		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);
		return Color.argb(alpha, 255 - r, 255 - g, 255 - b);
	}
	
	/**
	 * dp转px
	 * @param context
	 * @param dipValue
	 * @return int
	 */
	public static int dip2px(Context context, float dipValue) {
		float currentDensity = 0L;
		if (currentDensity > 0)
			return (int) (dipValue * currentDensity + 0.5f);

		currentDensity = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * currentDensity + 0.5f);
	}

	public void setPager(CustomViewPager pager) {
		this.pager = pager;
	}
	
	

}
