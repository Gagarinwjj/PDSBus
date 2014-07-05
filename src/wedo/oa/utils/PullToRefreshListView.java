package wedo.oa.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import wedo.oa.acitvity.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PullToRefreshListView extends ListView implements OnScrollListener {

	private static final int TAP_TO_REFRESH = 1;
	private static final int PULL_TO_REFRESH = 2;
	private static final int RELEASE_TO_REFRESH = 3;
	private static final int REFRESHING = 4;

	private static final String TAG = "PullToRefreshListView";

	private OnRefreshListener mOnRefreshListener;
	private OnAddMoreListener mOnAddMoreListener;

	/**
	 * Listener that will receive notifications every time the list scrolls.
	 */
	private OnScrollListener mOnScrollListener;
	private LayoutInflater mInflater;
	// 头部刷新
	private LinearLayout mRefreshView;
	private TextView mRefreshViewText;
	private ImageView mRefreshViewImage;
	private ProgressBar mRefreshViewProgress;
	private TextView mRefreshViewLastUpdated;
	// 底部加载更多
	private RelativeLayout mAddmoreView;
	private ImageView mAddmoreImageView;
	private TextView mInfoCurrentTextView;
	private TextView mInfoAllTextView;

	private int mCurrentScrollState;
	private int mRefreshState;

	private RotateAnimation mFlipAnimation;
	private RotateAnimation mReverseFlipAnimation;

	private int mRefreshViewHeight;
	private int mRefreshOriginalTopPadding;
	private int mLastMotionY;

	public PullToRefreshListView(Context context) {
		super(context);
		init(context);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		// Load all of the animations we need in code rather than through XML
		mFlipAnimation = new RotateAnimation(0, -180,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mFlipAnimation.setInterpolator(new LinearInterpolator());
		mFlipAnimation.setDuration(250);
		mFlipAnimation.setFillAfter(true);

		mReverseFlipAnimation = new RotateAnimation(-180, 0,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
		mReverseFlipAnimation.setDuration(250);
		mReverseFlipAnimation.setFillAfter(true);

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// 头部刷新图
		mRefreshView = (LinearLayout) mInflater.inflate(
				R.layout.pull_to_refresh_header, null);

		mRefreshViewText = (TextView) mRefreshView
				.findViewById(R.id.pull_to_refresh_text);
		mRefreshViewImage = (ImageView) mRefreshView
				.findViewById(R.id.pull_to_refresh_image);
		mRefreshViewProgress = (ProgressBar) mRefreshView
				.findViewById(R.id.pull_to_refresh_progress);
		mRefreshViewLastUpdated = (TextView) mRefreshView
				.findViewById(R.id.pull_to_refresh_updated_at);
		mRefreshViewImage.setMinimumHeight(50);

		// // 底部加载更多图
		mAddmoreView = (RelativeLayout) mInflater.inflate(R.layout.add_more,
				null);
		mAddmoreImageView = (ImageView) mAddmoreView.findViewById(R.id.loadimg);
		mInfoCurrentTextView = (TextView) mAddmoreView
				.findViewById(R.id.info_current);
		mInfoAllTextView = (TextView) mAddmoreView.findViewById(R.id.info_all);
		// 点击事件有bug，即点击后不可以拖拽，否则后面的点击失效！
		// 这是最初的认识，其实是因为数据太少，无法拖拽！有待优化
		// 使得少元素也可以拖拽！
		mRefreshView.setOnClickListener(new OnClickRefreshListener());
		mAddmoreView.setOnClickListener(loadMoreOnClickListener);
		mRefreshOriginalTopPadding = mRefreshView.getPaddingTop();

		mRefreshState = TAP_TO_REFRESH;

		addHeaderView(mRefreshView);
		addFooterView(mAddmoreView);

		super.setOnScrollListener(this);

		measureView(mRefreshView);
		mRefreshViewHeight = mRefreshView.getMeasuredHeight();
	}

	@Override
	protected void onAttachedToWindow() {
		// Android4.1中，PullToRefreshListView点击项目没反应，也不会报错，
		// 解决方法就是在 onAttachedToWindow()方法中加入 super.onAttachedToWindow();
		super.onAttachedToWindow();
		setSelection(1);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		if (adapter.getCount() > 0) {
			setSelection(1);
		} else {
			// 隐藏头尾标签
			// mRefreshView.setVisibility(View.GONE);
			// mAddmoreView.setVisibility(View.GONE);
			this.setVisibility(View.GONE);
		}
	}

	/**
	 * Set the listener that will receive notifications every time the list
	 * scrolls.
	 * 
	 * @param l
	 *            The scroll listener.
	 */
	@Override
	public void setOnScrollListener(AbsListView.OnScrollListener l) {
		mOnScrollListener = l;
	}

	/**
	 * Register a callback to be invoked when this list should be refreshed.
	 * 
	 * @param onRefreshListener
	 *            The callback to run.
	 */
	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		mOnRefreshListener = onRefreshListener;
	}

	/**
	 * Register a callback to be invoked when this list should addmore.
	 * 
	 * @param onAddMoreListener
	 *            The callback to run.
	 */
	public void setOnAddMoreListener(OnAddMoreListener onAddMoreListener) {
		mOnAddMoreListener = onAddMoreListener;
	}

	/**
	 * Set a text to represent when the list was last updated.
	 * 
	 * @param lastUpdated
	 *            Last updated at.
	 */
	public void setLastUpdated(CharSequence lastUpdated) {
		if (lastUpdated != null) {
			mRefreshViewLastUpdated.setVisibility(View.VISIBLE);
			mRefreshViewLastUpdated.setText("上次刷新: " + lastUpdated);
		} else {
			mRefreshViewLastUpdated.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int y = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			if (!isVerticalScrollBarEnabled()) {
				setVerticalScrollBarEnabled(true);
			}
			if (getFirstVisiblePosition() == 0 && mRefreshState != REFRESHING) {
				if ((mRefreshView.getBottom() > mRefreshViewHeight || mRefreshView
						.getTop() >= 0) && mRefreshState == RELEASE_TO_REFRESH) {
					// Initiate the refresh
					mRefreshState = REFRESHING;
					prepareForRefresh();
					onRefresh();
				} else if (mRefreshView.getBottom() < mRefreshViewHeight
						|| mRefreshView.getTop() < 0) {
					// Abort refresh and scroll down below the refresh view
					resetHeader();
					setSelection(1);
					// CustomToast.show(this.getContext(), "没有刷新哦");
				}
			}
			break;
		case MotionEvent.ACTION_DOWN:
			mLastMotionY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			applyHeaderPadding(event);
			break;
		}
		return super.onTouchEvent(event);
	}

	private void applyHeaderPadding(MotionEvent ev) {
		final int historySize = ev.getHistorySize();

		// Workaround for getPointerCount() which is unavailable in 1.5
		// (it's always 1 in 1.5)
		int pointerCount = 1;
		try {
			Method method = MotionEvent.class.getMethod("getPointerCount");
			pointerCount = (Integer) method.invoke(ev);
		} catch (NoSuchMethodException e) {
			pointerCount = 1;
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (IllegalAccessException e) {
			System.err.println("unexpected " + e);
		} catch (InvocationTargetException e) {
			System.err.println("unexpected " + e);
		}

		for (int h = 0; h < historySize; h++) {
			for (int p = 0; p < pointerCount; p++) {
				if (mRefreshState == RELEASE_TO_REFRESH) {
					if (isVerticalFadingEdgeEnabled()) {
						setVerticalScrollBarEnabled(false);
					}

					int historicalY = 0;
					try {
						// For Android > 2.0
						Method method = MotionEvent.class.getMethod(
								"getHistoricalY", Integer.TYPE, Integer.TYPE);
						historicalY = ((Float) method.invoke(ev, p, h))
								.intValue();
					} catch (NoSuchMethodException e) {
						// For Android < 2.0
						historicalY = (int) (ev.getHistoricalY(h));
					} catch (IllegalArgumentException e) {
						throw e;
					} catch (IllegalAccessException e) {
						System.err.println("unexpected " + e);
					} catch (InvocationTargetException e) {
						System.err.println("unexpected " + e);
					}

					// Calculate the padding to apply, we divide by 1.7 to
					// simulate a more resistant effect during pull.
					int topPadding = (int) (((historicalY - mLastMotionY) - mRefreshViewHeight) / 1.7);

					mRefreshView.setPadding(mRefreshView.getPaddingLeft(),
							topPadding, mRefreshView.getPaddingRight(),
							mRefreshView.getPaddingBottom());
				}
			}
		}
	}

	/**
	 * Sets the header padding back to original size.
	 */
	private void resetHeaderPadding() {
		mRefreshView.setPadding(mRefreshView.getPaddingLeft(),
				mRefreshOriginalTopPadding, mRefreshView.getPaddingRight(),
				mRefreshView.getPaddingBottom());
	}

	/**
	 * Resets the header to the original state.
	 */
	private void resetHeader() {
		if (mRefreshState != TAP_TO_REFRESH) {
			mRefreshState = TAP_TO_REFRESH;

			resetHeaderPadding();

			// Set refresh view text to the pull label
			mRefreshViewText.setText(R.string.pull_to_refresh_tap_label);
			// Replace refresh drawable with arrow drawable
			mRefreshViewImage
					.setImageResource(R.drawable.ic_pulltorefresh_arrow);
			// Clear the full rotation animation
			mRefreshViewImage.clearAnimation();
			// Hide progress bar and arrow.
			mRefreshViewImage.setVisibility(View.GONE);
			mRefreshViewProgress.setVisibility(View.GONE);
		}
	}

	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	// 在滚动的3种状态下，一直调用
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// When the refresh view is completely visible, change the text to say
		// "Release to refresh..." and flip the arrow drawable.
		if (mCurrentScrollState == SCROLL_STATE_TOUCH_SCROLL
				&& mRefreshState != REFRESHING) {
			if (firstVisibleItem == 0) {
				mRefreshViewImage.setVisibility(View.VISIBLE);
				if ((mRefreshView.getBottom() > mRefreshViewHeight + 20 || mRefreshView
						.getTop() >= 0) && mRefreshState != RELEASE_TO_REFRESH) {
					mRefreshViewText
							.setText(R.string.pull_to_refresh_release_label);
					mRefreshViewImage.clearAnimation();
					mRefreshViewImage.startAnimation(mFlipAnimation);
					mRefreshState = RELEASE_TO_REFRESH;
				} else if (mRefreshView.getBottom() < mRefreshViewHeight + 20
						&& mRefreshState != PULL_TO_REFRESH) {
					mRefreshViewText
							.setText(R.string.pull_to_refresh_pull_label);
					if (mRefreshState != TAP_TO_REFRESH) {
						mRefreshViewImage.clearAnimation();
						mRefreshViewImage.startAnimation(mReverseFlipAnimation);
					}
					mRefreshState = PULL_TO_REFRESH;
				}
			} else {
				mRefreshViewImage.setVisibility(View.GONE);
				resetHeader();

			}
		} else if (mCurrentScrollState == SCROLL_STATE_FLING
				&& firstVisibleItem == 0 && mRefreshState != REFRESHING) {
			setSelection(1);

		}

		if (mOnScrollListener != null) {
			mOnScrollListener.onScroll(view, firstVisibleItem,
					visibleItemCount, totalItemCount);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mCurrentScrollState = scrollState;
		// CustomToast.show(PullToRefreshListView.this.getContext(), "状态"
		// + scrollState);
		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	public void prepareForRefresh() {
		resetHeaderPadding();

		mRefreshViewImage.setVisibility(View.GONE);
		// We need this hack, otherwise it will keep the previous drawable.
		mRefreshViewImage.setImageDrawable(null);
		mRefreshViewProgress.setVisibility(View.VISIBLE);

		// Set refresh view text to the refreshing label
		mRefreshViewText.setText(R.string.pull_to_refresh_refreshing_label);

		mRefreshState = REFRESHING;
	}

	public void onRefresh() {
		Log.d(TAG, "onRefresh");
		// mAddmoreView.setVisibility(View.GONE);//隐藏底部
		if (mOnRefreshListener != null) {
			mOnRefreshListener.onRefresh();
		}
	}

	/**
	 * Resets the list to a normal state after a refresh.
	 * 
	 * @param lastUpdated
	 *            Last updated at.
	 */
	public void onRefreshComplete(CharSequence lastUpdated) {
		setLastUpdated(lastUpdated);
		onRefreshComplete();
	}

	/**
	 * Resets the list to a normal state after a refresh.
	 */
	public void onRefreshComplete() {
		Log.d(TAG, "onRefreshComplete");
		// mAddmoreView.setVisibility(View.VISIBLE);//显示底部
		resetHeader();
		// If refresh view is visible when loading completes, scroll down to
		// the next item.
		if (mRefreshView.getBottom() > 0) {
			// invalidateViews();
			// 不在这里重绘，则让客户端调用adapter.notifyDataSetChanged();
			setSelection(1);
		}
	}

	/**
	 * @param ishide
	 *            是否隐藏
	 * @param current
	 *            当前数据条数
	 * @param all
	 *            所有数据条数
	 */
	public void onAddmoreComplete(boolean ishide, int current, int all) {
		if (ishide) {
			// removeFooterView(mAddmoreView);//删除后就没有数据个数显示了
			mAddmoreImageView.clearAnimation();
			mAddmoreImageView.setVisibility(View.GONE);
			// mAddmoreView.setClickable(false);
			mAddmoreView.setOnClickListener(overLoadOnClickListener);

		} else {
			// 对于“新闻列表”而言，只要初始化一次监听就行
			// 但是对于“表单列表”而言，已办理，未办理、已委托共享这一个mAddmoreView。
			// 不能因为其他人不可以加载更多了，让能加载更多也不能了
			// 所以这里需要重新设置事件
			// 当然，可能原来就是这个事件，重新设置会照成重复new 同一个。浪费内存。尽管先前的会被回收。
			// 所以定义成全局变量overLoadOnClickListener和loadMoreOnClickListener，避免重复new。
			// 而且还可以发现，全局变量可以使用先，定义后。但是局部变量必须定义先，使用后。
			// **************分割线**************
			mAddmoreView.setOnClickListener(loadMoreOnClickListener);
			// 被其他列表结束 隐藏的 加号 恢复可见 |或者 可见 任可见
			mAddmoreImageView.setVisibility(View.VISIBLE);
			// **************分割线**************
			// 停止动画
			mAddmoreImageView.clearAnimation();
		}
		setBottomInfo(current, all);
	}

	View.OnClickListener overLoadOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			CustomToast
					.show(PullToRefreshListView.this.getContext(), "没有更多数据了");
		}
	};

	View.OnClickListener loadMoreOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mOnAddMoreListener != null) {
				RotateAnimation roate = new RotateAnimation(0, 360,
						Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				roate.setInterpolator(new LinearInterpolator());
				roate.setDuration(800);
				roate.setRepeatCount(Animation.INFINITE);
				mAddmoreImageView.startAnimation(roate);
				mOnAddMoreListener.onAddMore();
			}
		}
	};

	public void setBottomInfo(int current, int all) {
		mInfoCurrentTextView.setText("显示:" + current);
		mInfoAllTextView.setText("总共:" + all);
	}

	/**
	 * Invoked when the refresh view is clicked on. This is mainly used when
	 * there's only a few items in the list and it's not possible to drag the
	 * list. 解决只有少量元素，列表无法拖拽时的刷新
	 */
	private class OnClickRefreshListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (mRefreshState != REFRESHING && (getCount() < 10)) {
				prepareForRefresh();
				onRefresh();
			}
		}

	}

	/**
	 * Interface definition for a callback to be invoked when list should be
	 * refreshed.
	 */
	public interface OnRefreshListener {
		/**
		 * Called when the list should be refreshed.
		 * <p>
		 * A call to {@link PullToRefreshListView #onRefreshComplete()} is
		 * expected to indicate that the refresh has completed.
		 */
		public void onRefresh();
	}

	public interface OnAddMoreListener {
		public void onAddMore();
	}
}
